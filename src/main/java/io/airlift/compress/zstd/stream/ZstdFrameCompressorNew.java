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
package io.airlift.compress.zstd.stream;

import io.airlift.compress.Foo;
import io.airlift.compress.zstd.CompressionParameters;
import io.airlift.compress.zstd.Histogram;
import io.airlift.compress.zstd.HuffmanCompressionTable;
import io.airlift.compress.zstd.XxHash64;

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
import static io.airlift.compress.zstd.Huffman.MAX_SYMBOL;
import static io.airlift.compress.zstd.Huffman.MAX_SYMBOL_COUNT;
import static io.airlift.compress.zstd.UnsafeUtil.UNSAFE;
import static sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET;

class ZstdFrameCompressorNew
{
    private static final int CHECKSUM_FLAG = 0b100;
    private static final int SINGLE_SEGMENT_FLAG = 0b100000;

    private static final int MINIMUM_LITERALS_SIZE = 63;

    // the maximum table log allowed for literal encoding per RFC 8478, section 4.2.1
    private static final int MAX_HUFFMAN_TABLE_LOG = 11;

    private ZstdFrameCompressorNew()
    {
    }

    // visible for testing
    static void writeMagic(Foo out)
    {
        out.putInt(MAGIC_NUMBER);
    }

    // visible for testing
    static void writeFrameHeader(Foo out, int inputSize, int windowSize)
    {
        int contentSizeDescriptor = (inputSize >= 256 ? 1 : 0) + (inputSize >= 65536 + 256 ? 1 : 0);
        int frameHeaderDescriptor = (contentSizeDescriptor << 6) | CHECKSUM_FLAG; // dictionary ID missing

        boolean singleSegment = windowSize >= inputSize;
        if (singleSegment) {
            frameHeaderDescriptor |= SINGLE_SEGMENT_FLAG;
        }

        out.putByte((byte) frameHeaderDescriptor);

        if (!singleSegment) {
            int base = Integer.highestOneBit(windowSize);

            int exponent = 32 - Integer.numberOfLeadingZeros(base) - 1;
            if (exponent < MIN_WINDOW_LOG) {
                throw new IllegalArgumentException("Minimum window size is " + (1 << MIN_WINDOW_LOG));
            }

            int remainder = windowSize - base;
            if (remainder % (base / 8) != 0) {
                throw new IllegalArgumentException("Window size of magnitude 2^" + exponent + " must be multiple of " + (base / 8));
            }

            // mantissa is guaranteed to be between 0-7
            int mantissa = remainder / (base / 8);
            int encoded = ((exponent - MIN_WINDOW_LOG) << 3) | mantissa;

            out.putByte((byte) encoded);
        }

        switch (contentSizeDescriptor) {
            case 0:
                if (singleSegment) {
                    out.putByte((byte) inputSize);
                }
                break;
            case 1:
                out.putShort((short) (inputSize - 256));
                break;
            case 2:
                out.putInt(inputSize);
                break;
            default:
                throw new AssertionError();
        }
    }

    // visible for testing
    static void writeChecksum(Foo out, Object inputBase, long inputAddress, long inputLimit)
    {
        int inputSize = (int) (inputLimit - inputAddress);
        long hash = XxHash64.hash(0, inputBase, inputAddress, inputSize);
        out.putInt((int) hash);
    }

    public static void compress(Object inputBase, long inputAddress, long inputLimit, Foo out, int compressionLevel)
    {
        int inputSize = (int) (inputLimit - inputAddress);

        CompressionParameters parameters = CompressionParameters.compute(compressionLevel, inputSize);
        writeMagic(out);
        writeFrameHeader(out, inputSize, 1 << parameters.getWindowLog());
        compressFrame(inputBase, inputAddress, inputLimit, out, parameters);
        writeChecksum(out, inputBase, inputAddress, inputLimit);
    }

    private static void compressFrame(Object inputBase, long inputAddress, long inputLimit, Foo out, CompressionParameters parameters)
    {
        int windowSize = 1 << parameters.getWindowLog(); // TODO: store window size in parameters directly?
        int blockSize = Math.min(MAX_BLOCK_SIZE, windowSize);
        int remaining = (int) (inputLimit - inputAddress);
        long input = inputAddress;

        CompressionContextNew context = new CompressionContextNew(parameters, inputAddress, remaining);
        Foo outTmp = new Foo(1_000_000);

        do {
            int lastBlockFlag = blockSize >= remaining ? 1 : 0;
            blockSize = Math.min(blockSize, remaining);

            int compressedSize = 0;
            if (remaining > 0) {
                outTmp.init();
                compressBlock(inputBase, input, blockSize, outTmp, context, parameters);
                compressedSize = outTmp.getSize();
            }

            if (compressedSize == 0) { // block is not compressible
                int blockHeader = lastBlockFlag | (RAW_BLOCK << 1) | (blockSize << 3);
                UtilNew.put24BitLittleEndian(out, blockHeader);
                out.copyMemory(inputBase, input, blockSize);
            }
            else {
                int blockHeader = lastBlockFlag | (COMPRESSED_BLOCK << 1) | (compressedSize << 3);
                UtilNew.put24BitLittleEndian(out, blockHeader);
                out.copyMemory(outTmp.getCompressed(), outTmp.getSize());
            }

            input += blockSize;
            remaining -= blockSize;
        }
        while (remaining > 0);
    }

