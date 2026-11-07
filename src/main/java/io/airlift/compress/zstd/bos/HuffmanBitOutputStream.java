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

import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;

public class HuffmanBitOutputStream {

    private final OutByteArrayWithOffs out;
    private final int outputAddress;
    private final int outputLimit;

    private long container;
    private int bitCount;
    private int currentAddress;

    public HuffmanBitOutputStream(OutByteArrayWithOffs out, int outputSize) {
        this.out = out;
        outputAddress = out.getOffs();
        outputLimit = out.getOffs() + outputSize - SIZE_OF_LONG;
        currentAddress = out.getOffs();
    }

    public void addBitsFast(int value, int bits) {
        container |= (long) value << bitCount;
        bitCount += bits;
    }

    public void flush() {
        int bytes = bitCount / 8;

        for (int i = 0; i < bytes; i++) {
            out.putByte((byte) container);
            container >>>= 8;
        }

        currentAddress += bytes;

        if (currentAddress > outputLimit) {
            currentAddress = outputLimit;
        }

        bitCount &= 0b111;
    }

    public int close() {
        addBitsFast(1, 1); // end mark
        flush();

        if (currentAddress >= outputLimit) {
            return 0;
        }

        if (bitCount > 0) {
            // the last incomplete byte is not written by flush(), so write it here
            out.setOffs(currentAddress);
            out.putByte((byte) container);
        }

        return (currentAddress - outputAddress) + (bitCount > 0 ? 1 : 0);
    }
}
