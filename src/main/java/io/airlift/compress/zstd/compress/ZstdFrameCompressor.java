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

import io.airlift.compress.zstd.CompressIn;
import io.airlift.compress.zstd.Out;
import io.airlift.compress.zstd.io.ByteArrayRandomAccess;
import io.airlift.compress.zstd.io.ByteArrayRandomAccessIn;
import io.airlift.compress.zstd.io.RandomAccess;
import io.airlift.compress.zstd.model.BlockHeader;
import io.airlift.compress.zstd.model.FrameHeader;
import io.airlift.compress.zstd.seq.SequenceEncoder;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import static io.airlift.compress.zstd.model.Constants.MAGIC_NUMBER;
import static io.airlift.compress.zstd.model.Constants.MAX_BLOCK_SIZE;
import static io.airlift.compress.zstd.model.Constants.MIN_BLOCK_SIZE;

@RequiredArgsConstructor
public class ZstdFrameCompressor {

    private static void writeMagic(Out out) {
        out.writeDword(MAGIC_NUMBER);
    }

    private final CompressIn in;
    private final int compressionLevel;

    private static FrameHeader writeFrameHeader(Out out, CompressionParameters parameters, int contentSize) {
        FrameHeader frameHeader = FrameHeader.createWith(parameters.getWindowSize(), contentSize);
        frameHeader.encode(out);
        return frameHeader;
    }

    private static void writeChecksum(Out out, XXHash64 checksum, FrameHeader frameHeader) {
        if (frameHeader.isHasChecksum())
            out.writeDword((int) checksum.getValue());
    }

    public void compress(Out out) throws IOException {
        int contentSize = in.available();
        CompressionParameters parameters = CompressionParameters.compute(compressionLevel, contentSize);

        writeMagic(out);
        FrameHeader frameHeader = writeFrameHeader(out, parameters, contentSize);
        XXHash64 checksum = compressFrame(out, parameters, contentSize);
        writeChecksum(out, checksum, frameHeader);
    }

    private XXHash64 compressFrame(Out out, CompressionParameters parameters, int contentSize) throws IOException {
        int blockSize = Math.min(MAX_BLOCK_SIZE, parameters.getWindowSize());
        int remaining = contentSize;

        CompressionContext context = new CompressionContext(parameters, contentSize);
        BlockHeader blockHeader = new BlockHeader();
        // a block is compressed into this buffer first; it is copied to out only when the block is really compressed
        ByteArrayRandomAccess barao = new ByteArrayRandomAccess(blockSize);
        ByteArrayRandomAccessIn barai = new ByteArrayRandomAccessIn(parameters.getWindowSize(), blockSize);
        XXHash64 checksum = new XXHash64();

        while (remaining > 0) {
            int delta = barai.slide();
            if (delta > 0)
                context.blockCompressionState.rebase(delta);

            int read = barai.fill(in, Math.min(blockSize, remaining));
            if (read <= 0)
                break;

            checksum.update(barai.getBuf(), barai.getBlockStart(), read);

            blockHeader.setLastBlock(read >= remaining);
            barao.reset();
            barao.setLimit(read);

            boolean success = compressBlock(barao, context, barai);

            if (success) {
                blockHeader.setBlockType(BlockHeader.BlockType.COMPRESSED_BLOCK);
                blockHeader.setBlockSize(barao.getOffs());
            } else {
                blockHeader.setBlockType(BlockHeader.BlockType.RAW_BLOCK);
                blockHeader.setBlockSize(read);
            }

            blockHeader.encode(out);

            if (success)
                out.copyMemory(barao.getOutForCopy());
            else {
                barai.setOffs(barai.getBlockStart());
                out.copyMemory(barai, read);
            }

            remaining -= read;
        }

        return checksum;
    }

    private static boolean compressBlock(RandomAccess rao, CompressionContext context, ByteArrayRandomAccessIn raIn)
            throws IOException {
        RandomAccess in1 = new ByteArrayRandomAccess(raIn.getBuf(), raIn.getOffs(), raIn.getLimit());

        if (raIn.getSize() < MIN_BLOCK_SIZE + BlockHeader.HEADER_SIZE + 1)
            //  don't even attempt compression below a certain input size
            return false;

        final int offs = rao.getOffs();

        context.blockCompressionState.enforceMaxDistance(raIn.getLimit(), context.parameters.getWindowSize());
        context.sequenceStore.reset();

        int lastLiteralsSize = context.parameters.getStrategy().getCompressor().compressBlock(in1, context);
        int lastLiteralsAddress = raIn.getLimit() - lastLiteralsSize;

        // append [lastLiteralsAddress .. lastLiteralsSize] to sequenceStore literals buffer
        context.sequenceStore.appendLiterals(raIn.getBuf(), lastLiteralsAddress, lastLiteralsSize);

        // convert length/offsets into codes
        context.sequenceStore.generateCodes();

        boolean success = EncodeLiterals.encodeLiterals(rao, context);

        if (!success)
            return false;

        success = SequenceEncoder.compressSequences(rao, context);

        if (!success)
            return false; // sequences section does not fit into the output

        // Check compressibility
        int maxCompressedSize = in1.getOffs() - context.calculateMinimumGain(in1.getOffs());

        if (rao.getOffs() - offs > maxCompressedSize)
            return false; // not compressed

        // confirm repeated offsets and entropy tables
        context.commit();
        return true;
    }

}
