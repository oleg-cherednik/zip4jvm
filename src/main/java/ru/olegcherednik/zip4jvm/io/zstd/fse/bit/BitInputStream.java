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
package ru.olegcherednik.zip4jvm.io.zstd.fse.bit;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.zstd.Buffer;
import ru.olegcherednik.zip4jvm.io.zstd.UnsafeUtil;
import ru.olegcherednik.zip4jvm.io.zstd.huffman.BitStreamData;

import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_BYTE;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_LONG;
import static ru.olegcherednik.zip4jvm.io.zstd.Util.highestBit;
import static ru.olegcherednik.zip4jvm.io.zstd.Util.verify;

/**
 * Bit streams are encoded as a byte-aligned little-endian stream. Thus, bits are laid out
 * in the following manner, and the stream is read from right to left.
 * <p>
 * <p>
 * ... [16 17 18 19 20 21 22 23] [8 9 10 11 12 13 14 15] [0 1 2 3 4 5 6 7]
 */
public class BitInputStream {

    public static boolean isEndOfStream(long startAddress, long currentAddress, int bitsConsumed) {
        return startAddress == currentAddress && bitsConsumed == Long.SIZE;
    }

    static long readTail(Buffer inputBase, int size) {
        int inputAddress = inputBase.getOffs();
        long bits = inputBase.getByte();

        switch (size) {
            case 7:
                inputBase.seek(inputAddress);
                inputBase.skip(6);
                bits |= (inputBase.getByte() & 0xFFL) << 48;
            case 6:
                inputBase.seek(inputAddress);
                inputBase.skip(5);
                bits |= (inputBase.getByte() & 0xFFL) << 40;
            case 5:
                inputBase.seek(inputAddress);
                inputBase.skip(4);
                bits |= (inputBase.getByte() & 0xFFL) << 32;
            case 4:
                inputBase.seek(inputAddress);
                inputBase.skip(3);
                bits |= (inputBase.getByte() & 0xFFL) << 24;
            case 3:
                inputBase.seek(inputAddress);
                inputBase.skip(2);
                bits |= (inputBase.getByte() & 0xFFL) << 16;
            case 2:
                inputBase.seek(inputAddress);
                inputBase.skip(1);
                bits |= (inputBase.getByte() & 0xFFL) << 8;
        }

        return bits;
    }

