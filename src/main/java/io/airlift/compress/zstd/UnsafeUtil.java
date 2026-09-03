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
import java.nio.ByteOrder;

import static java.lang.String.format;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class UnsafeUtil {

    private static final Unsafe UNSAFE;

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
    }

    public static byte getByte(byte[] o, long offset) {
        return UNSAFE.getByte(o, offset);
    }

    public static long getLong(byte[] o, long offset) {
        return UNSAFE.getLong(o, offset);
    }

    public static int getInt(byte[] o, long offset) {
        return UNSAFE.getInt(o, offset);
    }

    public static short getShort(byte[] o, long offset) {
        return UNSAFE.getShort(o, offset);
    }

    public static void copyMemory(byte[] srcBase, long srcOffset,
                                  byte[] destBase, long destOffset,
                                  long bytes) {
        UNSAFE.copyMemory(srcBase, srcOffset, destBase, destOffset, bytes);
    }

    public static void putLong(byte[] o, long offset, long x) {
        UNSAFE.putLong(o, offset, x);
    }

    public static void putByte(byte[] o, long offset, byte x) {
        UNSAFE.putByte(o, offset, x);
    }

    public static void putInt(byte[] o, long offset, int x) {
        UNSAFE.putInt(o, offset, x);
    }

    public static void putShort(byte[] o, long offset, short x) {
        UNSAFE.putShort(o, offset, x);
    }

}
