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
package ru.olegcherednik.zip4jvm.io.zstd;

import sun.misc.Unsafe;

public final class UnsafeUtil {

    public static final int ARRAY_BYTE_BASE_OFFSET = Unsafe.ARRAY_BYTE_BASE_OFFSET;

    private UnsafeUtil() {}

    public static int getInt(byte[] inputBase, long inputAddress) {
        byte a = inputBase[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET];
        byte b = inputBase[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 1];
        byte c = inputBase[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 2];
        byte d = inputBase[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 3];
        int res = d & 0xFF;
        res = res << 8 | c & 0xFF;
        res = res << 8 | b & 0xFF;
        return res << 8 | a & 0xFF;
    }

    public static long getLong(byte[] inputBase, long inputAddress) {
        byte a = inputBase[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET];
        byte b = inputBase[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 1];
        byte c = inputBase[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 2];
        byte d = inputBase[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 3];
        byte e = inputBase[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 4];
        byte f = inputBase[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 5];
        byte g = inputBase[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 6];
        byte h = inputBase[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 7];
        long res = h & 0xFF;
        res = res << 8 | g & 0xFF;
        res = res << 8 | f & 0xFF;
        res = res << 8 | e & 0xFF;
        res = res << 8 | d & 0xFF;
        res = res << 8 | c & 0xFF;
        res = res << 8 | b & 0xFF;
        return res << 8 | a & 0xFF;
    }

    public static byte getByte(byte[] inputBase, long inputAddress) {
        return inputBase[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET];
    }

    public static short getShort(byte[] inputBase, long inputAddress) {
        byte a = inputBase[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET];
        byte b = inputBase[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 1];
        long res = b & 0xFF;
        return (short)(res << 8 | a & 0xFF);
    }

    public static void putLong(byte[] outputBase, long outputAddress, long value) {
        outputBase[(int)outputAddress - ARRAY_BYTE_BASE_OFFSET] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress - ARRAY_BYTE_BASE_OFFSET + 1] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress - ARRAY_BYTE_BASE_OFFSET + 2] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress - ARRAY_BYTE_BASE_OFFSET + 3] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress - ARRAY_BYTE_BASE_OFFSET + 4] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress - ARRAY_BYTE_BASE_OFFSET + 5] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress - ARRAY_BYTE_BASE_OFFSET + 6] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress - ARRAY_BYTE_BASE_OFFSET + 7] = (byte)(value & 0xFF);
    }

    public static void putByte(byte[] outputBase, long outputAddress, byte value) {
        outputBase[(int)outputAddress - ARRAY_BYTE_BASE_OFFSET] = value;
    }

    public static void putShort(byte[] outputBase, long outputAddress, short value) {
        outputBase[(int)outputAddress - ARRAY_BYTE_BASE_OFFSET] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress - ARRAY_BYTE_BASE_OFFSET + 1] = (byte)(value & 0xFF);
    }

    public static void putInt(byte[] outputBase, long outputAddress, int value) {
        outputBase[(int)outputAddress - ARRAY_BYTE_BASE_OFFSET] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress - ARRAY_BYTE_BASE_OFFSET + 1] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress - ARRAY_BYTE_BASE_OFFSET + 2] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress - ARRAY_BYTE_BASE_OFFSET + 3] = (byte)(value & 0xFF);
    }

    public static void copyMemory(Object inputBase, long inputAddress, Object outputBase, long offs, long inputSize) {
        byte[] in = (byte[])inputBase;
        byte[] out = (byte[])outputBase;
        System.arraycopy(in, (int)(inputAddress - ARRAY_BYTE_BASE_OFFSET), out, (int)(offs - ARRAY_BYTE_BASE_OFFSET), (int)inputSize);
    }

}
