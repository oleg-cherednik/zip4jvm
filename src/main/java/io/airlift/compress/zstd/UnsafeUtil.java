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

    public static byte getByte(ByteArrayWithOffs in, long offset) {
        return in.buf[(int) offset];
    }

    public static long getLong(ByteArrayWithOffs in, long offset) {
        long val = 0;

        for (int i = 0; i < 8; i++)
            val = ((long) (in.buf[(int) offset + i] & 0xFF) << 8 * i) | val;

        return val;
    }

    public static int getInt(ByteArrayWithOffs in, long offset) {
        long val = 0;

        for (int i = 0; i < 4; i++)
            val = ((long) (in.buf[(int) offset + i] & 0xFF) << 8 * i) | val;

        return (int) val;
    }

    public static void copyMemory(byte[] in, long srcOffset,
                                  byte[] out, long destOffset,
                                  long bytes) {
        System.arraycopy(in, (int) srcOffset, out, (int) destOffset, (int) bytes);
    }

}
