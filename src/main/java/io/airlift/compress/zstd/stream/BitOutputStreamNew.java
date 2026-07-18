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
package io.airlift.compress.zstd.stream;

import io.airlift.compress.Foo;

public class BitOutputStreamNew {

    private static final long[] BIT_MASK = {
            0x0, 0x1, 0x3, 0x7, 0xF, 0x1F,
            0x3F, 0x7F, 0xFF, 0x1FF, 0x3FF, 0x7FF,
            0xFFF, 0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF, 0x1FFFF,
            0x3FFFF, 0x7FFFF, 0xFFFFF, 0x1FFFFF, 0x3FFFFF, 0x7FFFFF,
            0xFFFFFF, 0x1FFFFFF, 0x3FFFFFF, 0x7FFFFFF, 0xFFFFFFF, 0x1FFFFFFF,
            0x3FFFFFFF, 0x7FFFFFFF }; // up to 31 bits

    private final Foo out;
    private final long outputAddress;

    private long container;
    private int bitCount;

    public BitOutputStreamNew(Foo out) {
        this.out = out;
        this.outputAddress = out.getOffs();
    }

    public void addBits(int value, int bits) {
        container |= (value & BIT_MASK[bits]) << bitCount;
        bitCount += bits;
    }

    /**
     * Note: leading bits of value must be 0
     */
    public void addBitsFast(int value, int bits) {
        container |= ((long) value) << bitCount;
        bitCount += bits;
    }

    public void flush() {
        int bytes = bitCount >>> 3;

        for (int i = 0; i < bytes; i++) {
            out.putByte((byte) container);
            container >>>= 8;
            bitCount -= 8;
        }
    }

    public int close() {
        addBitsFast(1, 1); // end mark

        flush();

        if (bitCount > 0)
            out.putByte((byte) container);

        return (int) (out.getOffs() - outputAddress);
    }
}
