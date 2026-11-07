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

import ru.olegcherednik.zip4jvm.utils.BitUtils;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.Histogram;
import io.airlift.compress.zstd.In;
import io.airlift.compress.zstd.InputStreamToInAdapter;
import io.airlift.compress.zstd.OutByteArrayWithOffs;
import io.airlift.compress.zstd.dep.InByteArrayWithOffs;
import io.airlift.compress.zstd.huffman.HuffmanCompressionContext;
import io.airlift.compress.zstd.huffman.HuffmanCompressionTable;
import io.airlift.compress.zstd.huffman.HuffmanCompressor;
import io.airlift.compress.zstd.seq.SequenceEncoder;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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
import static io.airlift.compress.zstd.huffman.HuffmanDecompressor.MAX_SYMBOL;
import static io.airlift.compress.zstd.huffman.HuffmanDecompressor.MAX_SYMBOL_COUNT;

@RequiredArgsConstructor
public class ZstdFrameCompressor {

    private static final int CHECKSUM_FLAG = 0b100;
    private static final int MINIMUM_LITERALS_SIZE = 63;

    // the maximum table log allowed for literal encoding per RFC 8478, section 4.2.1
    private static final int MAX_HUFFMAN_TABLE_LOG = 11;

    private static void writeMagic(OutByteArrayWithOffs out) {
        out.putInt(MAGIC_NUMBER);
    }

    private final InByteArrayWithOffs in;
    private final int compressionLevel;

    private void writeFrameHeader(OutByteArrayWithOffs out, CompressionParameters parameters) {
        boolean singleSegment = parameters.getWindowSize() >= in.available();
        int frameContentSizeFlag = writeFrameHeaderDescriptor(out, singleSegment);

        if (!singleSegment)
            writeWindowDescriptor(out, parameters.getWindowSize());

        writeFrameContentSize(out, singleSegment, frameContentSizeFlag);
    }

    private int writeFrameHeaderDescriptor(OutByteArrayWithOffs out, boolean singleSegment) {
        int frameContentSizeFlag = (in.available() >= 256 ? 1 : 0) + (in.available() >= 65536 + 256 ? 1 : 0);
        int frameHeaderDescriptor = (frameContentSizeFlag << 6) | CHECKSUM_FLAG; // dictionary ID missing

        if (singleSegment)
            frameHeaderDescriptor = BitUtils.setBits(frameHeaderDescriptor, BitUtils.BIT5);

        out.putByte((byte) frameHeaderDescriptor);
        return frameContentSizeFlag;
    }

    private static void writeWindowDescriptor(OutByteArrayWithOffs out, int windowSize) {
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

        out.putByte((byte) encoded);
    }

    private void writeFrameContentSize(OutByteArrayWithOffs out, boolean singleSegment, int frameContentSizeFlag) {
        if (frameContentSizeFlag == 0) {
            if (singleSegment)
                out.putByte((byte) in.available());
        } else if (frameContentSizeFlag == 1)
            out.putShort((short) (in.available() - 256));
        else if (frameContentSizeFlag == 2)
            out.putInt(in.available());
        else
            throw new AssertionError();
    }

    private void writeChecksum(OutByteArrayWithOffs out) {
        XXHash64 checksum = new XXHash64();
        checksum.update(in.buf);
        out.putInt((int) checksum.getValue());
    }

    public void compress(OutByteArrayWithOffs out) throws IOException {
        CompressionParameters parameters = CompressionParameters.compute(compressionLevel, in.available());

        writeMagic(out);
        writeFrameHeader(out, parameters);
        compressFrame(out, parameters);
        writeChecksum(out);
    }

