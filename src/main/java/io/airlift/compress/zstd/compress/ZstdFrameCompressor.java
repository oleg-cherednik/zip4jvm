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
import io.airlift.compress.zstd.dep.InByteArrayWithOffs;
import io.airlift.compress.zstd.io.Out;
import io.airlift.compress.zstd.io.OutByteArrayWithOffs;
import io.airlift.compress.zstd.io.RandomAccessOut;
import io.airlift.compress.zstd.seq.SequenceEncoder;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Arrays;

import static io.airlift.compress.zstd.Constants.MAGIC_NUMBER;
import static io.airlift.compress.zstd.Constants.MAX_BLOCK_SIZE;
import static io.airlift.compress.zstd.Constants.MIN_BLOCK_SIZE;
import static io.airlift.compress.zstd.Constants.MIN_WINDOW_LOG;

@RequiredArgsConstructor
public class ZstdFrameCompressor {

    private static final int CHECKSUM_FLAG = 0b100;

    private static void writeMagic(Out out) {
        out.putInt(MAGIC_NUMBER);
    }

    private final InByteArrayWithOffs in;
    private final int compressionLevel;

    private void writeFrameHeader(Out out, CompressionParameters parameters) {
        boolean singleSegment = parameters.getWindowSize() >= in.available();
        int frameContentSizeFlag = writeFrameHeaderDescriptor(out, singleSegment);

        if (!singleSegment)
            writeWindowDescriptor(out, parameters.getWindowSize());

        writeFrameContentSize(out, singleSegment, frameContentSizeFlag);
    }

    private int writeFrameHeaderDescriptor(Out out, boolean singleSegment) {
        int frameContentSizeFlag = (in.available() >= 256 ? 1 : 0) + (in.available() >= 65536 + 256 ? 1 : 0);
        int frameHeaderDescriptor = (frameContentSizeFlag << 6) | CHECKSUM_FLAG;

        if (singleSegment)
            frameHeaderDescriptor = BitUtils.setBits(frameHeaderDescriptor, BitUtils.BIT5);

        out.putByte((byte) frameHeaderDescriptor);
        return frameContentSizeFlag;
    }

    private static void writeWindowDescriptor(Out out, int windowSize) {
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

    private void writeFrameContentSize(Out out, boolean singleSegment, int frameContentSizeFlag) {
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

    private void writeChecksum(Out out) {
        // TODO check the flag
        XXHash64 checksum = new XXHash64();
        checksum.update(in.buf);
        out.putInt((int) checksum.getValue());
    }

    public void compress(Out out) throws IOException {
        CompressionParameters parameters = CompressionParameters.compute(compressionLevel, in.available());

        writeMagic(out);
        writeFrameHeader(out, parameters);
        compressFrame(out, parameters);
        writeChecksum(out);
    }

    private int compressFrame(Out out, CompressionParameters parameters) throws IOException {
        final int startOffs = out.getOffs();
        int blockSize = Math.min(MAX_BLOCK_SIZE, parameters.getWindowSize());
        int remaining = in.available();
        int inOffs = 0;

        CompressionContext context = new CompressionContext(parameters, in.available());
        BlockHeader blockHeader = new BlockHeader();
        // a block is compressed into this buffer first; it is copied to out only when the block is really compressed
        OutByteArrayWithOffs blockOut = new OutByteArrayWithOffs(new byte[blockSize]);
        blockOut.id = "block_out";

        while (remaining > 0) {
            blockHeader.setLastBlock(blockSize >= remaining);
            blockSize = Math.min(blockSize, remaining);

            blockOut.setOffs(0);
            blockOut.setLimit(blockSize);
            Arrays.fill(blockOut.buf, (byte) 0);

            in.setOffs(inOffs);
            // TODO it looks not correct: blockSize is not related to input
            in.setSize(blockSize);
            in.setLimit(inOffs + blockSize);
            boolean success = compressBlock(blockOut, context);

            if (success) {
                blockHeader.setBlockType(BlockHeader.BlockType.COMPRESSED_BLOCK);
                blockHeader.setBlockSize(blockOut.getOffs());
            } else {
                blockHeader.setBlockType(BlockHeader.BlockType.RAW_BLOCK);
                blockHeader.setBlockSize(blockSize);
            }

            blockHeader.encode(out);

            if (success)
                out.copyMemory(blockOut, blockHeader.getBlockSize());
            else {
                int offs = in.getOffs();
                in.setOffs(inOffs);
                out.copyMemory(in, blockHeader.getBlockSize());
                in.setOffs(offs);
            }

            inOffs += blockSize;
            remaining -= blockSize;
        }

        return out.getOffs() - startOffs;
    }

    private boolean compressBlock(RandomAccessOut rao, CompressionContext context) throws IOException {
        final int offs = rao.getOffs();

        if (in.getSize() < MIN_BLOCK_SIZE + BlockHeader.HEADER_SIZE + 1) {
            //  don't even attempt compression below a certain input size
            return false;
        }

        context.blockCompressionState.enforceMaxDistance(in.getLimit(), context.parameters.getWindowSize());
        context.sequenceStore.reset();

        int lastLiteralsSize = context.parameters.getStrategy().getCompressor().compressBlock(in, context);
        int lastLiteralsAddress = in.getLimit() - lastLiteralsSize;

        // append [lastLiteralsAddress .. lastLiteralsSize] to sequenceStore literals buffer
        context.sequenceStore.appendLiterals(in.buf, lastLiteralsAddress, lastLiteralsSize);

        // convert length/offsets into codes
        context.sequenceStore.generateCodes();

        boolean success = EncodeLiterals.encodeLiterals(rao, context);

        if (!success)
            return false;

        success = SequenceEncoder.compressSequences(rao, context);

        if (!success)
            return false; // sequences section does not fit into the output

        // Check compressibility
        int maxCompressedSize = in.getSize() - context.calculateMinimumGain(in.getSize());

        if (rao.getOffs() - offs > maxCompressedSize)
            return false; // not compressed

        // confirm repeated offsets and entropy tables
        context.commit();
        return true;
    }

}
