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

import lombok.Getter;

import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;
import static io.airlift.compress.zstd.Util.highestBit;
import static io.airlift.compress.zstd.Util.verify;

/**
 * Bit streams are encoded as a byte-aligned little-endian stream. Thus, bits are laid out
 * in the following manner, and the stream is read from right to left.
 * <p>
 * <p>
 * ... [16 17 18 19 20 21 22 23] [8 9 10 11 12 13 14 15] [0 1 2 3 4 5 6 7]
 */
@Getter
public class BackwardBitInputStream {

    private final byte[] buf;
    private final int fromOffs;
    private final int totalBytes;
    private int offs;

    public BackwardBitInputStream(ByteArrayWithOffs in, int totalBytes, boolean longPadded) {
        if (longPadded) {
            // the whole bitstream, zero-padded up to SIZE_OF_LONG so that the tail of a short stream
            // can be read with a plain getLong() instead of a byte-by-byte special case
            buf = new byte[Math.max(totalBytes, SIZE_OF_LONG)];
            offs = Math.max(0, buf.length - SIZE_OF_LONG);
        } else {
            buf = new byte[totalBytes];
            offs = buf.length - 1;
        }

        fromOffs = in.getOffs();
        this.totalBytes = totalBytes;
        in.copyMemory(buf, totalBytes);
    }

    public void decOffs(int bytes) {
        offs -= bytes;
    }

    public int getLastByte() {
        return buf[buf.length - 1] & 0xFF;
    }

    public long getLong() {
        long val = 0;

        for (int i = 0; i < SIZE_OF_LONG; i++)
            val = ((long) (buf[offs + i] & 0xFF) << 8 * i) | val;

        return val;
    }

    public int getBitsConsumed() {
        // the whole bitstream, zero-padded up to SIZE_OF_LONG so that the tail of a short stream
        // can be read with a plain getLong() instead of a byte-by-byte special case
        int lastByte = getLastByte();
        verify(lastByte != 0, fromOffs + buf.length, "Bitstream end mark not present");

        // padding bits of a stream shorter than SIZE_OF_LONG are consumed up front
        int padding = Math.max(0, SIZE_OF_LONG - buf.length);
        return SIZE_OF_LONG - highestBit(lastByte) + padding * 8;
    }

    @Override
    public String toString() {
        return String.format("offs: %d", offs);
    }

    static long readTail(ByteArrayWithOffs in, int offs, int inputSize) {
        long bits = in.getByte(offs) & 0xFF;

        switch (inputSize) {
            case 7:
                bits |= (in.getByte(offs + 6) & 0xFFL) << 48;
            case 6:
                bits |= (in.getByte(offs + 5) & 0xFFL) << 40;
            case 5:
                bits |= (in.getByte(offs + 4) & 0xFFL) << 32;
            case 4:
                bits |= (in.getByte(offs + 3) & 0xFFL) << 24;
            case 3:
                bits |= (in.getByte(offs + 2) & 0xFFL) << 16;
            case 2:
                bits |= (in.getByte(offs + 1) & 0xFFL) << 8;
        }

        return bits;
    }

}
