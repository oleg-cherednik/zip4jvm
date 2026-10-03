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

            // offs is right before the loaded window, i.e. on the next byte to load
            if (bd.getOffs() < 0) {
                done = true;
                return;
            }

            // shift in a new byte for each fully consumed one, until the stream start is reached
            int bytes = bitsConsumed >>> 3; // divide by 8

            for (; bytes > 0 && bd.getOffs() >= 0; bytes--) {
                bits = (bits << 8) | bd.getByte();
                bitsConsumed -= 8;
            }

            done = bytes > 0;
        }
    }

}
