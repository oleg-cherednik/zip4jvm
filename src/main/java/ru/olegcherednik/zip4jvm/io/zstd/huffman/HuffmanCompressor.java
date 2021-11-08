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
package ru.olegcherednik.zip4jvm.io.zstd.huffman;

import ru.olegcherednik.zip4jvm.io.zstd.UnsafeUtil;
import ru.olegcherednik.zip4jvm.io.zstd.bit.BitOutputStream;

import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_LONG;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_SHORT;

public class HuffmanCompressor {

    private HuffmanCompressor() {
    }

    public static int compress4streams(byte[] outputBase, int outputAddress, int outputSize, byte[] inputBase, int inputAddress, int inputSize,
            HuffmanCompressionTable table) {
        int input = inputAddress;
        int inputLimit = inputAddress + inputSize;
        int output = outputAddress;
        int outputLimit = outputAddress + outputSize;

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
        compressedSize = compressSingleStream(outputBase, output, outputLimit - output, inputBase, input, segmentSize, table);
        if (compressedSize == 0) {
            return 0;
        }
        UnsafeUtil.putShort(outputBase, outputAddress, (short)compressedSize);
        output += compressedSize;
        input += segmentSize;

        // second segment
        compressedSize = compressSingleStream(outputBase, output, outputLimit - output, inputBase, input, segmentSize, table);
        if (compressedSize == 0) {
            return 0;
        }
        UnsafeUtil.putShort(outputBase, outputAddress + SIZE_OF_SHORT, (short)compressedSize);
        output += compressedSize;
        input += segmentSize;

        // third segment
        compressedSize = compressSingleStream(outputBase, output, (int)(outputLimit - output), inputBase, input, segmentSize, table);
        if (compressedSize == 0) {
            return 0;
        }
        UnsafeUtil.putShort(outputBase, outputAddress + SIZE_OF_SHORT + SIZE_OF_SHORT, (short)compressedSize);
        output += compressedSize;
        input += segmentSize;

        // fourth segment
        compressedSize = compressSingleStream(outputBase, output, (int)(outputLimit - output), inputBase, input, inputLimit - input, table);
        if (compressedSize == 0) {
            return 0;
        }
        output += compressedSize;

        return (int)(output - outputAddress);
    }

    public static int compressSingleStream(byte[] outputBase, int outputAddress, int outputSize, byte[] inputBase, int inputAddress, int inputSize,
            HuffmanCompressionTable table) {
        if (outputSize < SIZE_OF_LONG) {
            return 0;
        }

        BitOutputStream bitstream = new BitOutputStream(outputBase, outputAddress, outputSize);

        int n = inputSize & ~3; // join to mod 4

        switch (inputSize & 3) {
            case 3:
                table.encodeSymbol(bitstream, UnsafeUtil.getByte(inputBase, inputAddress + n + 2) & 0xFF);
                if (SIZE_OF_LONG * 8 < Huffman.MAX_TABLE_LOG * 4 + 7) {
                    bitstream.flush();
                }
                // fall-through
            case 2:
                table.encodeSymbol(bitstream, UnsafeUtil.getByte(inputBase, inputAddress + n + 1) & 0xFF);
                if (SIZE_OF_LONG * 8 < Huffman.MAX_TABLE_LOG * 2 + 7) {
                    bitstream.flush();
                }
                // fall-through
            case 1:
                table.encodeSymbol(bitstream, UnsafeUtil.getByte(inputBase, inputAddress + n + 0) & 0xFF);
                bitstream.flush();
                // fall-through
            case 0: /* fall-through */
            default:
                break;
        }

        for (; n > 0; n -= 4) {  // note: n & 3 == 0 at this stage
            table.encodeSymbol(bitstream, UnsafeUtil.getByte(inputBase, inputAddress + n - 1) & 0xFF);
            if (SIZE_OF_LONG * 8 < Huffman.MAX_TABLE_LOG * 2 + 7) {
                bitstream.flush();
            }
            table.encodeSymbol(bitstream, UnsafeUtil.getByte(inputBase, inputAddress + n - 2) & 0xFF);
            if (SIZE_OF_LONG * 8 < Huffman.MAX_TABLE_LOG * 4 + 7) {
                bitstream.flush();
            }
            table.encodeSymbol(bitstream, UnsafeUtil.getByte(inputBase, inputAddress + n - 3) & 0xFF);
            if (SIZE_OF_LONG * 8 < Huffman.MAX_TABLE_LOG * 2 + 7) {
                bitstream.flush();
            }
            table.encodeSymbol(bitstream, UnsafeUtil.getByte(inputBase, inputAddress + n - 4) & 0xFF);
            bitstream.flush();
        }

        return bitstream.close();
    }

}