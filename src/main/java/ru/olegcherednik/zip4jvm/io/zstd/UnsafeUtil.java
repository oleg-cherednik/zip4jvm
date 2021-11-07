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
import static sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET;

public final class UnsafeUtil {

    public static final Unsafe UNSAFE;
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
        return UNSAFE.getInt(inputBase, inputAddress);
    }

    public static long getLong(Object inputBase, long inputAddress) {
        return UNSAFE.getLong(inputBase, inputAddress);
    }

    public static byte getByte(Object inputBase, long inputAddress) {
        return UNSAFE.getByte(inputBase, inputAddress);
    }

    public static short getShort(Object inputBase, long inputAddress) {
        return UNSAFE.getShort(inputBase, inputAddress);
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

    public static void copyMemory(Object inputBase, long inputAddress, Object literalsBuffer, long literalsLength, long inputSize) {
        UNSAFE.copyMemory(inputBase, inputAddress, literalsBuffer, ARRAY_BYTE_BASE_OFFSET + literalsLength, inputSize);
    }

}
