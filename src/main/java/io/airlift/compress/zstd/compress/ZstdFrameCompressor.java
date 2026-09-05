/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.airlift.compress.zstd.compress;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.Histogram;
import io.airlift.compress.zstd.huffman.HuffmanCompressionContext;
import io.airlift.compress.zstd.huffman.HuffmanCompressionTable;
import io.airlift.compress.zstd.huffman.HuffmanCompressor;
import io.airlift.compress.zstd.seq.SequenceEncoder;
import lombok.RequiredArgsConstructor;

import static io.airlift.compress.zstd.Constants.COMPRESSED_BLOCK;
import static io.airlift.compress.zstd.Constants.COMPRESSED_LITERALS_BLOCK;
import static io.airlift.compress.zstd.Constants.MAGIC_NUMBER;
import static io.airlift.compress.zstd.Constants.MAX_BLOCK_SIZE;
import static io.airlift.compress.zstd.Constants.MIN_BLOCK_SIZE;
import static io.airlift.compress.zstd.Constants.MIN_WINDOW_LOG;
import static io.airlift.compress.zstd.Constants.RAW_BLOCK;
import static io.airlift.compress.zstd.Constants.RAW_LITERALS_BLOCK;
import static io.airlift.compress.zstd.Constants.RLE_LITERALS_BLOCK;
import static io.airlift.compress.zstd.Constants.SIZE_OF_BLOCK_HEADER;
import static io.airlift.compress.zstd.Constants.SIZE_OF_INT;
import static io.airlift.compress.zstd.Constants.TREELESS_LITERALS_BLOCK;
import static io.airlift.compress.zstd.Util.checkArgument;
import static io.airlift.compress.zstd.Util.put24BitLittleEndian;
import static io.airlift.compress.zstd.huffman.Huffman.MAX_SYMBOL;
import static io.airlift.compress.zstd.huffman.Huffman.MAX_SYMBOL_COUNT;

@RequiredArgsConstructor
public class ZstdFrameCompressor {

    private static final int CHECKSUM_FLAG = 0b100;
    private static final int SINGLE_SEGMENT_FLAG = 0b100000;

    private static final int MINIMUM_LITERALS_SIZE = 63;

    // the maximum table log allowed for literal encoding per RFC 8478, section 4.2.1
    private static final int MAX_HUFFMAN_TABLE_LOG = 11;

    private static int writeMagic(ByteArrayWithOffs out, int offs) {
        return out.putInt(offs, MAGIC_NUMBER);
    }

    private final ByteArrayWithOffs in;
    private final int compressionLevel;

    // visible for testing
    private int writeFrameHeader(ByteArrayWithOffs out,
                                 final int startOffs,
                                 int windowSize) {
        int offs = startOffs;
        int contentSizeDescriptor = (in.buf.length >= 256 ? 1 : 0) + (in.buf.length >= 65536 + 256 ? 1 : 0);
        int frameHeaderDescriptor = (contentSizeDescriptor << 6) | CHECKSUM_FLAG; // dictionary ID missing

        boolean singleSegment = windowSize >= in.buf.length;
        if (singleSegment) {
            frameHeaderDescriptor |= SINGLE_SEGMENT_FLAG;
        }

        offs += out.putByte(offs, (byte) frameHeaderDescriptor);

        if (!singleSegment) {
            int base = Integer.highestOneBit(windowSize);

            int exponent = 32 - Integer.numberOfLeadingZeros(base) - 1;
            if (exponent < MIN_WINDOW_LOG) {
                throw new IllegalArgumentException("Minimum window size is " + (1 << MIN_WINDOW_LOG));
            }

            int remainder = windowSize - base;
            if (remainder % (base / 8) != 0) {
                throw new IllegalArgumentException(
                        "Window size of magnitude 2^" + exponent + " must be multiple of " + (base / 8));
            }

            // mantissa is guaranteed to be between 0-7
            int mantissa = remainder / (base / 8);
            int encoded = ((exponent - MIN_WINDOW_LOG) << 3) | mantissa;

            offs += out.putByte(offs, (byte) encoded);
        }

        switch (contentSizeDescriptor) {
            case 0:
                if (singleSegment) {
                    offs += out.putByte(offs, (byte) in.buf.length);
                }
                break;
            case 1:
                offs += out.putShort(offs, (short) (in.buf.length - 256));
                break;
            case 2:
                offs += out.putInt(offs, in.buf.length);
                break;
            default:
                throw new AssertionError();
        }

        return offs - startOffs;
    }

