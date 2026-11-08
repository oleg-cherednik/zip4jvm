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

import io.airlift.compress.zstd.BlockHeader;
import io.airlift.compress.zstd.OutByteArrayWithOffs;
import io.airlift.compress.zstd.dep.InByteArrayWithOffs;
import io.airlift.compress.zstd.seq.SequenceEncoder;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import static io.airlift.compress.zstd.Constants.MAGIC_NUMBER;
import static io.airlift.compress.zstd.Constants.MAX_BLOCK_SIZE;
import static io.airlift.compress.zstd.Constants.MIN_BLOCK_SIZE;
import static io.airlift.compress.zstd.Constants.MIN_WINDOW_LOG;

@RequiredArgsConstructor
public class ZstdFrameCompressor {

    private static final int CHECKSUM_FLAG = 0b100;

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

        CompressionContext context = new CompressionContext(parameters, in.available());
        BlockHeader blockHeader = new BlockHeader();

        do {
            blockHeader.setLastBlock(blockSize >= remaining);
            blockSize = Math.min(blockSize, remaining);

            int compressedSize = 0;

            if (remaining > 0) {
                blockHeader.setBlockType(BlockHeader.TYPE_COMPRESSED_BLOCK);
                out.setOffs(offs + BlockHeader.SIZE);
                compressedSize = compressBlock(inOffs, blockSize,
                                               out, outputSize - BlockHeader.SIZE,
                                               context);
            }

            if (compressedSize == 0) {
                blockHeader.setBlockType(BlockHeader.TYPE_RAW_BLOCK);

                out.setOffs(offs + BlockHeader.SIZE);
                in.copyMemory(inOffs, out.buf, offs + BlockHeader.SIZE, blockSize);

                compressedSize = blockSize;
            }

            blockHeader.setBlockSize(compressedSize);
            compressedSize += BlockHeader.SIZE;

            out.setOffs(offs);
            blockHeader.write(out);

            inOffs += blockSize;
            remaining -= blockSize;
            offs += compressedSize;
            outputSize -= compressedSize;
        }
        while (remaining > 0);

        out.setOffs(offs);

        return offs - startOffs;
    }

    private int compressBlock(int inStartOffs, int inputSize,
                              OutByteArrayWithOffs out, int outputSize,
                              CompressionContext context) throws IOException {
        if (inputSize < MIN_BLOCK_SIZE + BlockHeader.SIZE + 1) {
            //  don't even attempt compression below a certain input size
            return 0;
        }

        int outputAddress = out.getOffs();
        int inputLimit = inStartOffs + inputSize;

        context.blockCompressionState.enforceMaxDistance(inputLimit, context.parameters.getWindowSize());
        context.sequenceStore.reset();

        in.setOffs(inStartOffs);
        in.setSize(inputSize);
        int lastLiteralsSize = context.parameters.getStrategy().getCompressor().compressBlock(in, context);
        int lastLiteralsAddress = inputLimit - lastLiteralsSize;

        // append [lastLiteralsAddress .. lastLiteralsSize] to sequenceStore literals buffer
        context.sequenceStore.appendLiterals(in.buf, lastLiteralsAddress, lastLiteralsSize);

        // convert length/offsets into codes
        context.sequenceStore.generateCodes();

        long outputLimit = outputAddress + outputSize;
        int output = outputAddress;

        out.setOffs(output);
        int compressedLiteralsSize = EncodeLiterals.encodeLiterals(out, (int) (outputLimit - output), context);
        output += compressedLiteralsSize;

        int compressedSequencesSize = SequenceEncoder.compressSequences(out, output, (int) (outputLimit - output),
                                                                        context);

        int compressedSize = compressedLiteralsSize + compressedSequencesSize;
        if (compressedSize == 0) {
            // not compressible
            return 0;
        }

        // Check compressibility
        int maxCompressedSize = inputSize - EncodeLiterals.calculateMinimumGain(inputSize,
                                                                                context.parameters.getStrategy());
        if (compressedSize > maxCompressedSize) {
            return 0; // not compressed
        }

        // confirm repeated offsets and entropy tables
        context.commit();

        return compressedSize;
    }

}