    private int compressFrame(OutByteArrayWithOffs out, CompressionParameters parameters) throws IOException {
        final int startOffs = out.getOffs();
        int blockSize = Math.min(MAX_BLOCK_SIZE, parameters.getWindowSize());
        int outputSize = out.available();
        int remaining = in.available();

        int offs = startOffs;
        int inOffs = 0;

        CompressionContext context = new CompressionContext(parameters, in.buf.length);

        do {
            boolean lastBlockFlag = blockSize >= remaining;
            blockSize = Math.min(blockSize, remaining);

            int compressedSize = 0;
            if (remaining > 0) {
                out.setOffs(offs + SIZE_OF_BLOCK_HEADER);
                compressedSize = compressBlock(in, inOffs, blockSize,
                                               out, outputSize - SIZE_OF_BLOCK_HEADER,
                                               context,
                                               parameters);
            }

            out.setOffs(offs);

            if (compressedSize == 0) { // block is not compressible
                checkArgument(blockSize + SIZE_OF_BLOCK_HEADER <= outputSize, "Output size too small");

                int blockHeader = (lastBlockFlag ? 1 : 0) | (RAW_BLOCK << 1) | (blockSize << 3);
                out.putThreeBytes(blockHeader);
                in.copyMemory(inOffs, out.buf, offs + SIZE_OF_BLOCK_HEADER, blockSize);
                compressedSize = SIZE_OF_BLOCK_HEADER + blockSize;
            } else {
                int blockHeader = (lastBlockFlag ? 1 : 0) | (COMPRESSED_BLOCK << 1) | (compressedSize << 3);
                out.putThreeBytes(blockHeader);
                compressedSize += SIZE_OF_BLOCK_HEADER;
            }

            inOffs += blockSize;
            remaining -= blockSize;
            offs += compressedSize;
            outputSize -= compressedSize;
        }
        while (remaining > 0);

        out.setOffs(offs);

        return offs - startOffs;
    }