    // visible for testing
    private int writeChecksum(ByteArrayWithOffs out, int outOffs) {
        int inputSize = in.buf.length;
        long hash = XxHash64.hash(0, in, 0, inputSize);
        return out.putInt(outOffs, (int) hash);
    }

    public int compress(ByteArrayWithOffs out) {
        CompressionParameters parameters = CompressionParameters.compute(compressionLevel, in.buf.length);

        int offs = 0;

        offs += writeMagic(out, offs);
        offs += writeFrameHeader(out, offs, 1 << parameters.getWindowLog());
        offs += compressFrame(out, offs, parameters);
        offs += writeChecksum(out, offs);

        return offs;
    }

    private int compressFrame(ByteArrayWithOffs out, final int outOffs, CompressionParameters parameters) {
        int windowSize = 1 << parameters.getWindowLog(); // TODO: store window size in parameters directly?
        int blockSize = Math.min(MAX_BLOCK_SIZE, windowSize);

        int outputSize = out.buf.length - outOffs;
        int remaining = in.buf.length;

        int offs = outOffs;
        int inOffs = 0;

        CompressionContext context = new CompressionContext(parameters, in.buf.length);

        do {
            int lastBlockFlag = blockSize >= remaining ? 1 : 0;
            blockSize = Math.min(blockSize, remaining);

            int compressedSize = 0;
            if (remaining > 0) {
                compressedSize = compressBlock(in,
                                               inOffs,
                                               blockSize,
                                               out,
                                               offs + SIZE_OF_BLOCK_HEADER,
                                               outputSize - SIZE_OF_BLOCK_HEADER,
                                               context,
                                               parameters);
            }

            if (compressedSize == 0) { // block is not compressible
                checkArgument(blockSize + SIZE_OF_BLOCK_HEADER <= outputSize, "Output size too small");

                int blockHeader = lastBlockFlag | (RAW_BLOCK << 1) | (blockSize << 3);
                put24BitLittleEndian(out, offs, blockHeader);
                in.copyMemory(inOffs, out.buf, offs + SIZE_OF_BLOCK_HEADER, blockSize);
                compressedSize = SIZE_OF_BLOCK_HEADER + blockSize;
            } else {
                int blockHeader = lastBlockFlag | (COMPRESSED_BLOCK << 1) | (compressedSize << 3);
                put24BitLittleEndian(out, offs, blockHeader);
                compressedSize += SIZE_OF_BLOCK_HEADER;
            }

            inOffs += blockSize;
            remaining -= blockSize;
            offs += compressedSize;
            outputSize -= compressedSize;
        }
        while (remaining > 0);

        return (int) (offs - outOffs);
    }

    private static int compressBlock(ByteArrayWithOffs in,
                                     int inStartOffs,
                                     int inputSize,
                                     ByteArrayWithOffs out,
                                     int outputAddress,
                                     int outputSize,
                                     CompressionContext context,
                                     CompressionParameters parameters) {
        if (inputSize < MIN_BLOCK_SIZE + SIZE_OF_BLOCK_HEADER + 1) {
            //  don't even attempt compression below a certain input size
            return 0;
        }

        context.blockCompressionState.enforceMaxDistance(inStartOffs + inputSize, 1 << parameters.getWindowLog());
        context.sequenceStore.reset();

        int lastLiteralsSize = parameters.getStrategy()
                                         .getCompressor()
                                         .compressBlock(in,
                                                        inStartOffs,
                                                        inputSize,
                                                        context.sequenceStore,
                                                        context.blockCompressionState,
                                                        context.offsets,
                                                        parameters);

        int lastLiteralsAddress = inStartOffs + inputSize - lastLiteralsSize;

        // append [lastLiteralsAddress .. lastLiteralsSize] to sequenceStore literals buffer
        context.sequenceStore.appendLiterals(in.buf, lastLiteralsAddress, lastLiteralsSize);

        // convert length/offsets into codes
        context.sequenceStore.generateCodes();

        long outputLimit = outputAddress + outputSize;
        int output = outputAddress;

        int compressedLiteralsSize = encodeLiterals(
                context.huffmanContext,
                parameters,
                out,
                output,
                (int) (outputLimit - output),
                context.sequenceStore.literalsBuffer,
                context.sequenceStore.literalsLength);
        output += compressedLiteralsSize;

        int compressedSequencesSize = SequenceEncoder.compressSequences(out,
                                                                        output,
                                                                        (int) (outputLimit - output),
                                                                        context.sequenceStore,
                                                                        parameters.getStrategy(),
                                                                        context.sequenceEncodingContext);

        int compressedSize = compressedLiteralsSize + compressedSequencesSize;
        if (compressedSize == 0) {
            // not compressible
            return compressedSize;
        }

        // Check compressibility
        int maxCompressedSize = inputSize - calculateMinimumGain(inputSize, parameters.getStrategy());
        if (compressedSize > maxCompressedSize) {
            return 0; // not compressed
        }

        // confirm repeated offsets and entropy tables
        context.commit();

        return compressedSize;
    }

