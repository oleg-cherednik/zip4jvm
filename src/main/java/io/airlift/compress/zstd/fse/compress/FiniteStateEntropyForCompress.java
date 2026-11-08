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
package io.airlift.compress.zstd.fse.compress;

import ru.olegcherednik.zip4jvm.utils.BitUtils;

import io.airlift.compress.zstd.OutByteArrayWithOffs;
import io.airlift.compress.zstd.Util;
import io.airlift.compress.zstd.bos.MaskBitOutputStream;
import io.airlift.compress.zstd.fse.FseCompressionTable;
import io.airlift.compress.zstd.fse.FseConst;

import static io.airlift.compress.zstd.Util.checkArgument;

public class FiniteStateEntropyForCompress {

    private static final int[] REST_TO_BEAT = { 0, 473195, 504333, 520860, 550000, 700000, 750000, 830000 };
    private static final short UNASSIGNED = -2;

    public void compress(MaskBitOutputStream bos,
                         byte[] weights, int weightsLength,
                         FseCompressionTable compressionTable) {
        if (weightsLength <= 2)
            return;

        int state1;
        int state2;
        int i = weightsLength;

        if (BitUtils.isBitSet(weightsLength, BitUtils.BIT0)) {
            state1 = compressionTable.begin(weights[--i]);
            state2 = compressionTable.begin(weights[--i]);
            state1 = compressionTable.encode(bos, state1, weights[--i]);
        } else {
            state2 = compressionTable.begin(weights[--i]);
            state1 = compressionTable.begin(weights[--i]);
        }

        // join to mod 4
        weightsLength -= 2;

        if (BitUtils.isBitSet(weightsLength, BitUtils.BIT1)) {
            state2 = compressionTable.encode(bos, state2, weights[--i]);
            state1 = compressionTable.encode(bos, state1, weights[--i]);
        }

        // 2 or 4 encoding per loop
        while (i > 0) {
            state2 = compressionTable.encode(bos, state2, weights[--i]);
            state1 = compressionTable.encode(bos, state1, weights[--i]);

            state2 = compressionTable.encode(bos, state2, weights[--i]);
            state1 = compressionTable.encode(bos, state1, weights[--i]);
        }

        compressionTable.finish(bos, state2);
        compressionTable.finish(bos, state1);
    }

    public int optimalTableLog(int maxTableLog, int inputSize, int maxSymbol) {
        if (inputSize <= 1) {
            throw new IllegalArgumentException(); // not supported. Use RLE instead
        }

        int result = maxTableLog;

        // we may be able to reduce accuracy if input is small
        result = Math.min(result, Util.highestBit(inputSize - 1) - 2);

        // Need a minimum to safely represent all symbol values
        result = Math.max(result, Util.minTableLog(inputSize, maxSymbol));

        result = Math.max(result, FseConst.MIN_TABLE_LOG);
        result = Math.min(result, FseConst.MAX_TABLE_LOG);

        return result;
    }

    public int normalizeCounts(short[] normalizedCounts, int tableLog, int[] counts, int total, int maxSymbol) {
        checkArgument(tableLog >= FseConst.MIN_TABLE_LOG, "Unsupported FSE table size");
        checkArgument(tableLog <= FseConst.MAX_TABLE_LOG, "FSE table size too large");
        checkArgument(tableLog >= Util.minTableLog(total, maxSymbol), "FSE table size too small");

        long scale = 62 - tableLog;
        long step = (1L << 62) / total;
        long vstep = 1L << (scale - 20);

        int stillToDistribute = 1 << tableLog;

        int largest = 0;
        short largestProbability = 0;
        int lowThreshold = total >>> tableLog;

        for (int symbol = 0; symbol <= maxSymbol; symbol++) {
            if (counts[symbol] == total) {
                throw new IllegalArgumentException(); // TODO: should have been RLE-compressed by upper layers
            }
            if (counts[symbol] == 0) {
                normalizedCounts[symbol] = 0;
                continue;
            }
            if (counts[symbol] <= lowThreshold) {
                normalizedCounts[symbol] = -1;
                stillToDistribute--;
            } else {
                short probability = (short) ((counts[symbol] * step) >>> scale);
                if (probability < 8) {
                    long restToBeat = vstep * REST_TO_BEAT[probability];
                    long delta = counts[symbol] * step - (((long) probability) << scale);
                    if (delta > restToBeat) {
                        probability++;
                    }
                }
                if (probability > largestProbability) {
                    largestProbability = probability;
                    largest = symbol;
                }
                normalizedCounts[symbol] = probability;
                stillToDistribute -= probability;
            }
        }

        if (-stillToDistribute >= (normalizedCounts[largest] >>> 1)) {
            // corner case. Need another normalization method
            // TODO size_t const errorCode = FSE_normalizeM2(normalizedCounter, tableLog, count, total, maxSymbolValue);
            normalizeCounts2(normalizedCounts, tableLog, counts, total, maxSymbol);
        } else {
            normalizedCounts[largest] += (short) stillToDistribute;
        }

        return tableLog;
    }

