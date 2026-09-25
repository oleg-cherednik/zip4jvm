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
package io.airlift.compress.zstd.fse;

import io.airlift.compress.zstd.ByteArrayWithOffs;

import static io.airlift.compress.zstd.Util.highestBit;
import static io.airlift.compress.zstd.Util.verify;
import static io.airlift.compress.zstd.fse.FiniteStateEntropy.MAX_SYMBOL;
import static io.airlift.compress.zstd.fse.FiniteStateEntropy.MAX_TABLE_LOG;
import static io.airlift.compress.zstd.fse.FiniteStateEntropy.MIN_TABLE_LOG;

public class FseTableReader {

    private final short[] nextSymbol = new short[MAX_SYMBOL + 1];
    private final short[] normalizedCounters = new short[MAX_SYMBOL + 1];

    public FiniteStateEntropy.Table readFseTable(FiniteStateEntropy.Table table, ByteArrayWithOffs in, int totalBytes) {
        return readFseTable(table, in, in.getOffs() + totalBytes, MAX_SYMBOL, MAX_TABLE_LOG);
    }

    // 4.1.1. FSE Table Description
    // bytes are read from 'in' sequentially; when the method returns, 'in' points to the first byte behind the table
    // (the bit stream of the table is padded to a whole number of bytes)
    public FiniteStateEntropy.Table readFseTable(FiniteStateEntropy.Table table,
                                                 ByteArrayWithOffs in, int inputLimit,
                                                 int maxSymbol, int maxTableLog) {
        BitReader bits = new BitReader(in, inputLimit - in.getOffs());

        int symbolNumber = 0;
        boolean previousIsZero = false;

        int accuracyLog = bits.read(4) + MIN_TABLE_LOG;
        verify(accuracyLog <= maxTableLog, in.getOffs(), "FSE table size exceeds maximum allowed size");

        int numberOfBits = accuracyLog + 1;
        int remaining = (1 << accuracyLog) + 1;
        int threshold = 1 << accuracyLog;

        while (remaining > 1 && symbolNumber <= maxSymbol) {
            if (previousIsZero) {
                int n0 = symbolNumber;

                // repeat flag: '3' means 3 more zero symbols follow
                while (bits.peek(2) == 3) {
                    n0 += 3;
                    bits.skip(2);
                }

                n0 += bits.read(2);
                verify(n0 <= maxSymbol, in.getOffs(), "Symbol larger than max value");

                while (symbolNumber < n0) {
                    normalizedCounters[symbolNumber++] = 0;
                }
            }

            final short max = (short) (2 * threshold - 1 - remaining);
            short count = (short) bits.peek(numberOfBits - 1);

            if (count < max) {
                bits.skip(numberOfBits - 1);
            } else {
                count = (short) bits.read(numberOfBits);
                if (count >= threshold) {
                    count -= max;
                }
            }

            count--;  // extra accuracy

            remaining -= Math.abs(count);
            normalizedCounters[symbolNumber++] = count;
            previousIsZero = count == 0;

            while (remaining < threshold) {
                numberOfBits--;
                threshold >>>= 1;
            }
        }

        verify(remaining == 1 && !bits.isOverflow(), in.getOffs(), "Input is corrupted");

        maxSymbol = symbolNumber - 1;
        verify(maxSymbol <= MAX_SYMBOL, in.getOffs(), "Max symbol value too large (too many symbols for FSE)");

        // populate decoding table
        int symbolCount = maxSymbol + 1;
        int tableSize = 1 << accuracyLog;
        int highThreshold = tableSize - 1;

        table.log2Size = accuracyLog;

        for (byte symbol = 0; symbol < symbolCount; symbol++) {
            if (normalizedCounters[symbol] == -1) {
                table.symbol[highThreshold--] = symbol;
                nextSymbol[symbol] = 1;
            } else {
                nextSymbol[symbol] = normalizedCounters[symbol];
            }
        }

        int position = FseCompressionTable.spreadSymbols(normalizedCounters,
                                                         maxSymbol,
                                                         tableSize,
                                                         highThreshold,
                                                         table.symbol);

        // position must reach all cells once, otherwise normalizedCounter is incorrect
        verify(position == 0, in.getOffs(), "Input is corrupted");

        for (int i = 0; i < tableSize; i++) {
            byte symbol = table.symbol[i];
            short nextState = nextSymbol[symbol]++;
            table.numberOfBits[i] = (byte) (accuracyLog - highestBit(nextState));
            table.newState[i] = (short) ((nextState << table.numberOfBits[i]) - tableSize);
        }

        return table;
    }

}
