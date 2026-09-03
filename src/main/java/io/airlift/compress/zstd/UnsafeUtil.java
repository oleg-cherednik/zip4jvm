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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class UnsafeUtil {

    public static byte getByte(byte[] o, long offset) {
        return o[(int) offset];
    }

    public static long getLong(byte[] o, long offset) {
        long val = 0;

        for (int i = 0; i < 8; i++)
            val = ((long) (o[(int) offset + i] & 0xFF) << 8 * i) | val;

        return val;
    }

    public static int getInt(byte[] o, long offset) {
        long val = 0;

        for (int i = 0; i < 4; i++)
            val = ((long) (o[(int) offset + i] & 0xFF) << 8 * i) | val;

        return (int) val;
    }

    public static short getShort(byte[] o, long offset) {
        int val = 0;

        for (int i = 0; i < 2; i++)
            val = ((o[(int) offset + i] & 0xFF) << 8 * i) | val;

        return (short) val;
    }

    public static void copyMemory(byte[] srcBase, long srcOffset,
                                  byte[] destBase, long destOffset,
                                  long bytes) {
        System.arraycopy(srcBase, (int) srcOffset, destBase, (int) destOffset, (int) bytes);
    }

    public static void putLong(byte[] o, long offset, long x) {
        o[(int) offset] = (byte) (x & 0xFF);
        o[(int) offset + 1] = (byte) ((x & 0xFF00) >> 8);
        o[(int) offset + 2] = (byte) ((x & 0xFF0000) >> 8 * 2);
        o[(int) offset + 3] = (byte) ((x & 0xFF000000) >> 8 * 3);
        o[(int) offset + 4] = (byte) ((x & 0xFF00000000L) >> 8 * 4);
        o[(int) offset + 5] = (byte) ((x & 0xFF0000000000L) >> 8 * 5);
        o[(int) offset + 6] = (byte) ((x & 0xFF000000000000L) >> 8 * 6);
        o[(int) offset + 7] = (byte) ((x & 0xFF00000000000000L) >> 8 * 7);
    }

    public static void putByte(byte[] o, long offset, byte x) {
        o[(int) offset] = x;
    }

    public static void putInt(byte[] o, long offset, int x) {
        o[(int) offset] = (byte) (x & 0xFF);
        o[(int) offset + 1] = (byte) ((x & 0xFF00) >> 8);
        o[(int) offset + 2] = (byte) ((x & 0xFF0000) >> 8 * 2);
        o[(int) offset + 3] = (byte) ((x & 0xFF000000) >> 8 * 3);
    }

    public static void putShort(byte[] o, long offset, short x) {
        o[(int) offset] = (byte) (x & 0xFF);
        o[(int) offset + 1] = (byte) ((x & 0xFF00) >> 8);
    }

}
