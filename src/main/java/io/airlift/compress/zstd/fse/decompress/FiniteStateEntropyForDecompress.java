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
package io.airlift.compress.zstd.fse.decompress;

import io.airlift.compress.zstd.BackwardDecorator;
import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.In;
import io.airlift.compress.zstd.bis.BackwardBitInputDecorator;
import io.airlift.compress.zstd.fse.FseConst;

import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;
import static io.airlift.compress.zstd.Util.verify;
import static io.airlift.compress.zstd.bis.BackwardBitInputDecorator.peekBits;
import static io.airlift.compress.zstd.huffman.HuffmanDecompressor.MAX_FSE_TABLE_LOG;

public class FiniteStateEntropyForDecompress {

    private final FiniteStateEntropyForDecompress.Table table =
            new FiniteStateEntropyForDecompress.Table(MAX_FSE_TABLE_LOG);

    public FiniteStateEntropyForDecompress readFseTable(In in, int totalBytes) {
        new FseTableForDecompressReader(table, FseConst.MAX_TABLE_LOG)
                .readFseTable(in, totalBytes, FseConst.MAX_SYMBOL);
        return this;
    }

    public void decompress(BackwardDecorator bbis, ByteArrayWithOffs weights) {
        verify(bbis.getTotalBytes() >= 1, "Bitstream is empty");

        // a stream shorter than SIZE_OF_LONG is read as is; getBitsConsumed() counts the missing bytes as consumed
        long bits = readHead(bbis, Math.min(bbis.getTotalBytes(), SIZE_OF_LONG));
        int bitsConsumed = bbis.getBitsConsumed();

        // initialize first FSE stream
        int state1 = (int) peekBits(bitsConsumed, bits, table.log2Size);
        bitsConsumed = bbis.getBitsConsumed() + table.log2Size;

        BackwardBitInputDecorator bbid1 = new BackwardBitInputDecorator(bbis, bits, bitsConsumed);
        bits = bbid1.getBits();
        bitsConsumed = bbid1.getBitsConsumed();

        // initialize second FSE stream
        int state2 = (int) peekBits(bitsConsumed, bits, table.log2Size);
        bitsConsumed += table.log2Size;

        BackwardBitInputDecorator bbid2 = new BackwardBitInputDecorator(bbis, bits, bitsConsumed);
        bits = bbid2.getBits();
        bitsConsumed = bbid2.getBitsConsumed();

        // decode 4 symbols per loop
        while (true) {
            int numberOfBits;

            weights.putByte(table.symbol[state1]);
            numberOfBits = table.numberOfBits[state1];
            state1 = (int) (table.newState[state1] + peekBits(bitsConsumed, bits, numberOfBits));
            bitsConsumed += numberOfBits;

            weights.putByte(table.symbol[state2]);
            numberOfBits = table.numberOfBits[state2];
            state2 = (int) (table.newState[state2] + peekBits(bitsConsumed, bits, numberOfBits));
            bitsConsumed += numberOfBits;

            weights.putByte(table.symbol[state1]);
            numberOfBits = table.numberOfBits[state1];
            state1 = (int) (table.newState[state1] + peekBits(bitsConsumed, bits, numberOfBits));
            bitsConsumed += numberOfBits;

            weights.putByte(table.symbol[state2]);
            numberOfBits = table.numberOfBits[state2];
            state2 = (int) (table.newState[state2] + peekBits(bitsConsumed, bits, numberOfBits));
            bitsConsumed += numberOfBits;

            BackwardBitInputDecorator bbid3 = new BackwardBitInputDecorator(bbis, bits, bitsConsumed);
            boolean done = bbid3.isDone();
            bitsConsumed = bbid3.getBitsConsumed();
            bits = bbid3.getBits();

            if (done)
                break;
        }

        while (true) {
            weights.putByte(table.symbol[state1]);
            int numberOfBits = table.numberOfBits[state1];
            state1 = (int) (table.newState[state1] + peekBits(bitsConsumed, bits, numberOfBits));
            bitsConsumed += numberOfBits;

            BackwardBitInputDecorator bbid4 = new BackwardBitInputDecorator(bbis, bits, bitsConsumed);
            bitsConsumed = bbid4.getBitsConsumed();
            bits = bbid4.getBits();

            if (bbid4.isOverflow()) {
                weights.putByte(table.symbol[state2]);
                break;
            }

            weights.putByte(table.symbol[state2]);
            int numberOfBits1 = table.numberOfBits[state2];
            state2 = (int) (table.newState[state2] + peekBits(bitsConsumed, bits, numberOfBits1));
            bitsConsumed += numberOfBits1;

            BackwardBitInputDecorator bbid5 = new BackwardBitInputDecorator(bbis, bits, bitsConsumed);
            bitsConsumed = bbid5.getBitsConsumed();
            bits = bbid5.getBits();

            if (bbid5.isOverflow()) {
                weights.putByte(table.symbol[state1]);
                break;
            }
        }
    }

    private static long readHead(BackwardDecorator bbis, int size) {
        long bits = 0;

        for (int i = 0; i < size; i++)
            bits = (bits << 8) | bbis.getByte();

        return bits;
    }

    public static final class Table {

        public int log2Size;
        public final int[] newState;
        public final byte[] symbol;
        public final byte[] numberOfBits;

        public Table(int log2Capacity) {
            int capacity = 1 << log2Capacity;
            newState = new int[capacity];
            symbol = new byte[capacity];
            numberOfBits = new byte[capacity];
        }

        public Table(int log2Size, int[] newState, byte[] symbol, byte[] numberOfBits) {
            int size = 1 << log2Size;

            if (newState.length != size || symbol.length != size || numberOfBits.length != size) {
                throw new IllegalArgumentException("Expected arrays to match provided size");
            }

            this.log2Size = log2Size;
            this.newState = newState;
            this.symbol = symbol;
            this.numberOfBits = numberOfBits;
        }

        public void init(int value) {
            log2Size = 0;
            symbol[0] = (byte) value;
            newState[0] = 0;
            numberOfBits[0] = 0;
        }

    }

}