    private static int compressBlock(InByteArrayWithOffs in, int inStartOffs, int inputSize,
                                     OutByteArrayWithOffs out, int outputSize,
                                     CompressionContext context,
                                     CompressionParameters parameters) throws IOException {
        int outputAddress = out.getOffs();
        if (inputSize < MIN_BLOCK_SIZE + SIZE_OF_BLOCK_HEADER + 1) {
            //  don't even attempt compression below a certain input size
            return 0;
        }

        int inputLimit = inStartOffs + inputSize;

        context.blockCompressionState.enforceMaxDistance(inputLimit, parameters.getWindowSize());
        context.sequenceStore.reset();

        in.setOffs(inStartOffs);
        in.setSize(inputSize);
        int lastLiteralsSize = parameters.getStrategy()
                                         .getCompressor()
                                         .compressBlock(in, context, parameters);
        int lastLiteralsAddress = inputLimit - lastLiteralsSize;

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
                context.sequenceStore.literalsBuffer);
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
            OutByteArrayWithOffs out,
            int outOffs,
            int outputSize,
            ByteArrayWithOffs literals) throws IOException {
        // TODO: move this to Strategy
        boolean bypassCompression =
                (parameters.getStrategy() == CompressionParameters.Strategy.FAST) && (parameters.getTargetLength() > 0);
        if (bypassCompression || literals.getOffs() <= MINIMUM_LITERALS_SIZE) {
            out.setOffs(outOffs);
            return rawLiterals(out, outputSize, literals);
        }

        int headerSize = 3 + (literals.getOffs() >= 1024 ? 1 : 0) + (literals.getOffs() >= 16384 ? 1 : 0);

        checkArgument(headerSize + 1 <= outputSize, "Output buffer too small");

        Histogram histogram = new Histogram(MAX_SYMBOL_COUNT);
        histogram.count(literals);
        int maxSymbol = histogram.findMaxSymbol(MAX_SYMBOL);
        int largestCount = histogram.findLargestCount(maxSymbol);

        int literalsAddress = 0;
        if (largestCount == literals.getOffs()) {
            // all bytes in input are equal
            return rleLiterals(out, outOffs, literals);
        } else if (largestCount <= (literals.getOffs() >>> 7) + 4) {
            // heuristic: probably not compressible enough
            out.setOffs(outOffs);
            return rawLiterals(out, outputSize, literals);
        }

        HuffmanCompressionTable previousTable = context.getPreviousTable();
        HuffmanCompressionTable table;
        int serializedTableSize;
        boolean reuseTable;

        boolean canReuse = previousTable.isValid(histogram.getCounts(), maxSymbol);

        // heuristic: use existing table for small inputs if valid
        // TODO: move to Strategy
        boolean preferReuse = parameters.getStrategy().ordinal() < CompressionParameters.Strategy.LAZY.ordinal() &&
                literals.getOffs() <= 1024;
        if (preferReuse && canReuse) {
            table = previousTable;
            reuseTable = true;
            serializedTableSize = 0;
        } else {
            HuffmanCompressionTable newTable = context.borrowTemporaryTable();

            newTable.init(
                    histogram.getCounts(),
                    maxSymbol,
                    HuffmanCompressionTable.optimalNumberOfBits(MAX_HUFFMAN_TABLE_LOG, literals.getOffs(), maxSymbol),
                    context.getCompressionTableWorkspace());

            out.setOffs(outOffs + headerSize);

            serializedTableSize = newTable.write(out, outputSize - headerSize);

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

        boolean singleStream = literals.getOffs() < 256;
        out.setOffs(outOffs + headerSize + serializedTableSize);

        In in = new InputStreamToInAdapter(new ByteArrayInputStream(literals.buf));
        HuffmanCompressor huffmanCompressor = new HuffmanCompressor(in, table);

        int compressedSize = singleStream ? huffmanCompressor.compressSingleStream(out, literalsAddress)
                                          : huffmanCompressor.compress4streams(out, literals.getOffs());

        int totalSize = serializedTableSize + compressedSize;
        int minimumGain = calculateMinimumGain(literals.getOffs(), parameters.getStrategy());

        if (compressedSize == 0 || totalSize >= literals.getOffs() - minimumGain) {
            // incompressible or no savings
            // discard any temporary table we might have borrowed above
            context.discardTemporaryTable();
            out.setOffs(outOffs);
            return rawLiterals(out, outputSize, literals);
        }

        int encodingType = reuseTable ? TREELESS_LITERALS_BLOCK : COMPRESSED_LITERALS_BLOCK;

        // Build header
        switch (headerSize) {
            case 3: { // 2 - 2 - 10 - 10
                int header =
                        encodingType | ((singleStream ? 0 : 1) << 2) | (literals.getOffs() << 4) | (totalSize << 14);
                out.setOffs(outOffs);
                out.putThreeBytes(header);
                break;
            }
            case 4: { // 2 - 2 - 14 - 14
                int header = encodingType | (2 << 2) | (literals.getOffs() << 4) | (totalSize << 18);
                out.putInt(outOffs, header);
                break;
            }
            case 5: { // 2 - 2 - 18 - 18
                int header = encodingType | (3 << 2) | (literals.getOffs() << 4) | (totalSize << 22);
                out.putInt(outOffs, header);
                out.putByte(outOffs + SIZE_OF_INT, (byte) (totalSize >>> 10));
                break;
            }
            default:  // not possible : headerSize is {3,4,5}
                throw new IllegalStateException();
        }

        return headerSize + totalSize;
    }

    private static int rleLiterals(OutByteArrayWithOffs out, int outOffs, ByteArrayWithOffs in) {
        int headerSize = 1 + (in.getOffs() > 31 ? 1 : 0) + (in.getOffs() > 4095 ? 1 : 0);

        switch (headerSize) {
            case 1: // 2 - 1 - 5
                out.putByte(outOffs, (byte) (RLE_LITERALS_BLOCK | (in.getOffs() << 3)));
                break;
            case 2: // 2 - 2 - 12
                out.putShort(outOffs, (short) (RLE_LITERALS_BLOCK | (1 << 2) | (in.getOffs() << 4)));
                break;
            case 3: // 2 - 2 - 20
                out.putInt(outOffs, RLE_LITERALS_BLOCK | 3 << 2 | in.getOffs() << 4);
                break;
            default:   // impossible. headerSize is {1,2,3}
                throw new IllegalStateException();
        }

        out.putByte(outOffs + headerSize, in.buf[0]);
        return headerSize + 1;
    }

    private static int calculateMinimumGain(int inputSize, CompressionParameters.Strategy strategy) {
        return (inputSize >>> strategy.getMinLog()) + 2;
    }

    private static int rawLiterals(OutByteArrayWithOffs out, int outputSize, ByteArrayWithOffs in) {
        final int loOffs = out.getOffs();
        int headerSize = 1;
        if (in.getOffs() >= 32) {
            headerSize++;
        }
        if (in.getOffs() >= 4096) {
            headerSize++;
        }

        checkArgument(in.getOffs() + headerSize <= outputSize, "Output buffer too small");

        switch (headerSize) {
            case 1:
                out.putByte((byte) (RAW_LITERALS_BLOCK | (in.getOffs() << 3)));
                break;
            case 2:
                out.putShort((short) (RAW_LITERALS_BLOCK | (1 << 2) | (in.getOffs() << 4)));
                break;
            case 3:
                out.putThreeBytes(RAW_LITERALS_BLOCK | (3 << 2) | (in.getOffs() << 4));
                break;
            default:
                throw new AssertionError();
        }

        // TODO: ensure this test is correct
        checkArgument(in.getOffs() + 1 <= outputSize, "Output buffer too small");

        System.arraycopy(in.buf, 0, out.buf, loOffs + headerSize, in.getOffs());
        return headerSize + in.getOffs();
    }
}