    private static void compressBlock(Object inputBase, long inputAddress, int inputSize, Foo out, CompressionContextNew context, CompressionParameters parameters)
    {
        if (inputSize < MIN_BLOCK_SIZE + SIZE_OF_BLOCK_HEADER + 1) {
            //  don't even attempt compression below a certain input size
            return;
        }

        context.blockCompressionState.enforceMaxDistance(inputAddress + inputSize, 1 << parameters.getWindowLog());
        context.sequenceStore.reset();

        int lastLiteralsSize = parameters.getStrategy()
                .getCompressor()
                .compressBlock(inputBase, inputAddress, inputSize, context.sequenceStore, context.blockCompressionState, context.offsets, parameters);

        long lastLiteralsAddress = inputAddress + inputSize - lastLiteralsSize;

        // append [lastLiteralsAddress .. lastLiteralsSize] to sequenceStore literals buffer
        context.sequenceStore.appendLiterals(inputBase, lastLiteralsAddress, lastLiteralsSize);

        // convert length/offsets into codes
        context.sequenceStore.generateCodes();

        long output = out.getOffs();

        int compressedLiteralsSize = encodeLiterals(
                context.huffmanContext,
                parameters,
                out,
                context.sequenceStore.literalsBuffer,
                context.sequenceStore.literalsLength);
        output += compressedLiteralsSize;

        out.setOffs(output);
        int compressedSequencesSize = SequenceEncoderNew.compressSequences(out, context.sequenceStore, parameters.getStrategy(), context.sequenceEncodingContext);

        int compressedSize = compressedLiteralsSize + compressedSequencesSize;
        if (compressedSize == 0) {
            // not compressible
            return;
        }

        // Check compressibility
        int maxCompressedSize = inputSize - calculateMinimumGain(inputSize, parameters.getStrategy());
        if (compressedSize > maxCompressedSize) {
            return; // not compressed
        }

        // confirm repeated offsets and entropy tables
        context.commit();
    }

