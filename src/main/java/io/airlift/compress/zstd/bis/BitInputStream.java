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
package io.airlift.compress.zstd.bis;

import io.airlift.compress.zstd.BackwardDecorator;
import io.airlift.compress.zstd.ByteArrayWithOffs;
import lombok.Getter;

import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;
import static io.airlift.compress.zstd.Util.highestBit;
import static io.airlift.compress.zstd.Util.verify;

/**
 * Bit streams are encoded as a byte-aligned little-endian stream. Thus, bits are laid out
 * in the following manner, and the stream is read from right to left.
 * <p>
 * <p>
 * ... [16 17 18 19 20 21 22 23] [8 9 10 11 12 13 14 15] [0 1 2 3 4 5 6 7]
 */
public class BitInputStream {

    private BitInputStream() {
    }

    public static boolean isEndOfStream(long startAddress, long currentAddress, int bitsConsumed) {
        return startAddress == currentAddress && bitsConsumed == Long.SIZE;
    }

    /**
     * @return numberOfBits in the low order bits of a long
     */
    public static long peekBits(int bitsConsumed, long bitContainer, int numberOfBits) {
        return (bitContainer << bitsConsumed) >>> 1 >>> (63 - numberOfBits);
    }

    /**
     * numberOfBits must be > 0
     *
     * @return numberOfBits in the low order bits of a long
     */
    public static long peekBitsFast(int bitsConsumed, long bitContainer, int numberOfBits) {
        return (bitContainer << bitsConsumed) >>> (64 - numberOfBits);
    }

    public static final class Loader {

        private final ByteArrayWithOffs in;
        private final int inOffs;
        @Getter
        private long bits;
        @Getter
        private int curOffs;
        @Getter
        private int bitsConsumed;
        @Getter
        private boolean overflow;

        public Loader(ByteArrayWithOffs in, int inOffs, int curOffs, long bits, int bitsConsumed) {
            this.in = in;
            this.inOffs = inOffs;
            this.bits = bits;
            this.curOffs = curOffs;
            this.bitsConsumed = bitsConsumed;
            load();
        }

        public boolean load() {
            if (bitsConsumed > 64) {
                overflow = true;
                return true;
            }

            if (curOffs == inOffs)
                return true;

            int bytes = bitsConsumed >>> 3; // divide by 8
            if (curOffs >= inOffs + SIZE_OF_LONG) {
                if (bytes > 0) {
                    curOffs -= bytes;
                    bits = in.getLong(curOffs);
                }
                bitsConsumed &= 0b111;
            } else if (curOffs - bytes < inOffs) {
                bytes = curOffs - inOffs;
                curOffs = inOffs;
                bitsConsumed -= bytes * SIZE_OF_LONG;
                bits = in.getLong(inOffs);
                return true;
            } else {
                curOffs -= bytes;
                bitsConsumed -= bytes * SIZE_OF_LONG;
                bits = in.getLong(curOffs);
            }

            return false;
        }
    }

    @Getter
    public static final class LoaderNew {

        private final BackwardDecorator bbis;
        private final int inOffs;
        private long bits;
        private int bitsConsumed;
        private boolean overflow;
        private boolean done;

        public LoaderNew(BackwardDecorator bbis, long bits, int bitsConsumed) {
            this.bbis = bbis;
            inOffs = bbis.getFromOffs();
            this.bits = bits;
            this.bitsConsumed = bitsConsumed;

            load();
        }

        public void load() {
            if (bitsConsumed > 64) {
                overflow = true;
                done = true;
                return;
            }

            if (bbis.getOffs() == 0) {
                done = true;
                return;
            }

            int bytes = bitsConsumed >>> 3; // divide by 8

            if (bbis.getOffs() >= SIZE_OF_LONG) {
                if (bytes > 0) {
                    bbis.decOffs(bytes);
                    bits = bbis.getLong();
                }
                bitsConsumed &= 0b111;
                done = false;
                return;
            }


            if (bbis.getOffs() < bytes) {
                bytes = bbis.getOffs();
                bbis.decOffs(bytes);
                bitsConsumed -= bytes * SIZE_OF_LONG;
                bits = bbis.getLong();
                done = true;
                return;
            }

            bbis.decOffs(bytes);
            bits = bbis.getLong();
            bitsConsumed -= bytes * SIZE_OF_LONG;
            done = false;
        }
    }

}
