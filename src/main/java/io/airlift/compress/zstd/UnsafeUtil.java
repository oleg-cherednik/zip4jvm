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

import io.airlift.compress.IncompatibleJvmException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteOrder;

import static java.lang.String.format;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class UnsafeUtil {

    private static final Unsafe UNSAFE;
    private static final long ADDRESS_OFFSET;

    static {
        ByteOrder order = ByteOrder.nativeOrder();
        if (!order.equals(ByteOrder.LITTLE_ENDIAN)) {
            throw new IncompatibleJvmException(format("Zstandard requires a little endian platform (found %s)", order));
        }

        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
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

    public static byte getByte(byte[] o, long offset) {
        return o[(int) (offset - ADDRESS_OFFSET)];
    }

    public static long getLong(byte[] o, long offset) {
        int offs = (int) (offset - ADDRESS_OFFSET);
        long val = 0;

        for (int i = 0; i < 8; i++)
            val = ((long) (o[offs + i] & 0xFF) << 8 * i) | val;

        return val;
    }

    public static int getInt(byte[] o, long offset) {
        int offs = (int) (offset - ADDRESS_OFFSET);
        long val = 0;

        for (int i = 0; i < 4; i++)
            val = ((long) (o[offs + i] & 0xFF) << 8 * i) | val;

        return (int) val;
    }

    public static short getShort(byte[] o, long offset) {
        int offs = (int) (offset - ADDRESS_OFFSET);
        int val = 0;

        for (int i = 0; i < 2; i++)
            val = ((o[offs + i] & 0xFF) << 8 * i) | val;

        return (short) val;
    }

    public static void copyMemory(byte[] srcBase, long srcOffset,
                                  byte[] destBase, long destOffset,
                                  long bytes) {
        UNSAFE.copyMemory(srcBase, srcOffset, destBase, destOffset, bytes);
    }

    public static void putLong(byte[] o, long offset, long x) {
        int offs = (int) (offset - ADDRESS_OFFSET);

        o[offs] = (byte) (x & 0xFF);
        o[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        o[offs + 2] = (byte) ((x & 0xFF0000) >> 8 * 2);
        o[offs + 3] = (byte) ((x & 0xFF000000) >> 8 * 3);
        o[offs + 4] = (byte) ((x & 0xFF00000000L) >> 8 * 4);
        o[offs + 5] = (byte) ((x & 0xFF0000000000L) >> 8 * 5);
        o[offs + 6] = (byte) ((x & 0xFF000000000000L) >> 8 * 6);
        o[offs + 7] = (byte) ((x & 0xFF00000000000000L) >> 8 * 7);
    }

    public static void putByte(byte[] o, long offset, byte x) {
        o[(int) (offset - ADDRESS_OFFSET)] = x;
    }

    public static void putInt(byte[] o, long offset, int x) {
        int offs = (int) (offset - ADDRESS_OFFSET);

        o[offs] = (byte) (x & 0xFF);
        o[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        o[offs + 2] = (byte) ((x & 0xFF0000) >> 8 * 2);
        o[offs + 3] = (byte) ((x & 0xFF000000) >> 8 * 3);
    }

    public static void putShort(byte[] o, long offset, short x) {
        int offs = (int) (offset - ADDRESS_OFFSET);

        o[offs] = (byte) (x & 0xFF);
        o[offs + 1] = (byte) ((x & 0xFF00) >> 8);
    }

}
