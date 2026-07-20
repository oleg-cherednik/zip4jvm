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
import io.airlift.compress.zstd.Huffman;

import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;
import static io.airlift.compress.zstd.UnsafeUtil.UNSAFE;

public class HuffmanCompressorNew
{
    private HuffmanCompressorNew()
    {
    }

    public static void compress4streams(Foo out, Object inputBase, final long inputAddress, int inputSize, HuffmanCompressionTableNew table)
    {
        if (inputSize <= 6 + 1 + 1 + 1) { // jump table + one byte per stream
            return;  // no saving possible: input too small
        }

        long input = inputAddress;
        int segmentSize = (inputSize + 3) / 4;
        int size4 = inputSize - segmentSize * 3;

        Foo out1 = new Foo(1_000_000);
        Foo out2 = new Foo(1_000_000);
        Foo out3 = new Foo(1_000_000);
        Foo out4 = new Foo(1_000_000);

        compressSingleStream(out1, inputBase, input, segmentSize, table);
        if(out1.getSize() == 0) return;
        input += segmentSize;

        compressSingleStream(out2, inputBase, input, segmentSize, table);
        if(out2.getSize() == 0) return;
        input += segmentSize;

        compressSingleStream(out3, inputBase, input, segmentSize, table);
        if(out3.getSize() == 0) return;
        input += segmentSize;

        compressSingleStream(out4, inputBase, input, size4, table);

        if(out4.getSize() > 0) {
            out.putShort((short) out1.getSize());
            out.putShort((short) out2.getSize());
            out.putShort((short) out3.getSize());

            out.copyMemory(out1.getCompressed(), out1.getSize());
            out.copyMemory(out2.getCompressed(), out2.getSize());
            out.copyMemory(out3.getCompressed(), out3.getSize());
            out.copyMemory(out4.getCompressed(), out4.getSize());
        }
    }

    public static void compressSingleStream(Foo out, Object inputBase, long inputAddress, int inputSize, HuffmanCompressionTableNew table)
    {
        BitOutputStreamNew bitstream = new BitOutputStreamNew(out);
        long input = inputAddress;

        int n = inputSize & ~3; // join to mod 4

        switch (inputSize & 3) {
            case 3:
                table.encodeSymbol(bitstream, UNSAFE.getByte(inputBase, input + n + 2) & 0xFF);
                if (SIZE_OF_LONG * 8 < Huffman.MAX_TABLE_LOG * 4 + 7) {
                    bitstream.flush();
                }
                // fall-through
            case 2:
                table.encodeSymbol(bitstream, UNSAFE.getByte(inputBase, input + n + 1) & 0xFF);
                if (SIZE_OF_LONG * 8 < Huffman.MAX_TABLE_LOG * 2 + 7) {
                    bitstream.flush();
                }
                // fall-through
            case 1:
                table.encodeSymbol(bitstream, UNSAFE.getByte(inputBase, input + n + 0) & 0xFF);
                bitstream.flush();
                // fall-through
            case 0: /* fall-through */
            default:
                break;
        }

        for (; n > 0; n -= 4) {  // note: n & 3 == 0 at this stage
            table.encodeSymbol(bitstream, UNSAFE.getByte(inputBase, input + n - 1) & 0xFF);
            if (SIZE_OF_LONG * 8 < Huffman.MAX_TABLE_LOG * 2 + 7) {
                bitstream.flush();
            }
            table.encodeSymbol(bitstream, UNSAFE.getByte(inputBase, input + n - 2) & 0xFF);
            if (SIZE_OF_LONG * 8 < Huffman.MAX_TABLE_LOG * 4 + 7) {
                bitstream.flush();
            }
            table.encodeSymbol(bitstream, UNSAFE.getByte(inputBase, input + n - 3) & 0xFF);
            if (SIZE_OF_LONG * 8 < Huffman.MAX_TABLE_LOG * 2 + 7) {
                bitstream.flush();
            }
            table.encodeSymbol(bitstream, UNSAFE.getByte(inputBase, input + n - 4) & 0xFF);
            bitstream.flush();
        }

        bitstream.close();
    }
}
