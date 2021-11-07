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

public final class UnsafeUtil {

    public static int getInt(byte[] inputBase, int offs) {
        byte a = inputBase[offs];
        byte b = inputBase[offs + 1];
        byte c = inputBase[offs + 2];
        byte d = inputBase[offs + 3];
        int res = d & 0xFF;
        res = res << 8 | c & 0xFF;
        res = res << 8 | b & 0xFF;
        return res << 8 | a & 0xFF;
    }

    public static long getLong(byte[] inputBase, int offs) {
        byte a = inputBase[offs];
        byte b = inputBase[offs + 1];
        byte c = inputBase[offs + 2];
        byte d = inputBase[offs + 3];
        byte e = inputBase[offs + 4];
        byte f = inputBase[offs + 5];
        byte g = inputBase[offs + 6];
        byte h = inputBase[offs + 7];
        long res = h & 0xFF;
        res = res << 8 | g & 0xFF;
        res = res << 8 | f & 0xFF;
        res = res << 8 | e & 0xFF;
        res = res << 8 | d & 0xFF;
        res = res << 8 | c & 0xFF;
        res = res << 8 | b & 0xFF;
        return res << 8 | a & 0xFF;
    }

    public static byte getByte(byte[] inputBase, int offs) {
        return inputBase[offs];
    }

    public static short getShort(byte[] inputBase, int offs) {
        byte a = inputBase[offs];
        byte b = inputBase[offs + 1];
        long res = b & 0xFF;
        return (short)(res << 8 | a & 0xFF);
    }

    public static void putLong(byte[] outputBase, long outputAddress, long value) {
        outputBase[(int)outputAddress] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress + 1] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress + 2] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress + 3] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress + 4] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress + 5] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress + 6] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress + 7] = (byte)(value & 0xFF);
    }

    public static void putByte(byte[] outputBase, long outputAddress, byte value) {
        outputBase[(int)outputAddress] = value;
    }

    public static void putShort(byte[] outputBase, long outputAddress, short value) {
        outputBase[(int)outputAddress] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress + 1] = (byte)(value & 0xFF);
    }

    public static void putInt(byte[] outputBase, long outputAddress, int value) {
        outputBase[(int)outputAddress] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress + 1] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress + 2] = (byte)(value & 0xFF);
        value >>= 8;
        outputBase[(int)outputAddress + 3] = (byte)(value & 0xFF);
    }

    public static void copyMemory(Object inputBase, long inputAddress, Object outputBase, long offs, long inputSize) {
        byte[] in = (byte[])inputBase;
        byte[] out = (byte[])outputBase;
        System.arraycopy(in, (int)inputAddress, out, (int)offs, (int)inputSize);
    }

}
