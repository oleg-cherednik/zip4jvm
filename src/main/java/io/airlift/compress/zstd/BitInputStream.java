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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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

    static long readTail(ByteArrayWithOffs in, int offs, int inputSize) {
        long bits = in.getByte(offs) & 0xFF;

        switch (inputSize) {
            case 7:
                bits |= (in.getByte(offs + 6) & 0xFFL) << 48;
            case 6:
                bits |= (in.getByte(offs + 5) & 0xFFL) << 40;
            case 5:
                bits |= (in.getByte(offs + 4) & 0xFFL) << 32;
            case 4:
                bits |= (in.getByte(offs + 3) & 0xFFL) << 24;
            case 3:
                bits |= (in.getByte(offs + 2) & 0xFFL) << 16;
            case 2:
                bits |= (in.getByte(offs + 1) & 0xFFL) << 8;
        }

        return bits;
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

    @RequiredArgsConstructor
    public static class Initializer {

        private final ByteArrayWithOffs in;
        private final int inOffs;
        private final int endOffs;
        @Getter
        private long bits;
        @Getter
        private int curOffs;
        @Getter
        private int bitsConsumed;

        public void initialize() {
            verify(endOffs - inOffs >= 1, inOffs, "Bitstream is empty");

            int lastByte = in.getByte(endOffs - 1) & 0xFF;
            verify(lastByte != 0, endOffs, "Bitstream end mark not present");

            bitsConsumed = SIZE_OF_LONG - highestBit(lastByte);

            int inputSize = endOffs - inOffs;
            if (inputSize >= SIZE_OF_LONG) {  /* normal case */
                curOffs = endOffs - SIZE_OF_LONG;
                bits = in.getLong(curOffs);
            } else {
                curOffs = inOffs;
                bits = readTail(in, inOffs, inputSize);

                bitsConsumed += (SIZE_OF_LONG - inputSize) * 8;
            }
        }
    }

    @Getter
    public static class InitializerNew {

        private final BackwardBitInputStream bbis;

        private final int tableLog;
        private final byte[] symbols;
        private final byte[] numbersOfBits;

        @Setter
        private long bits;
        @Setter
        private int bitsConsumed;
        private boolean overflow;

        public InitializerNew(BackwardBitInputStream bbis, int tableLog, byte[] symbols, byte[] numbersOfBits) {
            this.bbis = bbis;
            this.tableLog = tableLog;
            this.symbols = symbols;
            this.numbersOfBits = numbersOfBits;
            init();
        }

        private int getLastByte() {
            return bbis.getLastByte();
        }

        private long getLong() {
            return bbis.getLong();
        }

        private void decOffs(int bytes) {
            bbis.decOffs(bytes);
        }

        public void init() {
            int lastByte = getLastByte();
            verify(lastByte != 0, bbis.getOffs() + bbis.getBuf().length, "Bitstream end mark not present");

            bitsConsumed = SIZE_OF_LONG - highestBit(lastByte);

            if (bbis.getTotalBytes() >= SIZE_OF_LONG) {  /* normal case */
                decOffs(SIZE_OF_LONG - 1);
                bits = getLong();
            } else {
                // a stream shorter than SIZE_OF_LONG is read in one go, starting from its very first byte
                decOffs(bbis.getOffs());
                bits = readTail(bbis.getTotalBytes());
                bitsConsumed += (SIZE_OF_LONG - bbis.getTotalBytes()) * 8;
            }
        }

        public boolean load() {
            if (bitsConsumed > 64) {
                overflow = true;
                return true;
            }

            if (bbis.getOffs() == 0)
                return true;

            int bytes = bitsConsumed >>> 3; // divide by 8

            if (bbis.getOffs() >= SIZE_OF_LONG) {
                if (bytes > 0) {
                    bbis.decOffs(bytes);
                    bits = getLong();
                }
                bitsConsumed &= 0b111;
                return false;
            }

            if (bbis.getOffs() < bytes) {
                bytes = bbis.getOffs();
                bbis.decOffs(bbis.getOffs());
                bitsConsumed -= bytes * SIZE_OF_LONG;
                bits = getLong();
                return true;
            }

            bbis.decOffs(bytes);
            bits = bbis.getLong();
            bitsConsumed -= bytes * SIZE_OF_LONG;
            return false;
        }

        private long readTail(int inputSize) {
            int offs = bbis.getOffs();
            long bits = bbis.getBuf()[offs] & 0xFF;

            switch (inputSize) {
                case 7:
                    bits |= (bbis.getBuf()[offs + 6] & 0xFFL) << 48;
                case 6:
                    bits |= (bbis.getBuf()[offs + 5] & 0xFFL) << 40;
                case 5:
                    bits |= (bbis.getBuf()[offs + 4] & 0xFFL) << 32;
                case 4:
                    bits |= (bbis.getBuf()[offs + 3] & 0xFFL) << 24;
                case 3:
                    bits |= (bbis.getBuf()[offs + 2] & 0xFFL) << 16;
                case 2:
                    bits |= (bbis.getBuf()[offs + 1] & 0xFFL) << 8;
            }

            return bits;
        }

        public void decodeTail(ByteArrayWithOffs in,
                               ByteArrayWithOffs out, int outOffs,
                               final long outputLimit) {
            // closer to the end
            while (outOffs < outputLimit) {
                LoaderNew loader = new LoaderNew(bbis, bits, bitsConsumed);
                bitsConsumed = loader.getBitsConsumed();
                bits = loader.getBits();

                if (loader.isDone())
                    break;

                decodeSymbol(out, outOffs++);
            }

            // not more data in bit stream, so no need to reload
            while (outOffs < outputLimit) {
                decodeSymbol(out, outOffs++);
            }

            verify(isEndOfStream(0, bbis.getOffs(), bitsConsumed),
                   bbis.getInOffs(), "Bit stream is not fully consumed");
        }

        public void decodeTail1(ByteArrayWithOffs in,
                                ByteArrayWithOffs out, int outOffs,
                                final long outputLimit) {
            // closer to the end
            while (outOffs < outputLimit) {
                LoaderNew loader = new LoaderNew(bbis, bits, bitsConsumed);
                boolean done = loader.isDone();
                bitsConsumed = loader.getBitsConsumed();
                bits = loader.getBits();

                if (done) {
                    break;
                }

                decodeSymbol(out, outOffs++);
            }

            // not more data in bit stream, so no need to reload
            while (outOffs < outputLimit) {
                decodeSymbol(out, outOffs++);
            }

            verify(isEndOfStream(0, bbis.getOffs(), bitsConsumed),
                   bbis.getInOffs(), "Bit stream is not fully consumed");
        }

        public void decodeSymbol(ByteArrayWithOffs out, int offs) {
            int value = (int) peekBitsFast(bitsConsumed, bits, tableLog);
            out.putByte(offs, symbols[value]);
            bitsConsumed += numbersOfBits[value];
        }

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

        private final BackwardBitInputStream bbis;
        private final int inOffs;
        private long bits;
        private int bitsConsumed;
        private boolean overflow;
        private boolean done;

        public LoaderNew(BackwardBitInputStream bbis, long bits, int bitsConsumed) {
            this.bbis = bbis;
            inOffs = bbis.getInOffs();
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