    private static int encodeLiterals(
            HuffmanCompressionContextNew context,
            CompressionParameters parameters,
            Foo out,
            byte[] literals,
            int literalsSize)
    {
        final long outputAddress = out.getOffs();
        // TODO: move this to Strategy
        boolean bypassCompression = (parameters.getStrategy() == CompressionParameters.Strategy.FAST) && (parameters.getTargetLength() > 0);
        if (bypassCompression || literalsSize <= MINIMUM_LITERALS_SIZE) {
            long offs = out.getOffs();
            rawLiterals(out, literals, ARRAY_BYTE_BASE_OFFSET, literalsSize);
            return (int)(out.getOffs() - offs);
        }

        int headerSize = 3 + (literalsSize >= 1024 ? 1 : 0) + (literalsSize >= 16384 ? 1 : 0);

        int[] counts = new int[MAX_SYMBOL_COUNT]; // TODO: preallocate
        Histogram.count(literals, literalsSize, counts);
        int maxSymbol = Histogram.findMaxSymbol(counts, MAX_SYMBOL);
        int largestCount = Histogram.findLargestCount(counts, maxSymbol);

        long literalsAddress = ARRAY_BYTE_BASE_OFFSET;
        long offs = out.getOffs();

        if (largestCount == literalsSize) {
            // all bytes in input are equal
            rleLiterals(out, literals, ARRAY_BYTE_BASE_OFFSET, literalsSize);
            return (int)(out.getOffs() - offs);
        }
        else if (largestCount <= (literalsSize >>> 7) + 4) {
            // heuristic: probably not compressible enough
            rawLiterals(out, literals, ARRAY_BYTE_BASE_OFFSET, literalsSize);
            return (int)(out.getOffs() - offs);
        }

        HuffmanCompressionTableNew previousTable = context.getPreviousTable();
        HuffmanCompressionTableNew table;
        int serializedTableSize;
        boolean reuseTable;

        boolean canReuse = previousTable.isValid(counts, maxSymbol);

        // heuristic: use existing table for small inputs if valid
        // TODO: move to Strategy
        boolean preferReuse = parameters.getStrategy().ordinal() < CompressionParameters.Strategy.LAZY.ordinal() && literalsSize <= 1024;
        if (preferReuse && canReuse) {
            table = previousTable;
            reuseTable = true;
            serializedTableSize = 0;
        }
        else {
            HuffmanCompressionTableNew newTable = context.borrowTemporaryTable();

            newTable.initialize(
                    counts,
                    maxSymbol,
                    HuffmanCompressionTable.optimalNumberOfBits(MAX_HUFFMAN_TABLE_LOG, literalsSize, maxSymbol),
                    context.getCompressionTableWorkspace());

            out.setOffs(outputAddress + headerSize);
            Foo outTmp = new Foo(1_000_000);
            serializedTableSize = newTable.write(out, context.getTableWriterWorkspace());

            // Check if using previous huffman table is beneficial
            if (canReuse && previousTable.estimateCompressedSize(counts, maxSymbol) <= serializedTableSize + newTable.estimateCompressedSize(counts, maxSymbol)) {
                table = previousTable;
                reuseTable = true;
                serializedTableSize = 0;
                context.discardTemporaryTable();
            }
            else {
                table = newTable;
                reuseTable = false;
            }
        }

        int compressedSize;
        boolean singleStream = literalsSize < 256;
        offs = out.getOffs();

        if (singleStream) {
            HuffmanCompressorNew.compressSingleStream(out, literals, literalsAddress, literalsSize, table);
        }
        else {
            HuffmanCompressorNew.compress4streams(out, literals, literalsAddress, literalsSize, table);
        }
        compressedSize = (int)(out.getOffs() - offs);
        int totalSize = serializedTableSize + compressedSize;
        int minimumGain = calculateMinimumGain(literalsSize, parameters.getStrategy());

        out.setOffs(outputAddress);

        if (compressedSize == 0 || totalSize >= literalsSize - minimumGain) {
            // incompressible or no savings

            // discard any temporary table we might have borrowed above
            context.discardTemporaryTable();
            offs = out.getOffs();
            rawLiterals(out, literals, ARRAY_BYTE_BASE_OFFSET, literalsSize);
            return (int)(out.getOffs() - offs);
        }

        int encodingType = reuseTable ? TREELESS_LITERALS_BLOCK : COMPRESSED_LITERALS_BLOCK;

        // Build header
        switch (headerSize) {
            case 3: { // 2 - 2 - 10 - 10
                int header = encodingType | ((singleStream ? 0 : 1) << 2) | (literalsSize << 4) | (totalSize << 14);
                UtilNew.put24BitLittleEndian(out, header);
                break;
            }
            case 4: { // 2 - 2 - 14 - 14
                int header = encodingType | (2 << 2) | (literalsSize << 4) | (totalSize << 18);
                out.putInt(header);
                break;
            }
            case 5: { // 2 - 2 - 18 - 18
                int header = encodingType | (3 << 2) | (literalsSize << 4) | (totalSize << 22);
                out.putInt(header);
                out.putByte((byte) (totalSize >>> 10));
                break;
            }
            default:  // not possible : headerSize is {3,4,5}
                throw new IllegalStateException();
        }

        return headerSize + totalSize;
    }

    private static void rleLiterals(Foo out,  Object inputBase, long inputAddress, int inputSize)
    {
        int headerSize = 1 + (inputSize > 31 ? 1 : 0) + (inputSize > 4095 ? 1 : 0);

        switch (headerSize) {
            case 1: // 2 - 1 - 5
                out.putByte((byte) (RLE_LITERALS_BLOCK | (inputSize << 3)));
                break;
            case 2: // 2 - 2 - 12
                out.putShort((short) (RLE_LITERALS_BLOCK | (1 << 2) | (inputSize << 4)));
                break;
            case 3: // 2 - 2 - 20
                out.putInt(RLE_LITERALS_BLOCK | 3 << 2 | inputSize << 4);
                break;
            default:   // impossible. headerSize is {1,2,3}
                throw new IllegalStateException();
        }

        out.putByte(UNSAFE.getByte(inputBase, inputAddress));
    }

    private static int calculateMinimumGain(int inputSize, CompressionParameters.Strategy strategy)
    {
        // TODO: move this to Strategy to avoid hardcoding a specific strategy here
        int minLog = strategy == CompressionParameters.Strategy.BTULTRA ? 7 : 6;
        return (inputSize >>> minLog) + 2;
    }

    private static void rawLiterals(Foo out, Object inputBase, long inputAddress, int inputSize)
    {
        int headerSize = 1;
        if (inputSize >= 32) {
            headerSize++;
        }
        if (inputSize >= 4096) {
            headerSize++;
        }

        switch (headerSize) {
            case 1:
                out.putByte((byte) (RAW_LITERALS_BLOCK | (inputSize << 3)));
                break;
            case 2:
                out.putShort((short) (RAW_LITERALS_BLOCK | (1 << 2) | (inputSize << 4)));
                break;
            case 3:
                UtilNew.put24BitLittleEndian(out, RAW_LITERALS_BLOCK | (3 << 2) | (inputSize << 4));
                break;
            default:
                throw new AssertionError();
        }

        out.copyMemory(inputBase, inputAddress, inputSize);
    }
}
