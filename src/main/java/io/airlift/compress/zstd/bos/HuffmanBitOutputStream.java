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
package io.airlift.compress.zstd.bos;

import io.airlift.compress.zstd.OutByteArrayWithOffs;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HuffmanBitOutputStream implements AutoCloseable {

    private final OutByteArrayWithOffs out;

    private long container;
    private int bitCount;

    public void addBits(int value, int bits) {
        container |= (long) value << bitCount;
        bitCount += bits;
        flush();
    }

    private void flush() {
        int bytes = bitCount / 8;

        for (int i = 0; i < bytes; i++) {
            out.putByte((byte) container);
            container >>>= 8;
        }

        bitCount &= 0b111;
    }

    @Override
    public void close() {
        addBits(1, 1); // end mark

        if (bitCount > 0)
            out.putByte((byte) container);
    }
}