    private static int encodeLiterals(
            HuffmanCompressionContext context,
            CompressionParameters parameters,
            ByteArrayWithOffs out,
            int outOffs,
            int outputSize,
            byte[] literals,
            int literalsSize) {
        // TODO: move this to Strategy
        boolean bypassCompression =
                (parameters.getStrategy() == CompressionParameters.Strategy.FAST) && (parameters.getTargetLength() > 0);
        if (bypassCompression || literalsSize <= MINIMUM_LITERALS_SIZE) {
            return rawLiterals(out,
                               outOffs,
                               outputSize,
                               literals,
                               literalsSize);
        }

        int headerSize = 3 + (literalsSize >= 1024 ? 1 : 0) + (literalsSize >= 16384 ? 1 : 0);

        checkArgument(headerSize + 1 <= outputSize, "Output buffer too small");

        Histogram histogram = new Histogram(MAX_SYMBOL_COUNT);
        histogram.count(literals, literalsSize);
        int maxSymbol = histogram.findMaxSymbol(MAX_SYMBOL);
        int largestCount = histogram.findLargestCount(maxSymbol);

        int literalsAddress = 0;
        if (largestCount == literalsSize) {
            // all bytes in input are equal
            return rleLiterals(out,
                               outOffs,
                               literals,
                               literalsSize);
        } else if (largestCount <= (literalsSize >>> 7) + 4) {
            // heuristic: probably not compressible enough
            return rawLiterals(out,
                               outOffs,
                               outputSize,
                               literals,
                               literalsSize);
        }

        HuffmanCompressionTable previousTable = context.getPreviousTable();
        HuffmanCompressionTable table;
        int serializedTableSize;
        boolean reuseTable;

        boolean canReuse = previousTable.isValid(histogram.getCounts(), maxSymbol);

        // heuristic: use existing table for small inputs if valid
        // TODO: move to Strategy
        boolean preferReuse = parameters.getStrategy().ordinal() < CompressionParameters.Strategy.LAZY.ordinal() &&
                literalsSize <= 1024;
        if (preferReuse && canReuse) {
            table = previousTable;
            reuseTable = true;
            serializedTableSize = 0;
        } else {
            HuffmanCompressionTable newTable = context.borrowTemporaryTable();

            newTable.initialize(
                    histogram.getCounts(),
                    maxSymbol,
                    HuffmanCompressionTable.optimalNumberOfBits(MAX_HUFFMAN_TABLE_LOG, literalsSize, maxSymbol),
                    context.getCompressionTableWorkspace());

            serializedTableSize = newTable.write(out,
                                                 outOffs + headerSize,
                                                 outputSize - headerSize,
                                                 context.getTableWriterWorkspace());

            // Check if using previous huffman table is beneficial
            if (canReuse && previousTable.estimateCompressedSize(histogram.getCounts(), maxSymbol) <=
                    serializedTableSize + newTable.estimateCompressedSize(histogram.getCounts(), maxSymbol)) {
                table = previousTable;
                reuseTable = true;
                serializedTableSize = 0;
                context.discardTemporaryTable();
            } else {
                table = newTable;
                reuseTable = false;
            }
        }

        int compressedSize;
        boolean singleStream = literalsSize < 256;
        if (singleStream) {
            compressedSize = HuffmanCompressor.compressSingleStream(out,
                                                                    outOffs + headerSize + serializedTableSize,
                                                                    outputSize - headerSize - serializedTableSize,
                                                                    new ByteArrayWithOffs(literals),
                                                                    literalsAddress,
                                                                    literalsSize,
                                                                    table);
        } else {
            compressedSize = HuffmanCompressor.compress4streams(out,
                                                                outOffs + headerSize + serializedTableSize,
                                                                outputSize - headerSize - serializedTableSize,
                                                                new ByteArrayWithOffs(literals),
                                                                literalsAddress,
                                                                literalsSize,
                                                                table);
        }

        int totalSize = serializedTableSize + compressedSize;
        int minimumGain = calculateMinimumGain(literalsSize, parameters.getStrategy());

        if (compressedSize == 0 || totalSize >= literalsSize - minimumGain) {
            // incompressible or no savings

            // discard any temporary table we might have borrowed above
            context.discardTemporaryTable();

            return rawLiterals(out,
                               outOffs,
                               outputSize,
                               literals,
                               literalsSize);
        }

        int encodingType = reuseTable ? TREELESS_LITERALS_BLOCK : COMPRESSED_LITERALS_BLOCK;

        // Build header
        switch (headerSize) {
            case 3: { // 2 - 2 - 10 - 10
                int header = encodingType | ((singleStream ? 0 : 1) << 2) | (literalsSize << 4) | (totalSize << 14);
                put24BitLittleEndian(out, outOffs, header);
                break;
            }
            case 4: { // 2 - 2 - 14 - 14
                int header = encodingType | (2 << 2) | (literalsSize << 4) | (totalSize << 18);
                out.putInt(outOffs, header);
                break;
            }
            case 5: { // 2 - 2 - 18 - 18
                int header = encodingType | (3 << 2) | (literalsSize << 4) | (totalSize << 22);
                out.putInt(outOffs, header);
                out.putByte(outOffs + SIZE_OF_INT, (byte) (totalSize >>> 10));
                break;
            }
            default:  // not possible : headerSize is {3,4,5}
                throw new IllegalStateException();
        }

        return headerSize + totalSize;
    }

