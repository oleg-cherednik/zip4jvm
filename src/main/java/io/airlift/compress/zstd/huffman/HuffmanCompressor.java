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
package io.airlift.compress.zstd.huffman;

import io.airlift.compress.zstd.BackwardDecorator;
import io.airlift.compress.zstd.BitOutputStream;
import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.InputStreamForRead;

import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;
import static io.airlift.compress.zstd.Constants.SIZE_OF_SHORT;

public class HuffmanCompressor {

    private HuffmanCompressor() {
    }

    public static int compress4streams(ByteArrayWithOffs out,
                                       int outputSize,
                                       InputStreamForRead in,
                                       int inOffs,
                                       int inputSize,
                                       HuffmanCompressionTable table) {
        int outOffs = out.getOffs();
        int input = inOffs;
        long inputLimit = inOffs + inputSize;
        int output = outOffs;
        int outputLimit = outOffs + outputSize;

        int segmentSize = (inputSize + 3) / 4;

        if (outputSize < 6 /* jump table */ + 1 /* first stream */ + 1 /* second stream */ + 1 /* third stream */ +
                8 /* 8 bytes minimum needed by the bitstream encoder */) {
            return 0; // minimum space to compress successfully
        }

        if (inputSize <= 6 + 1 + 1 + 1) { // jump table + one byte per stream
            return 0;  // no saving possible: input too small
        }

        output += SIZE_OF_SHORT + SIZE_OF_SHORT + SIZE_OF_SHORT; // jump table

        int compressedSize;

        // first segment
        out.setOffs(output);
        compressedSize = compressSingleStream(out, outputLimit - output,
                                              in, input, segmentSize,
                                              table);
        if (compressedSize == 0) {
            return 0;
        }
        out.putShort(outOffs, (short) compressedSize);
        output += compressedSize;
        input += segmentSize;

        // second segment
        out.setOffs(output);
        compressedSize = compressSingleStream(out, outputLimit - output,
                                              in, input, segmentSize,
                                              table);
        if (compressedSize == 0) {
            return 0;
        }
        out.putShort(outOffs + SIZE_OF_SHORT, (short) compressedSize);
        output += compressedSize;
        input += segmentSize;

        // third segment
        out.setOffs(output);
        compressedSize = compressSingleStream(out, outputLimit - output,
                                              in, input, segmentSize,
                                              table);
        if (compressedSize == 0) {
            return 0;
        }
        out.putShort(outOffs + SIZE_OF_SHORT + SIZE_OF_SHORT, (short) compressedSize);
        output += compressedSize;
        input += segmentSize;

        // fourth segment
        out.setOffs(output);
        compressedSize = compressSingleStream(out, outputLimit - output,
                                              in, input, (int) (inputLimit - input),
                                              table);
        if (compressedSize == 0) {
            return 0;
        }
        output += compressedSize;

        return output - outOffs;
    }

    public static int compressSingleStream(ByteArrayWithOffs out, int outputSize,
                                           InputStreamForRead in, int inOffs, int inputSize,
                                           HuffmanCompressionTable table) {
        if (outputSize < SIZE_OF_LONG)
            return 0;

        BitOutputStream bos = new BitOutputStream(out, outputSize);

        // symbols are encoded from the last to the first one (the decoder reads the bos backward),
        // so the input is read with a backward cursor that starts at its last byte
        BackwardDecorator bd = new BackwardDecorator(in, inputSize);

        int n = inputSize & ~3; // join to mod 4

        switch (inputSize & 3) {
            case 3:
                table.encodeSymbol(bos, bd.getByte());
                if (SIZE_OF_LONG * 8 < Huffman.MAX_TABLE_LOG * 4 + 7) {
                    bos.flush();
                }
                // fall-through
            case 2:
                table.encodeSymbol(bos, bd.getByte());
                if (SIZE_OF_LONG * 8 < Huffman.MAX_TABLE_LOG * 2 + 7) {
                    bos.flush();
                }
                // fall-through
            case 1:
                table.encodeSymbol(bos, bd.getByte());
                bos.flush();
                // fall-through
            case 0: /* fall-through */
            default:
                break;
        }

        for (; n > 0; n -= 4) {  // note: n & 3 == 0 at this stage
            table.encodeSymbol(bos, bd.getByte());
            if (SIZE_OF_LONG * 8 < Huffman.MAX_TABLE_LOG * 2 + 7) {
                bos.flush();
            }
            table.encodeSymbol(bos, bd.getByte());
            if (SIZE_OF_LONG * 8 < Huffman.MAX_TABLE_LOG * 4 + 7) {
                bos.flush();
            }
            table.encodeSymbol(bos, bd.getByte());
            if (SIZE_OF_LONG * 8 < Huffman.MAX_TABLE_LOG * 2 + 7) {
                bos.flush();
            }
            table.encodeSymbol(bos, bd.getByte());
            bos.flush();
        }

        return bos.close();
    }
}
