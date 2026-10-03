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
import lombok.Getter;

import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;

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

    @Getter
    public static final class Loader {

        private final BackwardDecorator bd;
        private final int inOffs;
        private long bits;
        private int bitsConsumed;
        private boolean overflow;
        private boolean done;

        public Loader(BackwardDecorator bd, long bits, int bitsConsumed) {
            this.bd = bd;
            inOffs = bd.getFromOffs();
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

            // getLong() leaves offs right before the start of the loaded window
            int offs = bd.getOffs();

            if (offs < 0) {
                done = true;
                return;
            }

            int bytes = bitsConsumed >>> 3; // divide by 8

            if (offs >= SIZE_OF_LONG - 1) {
                if (bytes > 0) {
                    bd.incOffs(SIZE_OF_LONG - bytes);
                    bits = bd.getLong();
                }
                bitsConsumed &= 0b111;
                done = false;
                return;
            }

            if (offs < bytes - 1) {
                // less than bytes is left before the window: shift it down to the stream start
                bytes = offs + 1;
                bd.incOffs(SIZE_OF_LONG - bytes);
                bitsConsumed -= bytes * SIZE_OF_LONG;
                bits = bd.getLong();
                done = true;
                return;
            }

            bd.incOffs(SIZE_OF_LONG - bytes);
            bits = bd.getLong();
            bitsConsumed -= bytes * SIZE_OF_LONG;
            done = false;
        }
    }

}
