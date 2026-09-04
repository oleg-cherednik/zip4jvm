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
package io.airlift.compress.zstd;

import io.airlift.compress.zstd.seq.SequenceStore;

import static io.airlift.compress.zstd.Constants.SIZE_OF_INT;
import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;

class DoubleFastBlockCompressor implements BlockCompressor {

    private static final int MIN_MATCH = 3;
    private static final int SEARCH_STRENGTH = 8;
    private static final int REP_MOVE = Constants.REPEATED_OFFSET_COUNT - 1;

    @Override
    public int compressBlock(ByteArrayWithOffs in,
                             final int inStartOffs,
                             int inputSize,
                             SequenceStore output,
                             BlockCompressionState state,
                             RepeatedOffsets offsets,
                             CompressionParameters parameters) {
        int matchSearchLength = Math.max(parameters.getSearchLength(), 4);

        // Offsets in hash tables are relative to baseAddress. Hash tables can be reused across calls to compressBlock as long as
        // baseAddress is kept constant.
        // We don't want to generate sequences that point before the current window limit, so we "filter" out all results from looking up in the hash tables
        // beyond that point.
        final long windowBaseAddress = state.getWindowBaseOffset();

        int[] longHashTable = state.hashTable;
        int longHashBits = parameters.getHashLog();

        int[] shortHashTable = state.chainTable;
        int shortHashBits = parameters.getChainLog();

        final int inputEnd = inStartOffs + inputSize;
        final long inputLimit = inputEnd - SIZE_OF_LONG; // We read a long at a time for computing the hashes

        int inOffs = inStartOffs;
        int anchor = inStartOffs;

        int offset1 = offsets.getOffset0();
        int offset2 = offsets.getOffset1();

        int savedOffset = 0;

        if (inOffs - windowBaseAddress == 0) {
            inOffs++;
        }
        int maxRep = (int) (inOffs - windowBaseAddress);

        if (offset2 > maxRep) {
            savedOffset = offset2;
            offset2 = 0;
        }

        if (offset1 > maxRep) {
            savedOffset = offset1;
            offset1 = 0;
        }

        while (inOffs < inputLimit) {   // < instead of <=, because repcode check at (inOffs+1)
            int shortHash = hash(in, inOffs, shortHashBits, matchSearchLength);
            int shortMatchAddress = shortHashTable[shortHash];

            int longHash = hash8(in.getLong(inOffs), longHashBits);
            int longMatchAddress = longHashTable[longHash];

            // update hash tables
            int current = inOffs;
            longHashTable[longHash] = current;
            shortHashTable[shortHash] = current;

            int matchLength;
            int offset;

            if (offset1 > 0 && in.getInt(inOffs + 1 - offset1) == in.getInt(inOffs + 1)) {
                // found a repeated sequence of at least 4 bytes, separated by offset1
                matchLength = count(in, inOffs + 1 + SIZE_OF_INT, inputEnd, inOffs + 1 + SIZE_OF_INT - offset1) +
                        SIZE_OF_INT;
                inOffs++;
                output.storeSequence(in, anchor, inOffs - anchor, 0, matchLength - MIN_MATCH);
            } else {
                // check prefix long match
                if (longMatchAddress > windowBaseAddress && in.getLong(longMatchAddress) == in.getLong(inOffs)) {
                    matchLength = count(in, inOffs + SIZE_OF_LONG, inputEnd, longMatchAddress + SIZE_OF_LONG) +
                            SIZE_OF_LONG;
                    offset = inOffs - longMatchAddress;
                    while (inOffs > anchor && longMatchAddress > windowBaseAddress && in.getByte(inOffs - 1) ==
                            in.getByte(longMatchAddress - 1)) {
                        inOffs--;
                        longMatchAddress--;
                        matchLength++;
                    }
                } else {
                    // check prefix short match
                    if (shortMatchAddress > windowBaseAddress && in.getInt(shortMatchAddress) == in.getInt(inOffs)) {
                        int nextOffsetHash = hash8(in.getLong(inOffs + 1), longHashBits);
                        int nextOffsetMatchAddress = longHashTable[nextOffsetHash];
                        longHashTable[nextOffsetHash] = current + 1;

                        // check prefix long +1 match
                        if (nextOffsetMatchAddress > windowBaseAddress &&
                                in.getLong(nextOffsetMatchAddress) == in.getLong(inOffs + 1)) {
                            matchLength = count(in,
                                                inOffs + 1 + SIZE_OF_LONG,
                                                inputEnd,
                                                nextOffsetMatchAddress + SIZE_OF_LONG) + SIZE_OF_LONG;
                            inOffs++;
                            offset = (int) (inOffs - nextOffsetMatchAddress);
                            while (inOffs > anchor && nextOffsetMatchAddress > windowBaseAddress
                                    && in.getByte(inOffs - 1) == in.getByte(nextOffsetMatchAddress - 1)) {
                                inOffs--;
                                nextOffsetMatchAddress--;
                                matchLength++;
                            }
                        } else {
                            // if no long +1 match, explore the short match we found
                            matchLength = count(in,
                                                inOffs + SIZE_OF_INT,
                                                inputEnd,
                                                shortMatchAddress + SIZE_OF_INT) + SIZE_OF_INT;
                            offset = inOffs - shortMatchAddress;
                            while (inOffs > anchor && shortMatchAddress > windowBaseAddress
                                    && in.getByte(inOffs - 1) == in.getByte(shortMatchAddress - 1)) {
                                inOffs--;
                                shortMatchAddress--;
                                matchLength++;
                            }
                        }
                    } else {
                        inOffs += (int) (((inOffs - anchor) >> SEARCH_STRENGTH) + 1);
                        continue;
                    }
                }

                offset2 = offset1;
                offset1 = offset;

                output.storeSequence(in,
                                     anchor,
                                     (int) (inOffs - anchor),
                                     offset + REP_MOVE,
                                     matchLength - MIN_MATCH);
            }

            inOffs += matchLength;
            anchor = inOffs;

            if (inOffs <= inputLimit) {
                // Fill Table
                longHashTable[hash8(in.getLong(current + 2), longHashBits)] = current + 2;
                shortHashTable[hash(in, current + 2, shortHashBits, matchSearchLength)] = current + 2;

                longHashTable[hash8(in.getLong(inOffs - 2), longHashBits)] = inOffs - 2;
                shortHashTable[hash(in, inOffs - 2, shortHashBits, matchSearchLength)] = inOffs - 2;

                while (inOffs <= inputLimit && offset2 > 0 && in.getInt(inOffs) == in.getInt(inOffs - offset2)) {
                    int repetitionLength = count(in,
                                                 inOffs + SIZE_OF_INT,
                                                 inputEnd,
                                                 inOffs + SIZE_OF_INT - offset2) + SIZE_OF_INT;

                    // swap offset2 <=> offset1
                    int temp = offset2;
                    offset2 = offset1;
                    offset1 = temp;

                    shortHashTable[hash(in, inOffs, shortHashBits, matchSearchLength)] = inOffs;
                    longHashTable[hash8(in.getLong(inOffs), longHashBits)] = inOffs;

                    output.storeSequence(in, anchor, 0, 0, repetitionLength - MIN_MATCH);

                    inOffs += repetitionLength;
                    anchor = inOffs;
                }
            }
        }

        // save reps for next block
        offsets.saveOffset0(offset1 != 0 ? offset1 : savedOffset);
        offsets.saveOffset1(offset2 != 0 ? offset2 : savedOffset);

        // return the last literals size
        return (int) (inputEnd - anchor);
    }