    static long readTail(byte[] inputBase, int inputAddress, int inputSize) {
        long bits = UnsafeUtil.getByte(inputBase, inputAddress) & 0xFF;

        switch (inputSize) {
            case 7:
                bits |= (UnsafeUtil.getByte(inputBase, inputAddress + 6) & 0xFFL) << 48;
            case 6:
                bits |= (UnsafeUtil.getByte(inputBase, inputAddress + 5) & 0xFFL) << 40;
            case 5:
                bits |= (UnsafeUtil.getByte(inputBase, inputAddress + 4) & 0xFFL) << 32;
            case 4:
                bits |= (UnsafeUtil.getByte(inputBase, inputAddress + 3) & 0xFFL) << 24;
            case 3:
                bits |= (UnsafeUtil.getByte(inputBase, inputAddress + 2) & 0xFFL) << 16;
            case 2:
                bits |= (UnsafeUtil.getByte(inputBase, inputAddress + 1) & 0xFFL) << 8;
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

    public static class Initializer {

        private final byte[] inputBase;
        private final int startAddress;
        private final int endAddress;
        private long bits;
        private int currentAddress;
        private int bitsConsumed;

        public Initializer(Buffer inputBase, int startAddress, int endAddress) {
            this(inputBase.getBuf(), startAddress, endAddress);
        }

        public Initializer(byte[] inputBase, int startAddress, int endAddress) {
            this.inputBase = inputBase;
            this.startAddress = startAddress;
            this.endAddress = endAddress;
        }

        public long getBits() {
            return bits;
        }

        public int getCurrentAddress() {
            return currentAddress;
        }

        public int getBitsConsumed() {
            return bitsConsumed;
        }

        public void initialize() {
            int lastByte = UnsafeUtil.getByte(inputBase, endAddress - 1) & 0xFF;
            verify(lastByte != 0, endAddress, "Bitstream end mark not present");

            bitsConsumed = SIZE_OF_LONG - highestBit(lastByte);

            int inputSize = endAddress - startAddress;
            if (inputSize >= SIZE_OF_LONG) {  /* normal case */
                currentAddress = endAddress - SIZE_OF_LONG;
                bits = UnsafeUtil.getLong(inputBase, currentAddress);
            } else {
                currentAddress = startAddress;
                bits = readTail(inputBase, startAddress, inputSize);
                bitsConsumed += (SIZE_OF_LONG - inputSize) * 8;
            }
        }

    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class InitializerBuffer {

        private final long bits;
        private final int currentAddress;
        private final int bitsConsumed;

        public static InitializerBuffer read(Buffer inputBase, int size) {
            int startAddress = inputBase.getOffs();
            int endAddress = startAddress + size;

            inputBase.skip(size);
            inputBase.skip(-SIZE_OF_BYTE);
            int lastByte = inputBase.getByte();

            verify(lastByte != 0, endAddress, "Bitstream end mark not present");

            int bitsConsumed = SIZE_OF_LONG - highestBit(lastByte);
            int currentAddress;
            long bits;

            if (size >= SIZE_OF_LONG) {
                inputBase.skip(-SIZE_OF_LONG);
                currentAddress = inputBase.getOffs();
                bits = inputBase.getLong();
            } else {
                inputBase.skip(-size);
                currentAddress = inputBase.getOffs();
                bits = readTail(inputBase, size);
                bitsConsumed += (SIZE_OF_LONG - size) * 8;
            }

            inputBase.seek(endAddress);

            return new InitializerBuffer(bits, currentAddress, bitsConsumed);
        }

    }

    public static final class Loader {

        private final byte[] inputBase;
        private final int startAddress;
        private long bits;
        private int currentAddress;
        private int bitsConsumed;
        private boolean overflow;

        public Loader(byte[] inputBase, BitStreamData stream) {
            this(inputBase, stream.getOffs(), stream.getCurrentAddress(), stream.getBits(), stream.getBitsConsumed());
        }

        public Loader(byte[] inputBase, int startAddress, int currentAddress, long bits, int bitsConsumed) {
            this.inputBase = inputBase;
            this.startAddress = startAddress;
            this.bits = bits;
            this.currentAddress = currentAddress;
            this.bitsConsumed = bitsConsumed;
        }

        public long getBits() {
            return bits;
        }

        public int getCurrentAddress() {
            return currentAddress;
        }

        public int getBitsConsumed() {
            return bitsConsumed;
        }

        public boolean isOverflow() {
            return overflow;
        }

        public boolean load() {
            if (bitsConsumed > 64) {
                overflow = true;
                return true;
            }

            if (currentAddress == startAddress) {
                return true;
            }

            int bytes = bitsConsumed >>> 3; // divide by 8
            if (currentAddress >= startAddress + SIZE_OF_LONG) {
                if (bytes > 0) {
                    currentAddress -= bytes;
                    bits = UnsafeUtil.getLong(inputBase, currentAddress);
                }
                bitsConsumed &= 0b111;
            } else if (currentAddress - bytes < startAddress) {
                bytes = currentAddress - startAddress;
                currentAddress = startAddress;
                bitsConsumed -= bytes * SIZE_OF_LONG;
                bits = UnsafeUtil.getLong(inputBase, startAddress);
                return true;
            } else {
                currentAddress -= bytes;
                bitsConsumed -= bytes * SIZE_OF_LONG;
                bits = UnsafeUtil.getLong(inputBase, currentAddress);
            }

            return false;
        }

    }

    @Getter
    @RequiredArgsConstructor
    public static final class LoaderBuffer {

        private final byte[] inputBase;
        private final BitStreamData stream;
        private boolean overflow;

        public boolean load() {
            if (stream.getCurrentAddress() > 64) {
                overflow = true;
                return true;
            }

            if (stream.getCurrentAddress() == stream.getOffs()) {
                return true;
            }

            int bytes = stream.getBitsConsumed() >>> 3; // divide by 8
            if (stream.getCurrentAddress() >= stream.getOffs() + SIZE_OF_LONG) {
                if (bytes > 0) {
                    stream.setCurrentAddress(stream.getCurrentAddress() - bytes);
                    stream.setBits(UnsafeUtil.getLong(inputBase, stream.getCurrentAddress()));
                }
                stream.setBitsConsumed(stream.getBitsConsumed() & 0b111);
            } else if (stream.getCurrentAddress() - bytes < stream.getOffs()) {
                bytes = stream.getCurrentAddress() - stream.getOffs();
                stream.setCurrentAddress(stream.getOffs());
                stream.setBitsConsumed(stream.getBitsConsumed() - bytes * SIZE_OF_LONG);
                stream.setBits(UnsafeUtil.getLong(inputBase, stream.getOffs()));
                return true;
            } else {
                stream.setCurrentAddress(stream.getCurrentAddress() - bytes);
                stream.setBitsConsumed(stream.getBitsConsumed() - bytes * SIZE_OF_LONG);
                stream.setBits(UnsafeUtil.getLong(inputBase, stream.getCurrentAddress()));
            }

            return false;
        }

    }

}
