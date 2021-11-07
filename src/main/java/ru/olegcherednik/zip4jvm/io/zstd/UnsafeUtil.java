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

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteOrder;

import static java.lang.String.format;

public final class UnsafeUtil {

    public static final int ARRAY_BYTE_BASE_OFFSET = Unsafe.ARRAY_BYTE_BASE_OFFSET;

    private static final Unsafe UNSAFE;
    private static final long ADDRESS_OFFSET;

    private UnsafeUtil() {}

    static {
        ByteOrder order = ByteOrder.nativeOrder();
        if (!order.equals(ByteOrder.LITTLE_ENDIAN)) {
            throw new IncompatibleJvmException(format("Zstandard requires a little endian platform (found %s)", order));
        }

        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe)theUnsafe.get(null);
        } catch (Exception e) {
            throw new IncompatibleJvmException("Zstandard requires access to sun.misc.Unsafe");
        }

        try {
            // fetch the address field for direct buffers
            ADDRESS_OFFSET = UNSAFE.objectFieldOffset(Buffer.class.getDeclaredField("address"));
        } catch (NoSuchFieldException e) {
            throw new IncompatibleJvmException("Zstandard requires access to java.nio.Buffer raw address field");
        }
    }

    public static int getInt(Object inputBase, long inputAddress) {
        byte[] in = (byte[])inputBase;
        byte a = in[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET];
        byte b = in[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 1];
        byte c = in[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 2];
        byte d = in[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 3];
        int res = d & 0xFF;
        res = res << 8 | c & 0xFF;
        res = res << 8 | b & 0xFF;
        return res << 8 | a & 0xFF;
//        return UNSAFE.getInt(inputBase, inputAddress);
    }

    public static long getLong(Object inputBase, long inputAddress) {
        byte[] in = (byte[])inputBase;
        byte a = in[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET];
        byte b = in[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 1];
        byte c = in[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 2];
        byte d = in[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 3];
        byte e = in[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 4];
        byte f = in[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 5];
        byte g = in[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 6];
        byte h = in[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 7];
        long res = h & 0xFF;
        res = res << 8 | g & 0xFF;
        res = res << 8 | f & 0xFF;
        res = res << 8 | e & 0xFF;
        res = res << 8 | d & 0xFF;
        res = res << 8 | c & 0xFF;
        res = res << 8 | b & 0xFF;
        return res << 8 | a & 0xFF;
//        return UNSAFE.getLong(inputBase, inputAddress);
    }

    public static byte getByte(Object inputBase, long inputAddress) {
        byte[] in = (byte[])inputBase;
        return in[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET];
//        return UNSAFE.getByte(inputBase, inputAddress);
    }

    public static short getShort(Object inputBase, long inputAddress) {
        byte[] in = (byte[])inputBase;
        byte a = in[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET];
        byte b = in[(int)inputAddress - ARRAY_BYTE_BASE_OFFSET + 1];
        long res = b & 0xFF;
        return (short)(res << 8 | a & 0xFF);
//        return UNSAFE.getShort(inputBase, inputAddress);
    }

    public static long getAddress(Buffer buffer) {
        if (!buffer.isDirect()) {
            throw new IllegalArgumentException("buffer is not direct");
        }

        return UNSAFE.getLong(buffer, ADDRESS_OFFSET);
    }

    public static void putLong(Object outputBase, long currentAddress, long container) {
        UNSAFE.putLong(outputBase, currentAddress, container);
    }

    public static void putByte(Object outputBase, long output, byte value) {
        UNSAFE.putByte(outputBase, output, value);
    }

    public static void putShort(Object outputBase, long output, short value) {
        UNSAFE.putShort(outputBase, output, value);
    }

    public static void putInt(Object outputBase, long output, int value) {
        UNSAFE.putInt(outputBase, output, value);
    }

    public static void copyMemory(Object inputBase, long inputAddress, Object literalsBuffer, long offs, long inputSize) {
        UNSAFE.copyMemory(inputBase, inputAddress, literalsBuffer, offs, inputSize);
    }

}