    // TODO: same as LZ4RawCompressor.count

    /**
     * matchAddress must be < inOffs
     */
    public static int count(ByteArrayWithOffs in,
                            final int inOffs,
                            final int inputLimit,
                            final int matchAddress) {
        int input = inOffs;
        int match = matchAddress;
        int remaining = inputLimit - inOffs;

        // first, compare long at a time
        int count = 0;
        while (count < remaining - (SIZE_OF_LONG - 1)) {
            long diff = in.getLong(match) ^ in.getLong(input);
            if (diff != 0) {
                return count + (Long.numberOfTrailingZeros(diff) >> 3);
            }

            count += SIZE_OF_LONG;
            input += SIZE_OF_LONG;
            match += SIZE_OF_LONG;
        }

        while (count < remaining && in.getByte(match) == in.getByte(input)) {
            count++;
            input++;
            match++;
        }

        return count;
    }

    private static int hash(ByteArrayWithOffs in, int offs, int bits, int matchSearchLength) {
        switch (matchSearchLength) {
            case 8:
                return hash8(in.getLong(offs), bits);
            case 7:
                return hash7(in.getLong(offs), bits);
            case 6:
                return hash6(in.getLong(offs), bits);
            case 5:
                return hash5(in.getLong(offs), bits);
            default:
                return hash4(in.getInt(offs), bits);
        }
    }

    private static final int PRIME_4_BYTES = 0x9E3779B1;
    private static final long PRIME_5_BYTES = 0xCF1BBCDCBBL;
    private static final long PRIME_6_BYTES = 0xCF1BBCDCBF9BL;
    private static final long PRIME_7_BYTES = 0xCF1BBCDCBFA563L;
    private static final long PRIME_8_BYTES = 0xCF1BBCDCB7A56463L;

    private static int hash4(int value, int bits) {
        return (value * PRIME_4_BYTES) >>> (Integer.SIZE - bits);
    }

    private static int hash5(long value, int bits) {
        return (int) (((value << (Long.SIZE - 40)) * PRIME_5_BYTES) >>> (Long.SIZE - bits));
    }

    private static int hash6(long value, int bits) {
        return (int) (((value << (Long.SIZE - 48)) * PRIME_6_BYTES) >>> (Long.SIZE - bits));
    }

    private static int hash7(long value, int bits) {
        return (int) (((value << (Long.SIZE - 56)) * PRIME_7_BYTES) >>> (Long.SIZE - bits));
    }

    private static int hash8(long value, int bits) {
        return (int) ((value * PRIME_8_BYTES) >>> (Long.SIZE - bits));
    }
}