    private static int rleLiterals(ByteArrayWithOffs out,
                                   int outOffs,
                                   byte[] in,
                                   int inputSize) {
        int headerSize = 1 + (inputSize > 31 ? 1 : 0) + (inputSize > 4095 ? 1 : 0);

        switch (headerSize) {
            case 1: // 2 - 1 - 5
                out.putByte(outOffs, (byte) (RLE_LITERALS_BLOCK | (inputSize << 3)));
                break;
            case 2: // 2 - 2 - 12
                out.putShort(outOffs, (short) (RLE_LITERALS_BLOCK | (1 << 2) | (inputSize << 4)));
                break;
            case 3: // 2 - 2 - 20
                out.putInt(outOffs, RLE_LITERALS_BLOCK | 3 << 2 | inputSize << 4);
                break;
            default:   // impossible. headerSize is {1,2,3}
                throw new IllegalStateException();
        }

        out.putByte(outOffs + headerSize, in[0]);
        return headerSize + 1;
    }

    private static int calculateMinimumGain(int inputSize, CompressionParameters.Strategy strategy) {
        // TODO: move this to Strategy to avoid hardcoding a specific strategy here
        int minLog = strategy == CompressionParameters.Strategy.BTULTRA ? 7 : 6;
        return (inputSize >>> minLog) + 2;
    }

    private static int rawLiterals(ByteArrayWithOffs out,
                                   int outOffs,
                                   int outputSize,
                                   byte[] in,
                                   int inputSize) {
        int headerSize = 1;
        if (inputSize >= 32) {
            headerSize++;
        }
        if (inputSize >= 4096) {
            headerSize++;
        }

        checkArgument(inputSize + headerSize <= outputSize, "Output buffer too small");

        switch (headerSize) {
            case 1:
                out.putByte(outOffs, (byte) (RAW_LITERALS_BLOCK | (inputSize << 3)));
                break;
            case 2:
                out.putShort(outOffs, (short) (RAW_LITERALS_BLOCK | (1 << 2) | (inputSize << 4)));
                break;
            case 3:
                put24BitLittleEndian(out, outOffs, RAW_LITERALS_BLOCK | (3 << 2) | (inputSize << 4));
                break;
            default:
                throw new AssertionError();
        }

        // TODO: ensure this test is correct
        checkArgument(inputSize + 1 <= outputSize, "Output buffer too small");

        System.arraycopy(in, 0, out.buf, outOffs + headerSize, inputSize);
        return headerSize + inputSize;
    }
}