    private static int normalizeCounts2(short[] normalizedCounts,
                                        int tableLog,
                                        int[] counts,
                                        int total,
                                        int maxSymbol) {
        int distributed = 0;

        int lowThreshold = total >>>
                tableLog; // minimum count below which frequency in the normalized table is "too small" (~ < 1)
        int lowOne = (total * 3) >>>
                (tableLog + 1); // 1.5 * lowThreshold. If count in (lowThreshold, lowOne] => assign frequency 1

        for (int i = 0; i <= maxSymbol; i++) {
            if (counts[i] == 0) {
                normalizedCounts[i] = 0;
            } else if (counts[i] <= lowThreshold) {
                normalizedCounts[i] = -1;
                distributed++;
                total -= counts[i];
            } else if (counts[i] <= lowOne) {
                normalizedCounts[i] = 1;
                distributed++;
                total -= counts[i];
            } else {
                normalizedCounts[i] = UNASSIGNED;
            }
        }

        int normalizationFactor = 1 << tableLog;
        int toDistribute = normalizationFactor - distributed;

        if ((total / toDistribute) > lowOne) {
            /* risk of rounding to zero */
            lowOne = ((total * 3) / (toDistribute * 2));
            for (int i = 0; i <= maxSymbol; i++) {
                if ((normalizedCounts[i] == UNASSIGNED) && (counts[i] <= lowOne)) {
                    normalizedCounts[i] = 1;
                    distributed++;
                    total -= counts[i];
                }
            }
            toDistribute = normalizationFactor - distributed;
        }

        if (distributed == maxSymbol + 1) {
            // all values are pretty poor;
            // probably incompressible data (should have already been detected);
            // find max, then give all remaining points to max
            int maxValue = 0;
            int maxCount = 0;
            for (int i = 0; i <= maxSymbol; i++) {
                if (counts[i] > maxCount) {
                    maxValue = i;
                    maxCount = counts[i];
                }
            }
            normalizedCounts[maxValue] += (short) toDistribute;
            return 0;
        }

        if (total == 0) {
            // all of the symbols were low enough for the lowOne or lowThreshold
            for (int i = 0; toDistribute > 0; i = (i + 1) % (maxSymbol + 1)) {
                if (normalizedCounts[i] > 0) {
                    toDistribute--;
                    normalizedCounts[i]++;
                }
            }
            return 0;
        }

        // TODO: simplify/document this code
        long vStepLog = 62 - tableLog;
        long mid = (1L << (vStepLog - 1)) - 1;
        long rStep = (((1L << vStepLog) * toDistribute) + mid) / total;   /* scale on remaining */
        long tmpTotal = mid;
        for (int i = 0; i <= maxSymbol; i++) {
            if (normalizedCounts[i] == UNASSIGNED) {
                long end = tmpTotal + (counts[i] * rStep);
                int sStart = (int) (tmpTotal >>> vStepLog);
                int sEnd = (int) (end >>> vStepLog);
                int weight = sEnd - sStart;

                if (weight < 1) {
                    throw new AssertionError();
                }
                normalizedCounts[i] = (short) weight;
                tmpTotal = end;
            }
        }

        return 0;
    }

    public void writeNormalizedCounts(OutByteArrayWithOffs out,
                                      short[] normalizedCounts,
                                      int maxSymbol,
                                      int tableLog) {
        checkArgument(tableLog <= FseConst.MAX_TABLE_LOG, "FSE table too large");
        checkArgument(tableLog >= FseConst.MIN_TABLE_LOG, "FSE table too small");

        int tableSize = 1 << tableLog;
        int bitCount = 0;

        // encode table size
        int bitStream = tableLog - FseConst.MIN_TABLE_LOG;
        bitCount += 4;

        int remaining = tableSize + 1; // +1 for extra accuracy
        int threshold = tableSize;
        int tableBitCount = tableLog + 1;

        int symbol = 0;

        boolean previousIs0 = false;
        while (remaining > 1) {
            if (previousIs0) {
                // From RFC 8478, section 4.1.1:
                //   When a symbol has a probability of zero, it is followed by a 2-bit
                //   repeat flag.  This repeat flag tells how many probabilities of zeroes
                //   follow the current one.  It provides a number ranging from 0 to 3.
                //   If it is a 3, another 2-bit repeat flag follows, and so on.
                int start = symbol;

                // find run of symbols with count 0
                while (normalizedCounts[symbol] == 0) {
                    symbol++;
                }

                // encode in batches if 8 repeat sequences in one shot (representing 24 symbols total)
                while (symbol >= start + 24) {
                    start += 24;
                    bitStream |= 0b11_11_11_11_11_11_11_11 << bitCount;

                    out.putShort((short) bitStream);

                    // flush now, so no need to increase bitCount by 16
                    bitStream >>>= Short.SIZE;
                }

                // encode remaining in batches of 3 symbols
                while (symbol >= start + 3) {
                    start += 3;
                    bitStream |= 0b11 << bitCount;
                    bitCount += 2;
                }

                // encode tail
                bitStream |= (symbol - start) << bitCount;
                bitCount += 2;

                // flush bitstream if necessary
                if (bitCount > 16) {
                    out.putShort((short) bitStream);
                    bitStream >>>= Short.SIZE;
                    bitCount -= Short.SIZE;
                }
            }

            int count = normalizedCounts[symbol++];
            int max = (2 * threshold - 1) - remaining;
            remaining -= count < 0 ? -count : count;
            count++;   /* +1 for extra accuracy */
            if (count >= threshold) {
                count += max;
            }
            bitStream |= count << bitCount;
            bitCount += tableBitCount;
            bitCount -= count < max ? 1 : 0;
            previousIs0 = count == 1;

            if (remaining < 1) {
                throw new AssertionError();
            }

            while (remaining < threshold) {
                tableBitCount--;
                threshold >>= 1;
            }

            // flush bitstream if necessary
            if (bitCount > 16) {
                out.putShort((short) bitStream);

                bitStream >>>= Short.SIZE;
                bitCount -= Short.SIZE;
            }
        }

        // flush remaining bitstream
        int tailBytes = (bitCount + 7) / 8;
        int tmp = bitStream;

        for (int i = 0; i < tailBytes; i++) {
            out.putByte((byte) (tmp & 0xFF));
            tmp >>>= 8;
        }

        checkArgument(symbol <= maxSymbol + 1, "Error"); // TODO
    }

}
