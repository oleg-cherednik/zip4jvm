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
package io.airlift.compress.zstd.compress;

import java.util.zip.Checksum;

/**
 * Streaming XXH64 hash. The data can be fed in chunks of any size; {@link #getValue()} returns the hash of all bytes
 * passed to {@code update()} since the creation or the last {@link #reset()} and does not change the state, so more
 * data can be added afterward.
 * <p>
 * Zstd {@code Content_Checksum} is the lower 32 bits of XXH64 with seed {@code 0}.
 */
// forked from https://github.com/airlift/slice
public final class XXHash64 implements Checksum {

    private static final long PRIME64_1 = 0x9E3779B185EBCA87L;
    private static final long PRIME64_2 = 0xC2B2AE3D27D4EB4FL;
    private static final long PRIME64_3 = 0x165667B19E3779F9L;
    private static final long PRIME64_4 = 0x85EBCA77C2b2AE63L;
    private static final long PRIME64_5 = 0x27D4EB2F165667C5L;

    // the body is processed by stripes of 4 longs
    private static final int STRIPE_SIZE = 32;

    private final long seed;
    // not processed bytes (the beginning of the next stripe)
    private final byte[] buf = new byte[STRIPE_SIZE];
    private int bufSize;
    private long totalLength;

    private long v1;
    private long v2;
    private long v3;
    private long v4;

    public XXHash64() {
        this(0);
    }

    public XXHash64(long seed) {
        this.seed = seed;
        reset();
    }

    @Override
    public void update(int b) {
        buf[bufSize++] = (byte) b;
        totalLength++;

        if (bufSize == STRIPE_SIZE) {
            updateBody(buf, 0);
            bufSize = 0;
        }
    }

    public void update(byte[] b) {
        update(b, 0, b.length);
    }

    @Override
    public void update(byte[] buf, int offs, int len) {
        if (offs < 0 || len < 0 || offs > buf.length - len)
            throw new ArrayIndexOutOfBoundsException();

        totalLength += len;

        if (bufSize > 0) {
            int bytes = Math.min(STRIPE_SIZE - bufSize, len);
            System.arraycopy(buf, offs, this.buf, bufSize, bytes);
            bufSize += bytes;
            offs += bytes;
            len -= bytes;

            if (bufSize < STRIPE_SIZE)
                return;

            updateBody(this.buf, 0);
            bufSize = 0;
        }

        while (len >= STRIPE_SIZE) {
            updateBody(buf, offs);
            offs += STRIPE_SIZE;
            len -= STRIPE_SIZE;
        }

        System.arraycopy(buf, offs, this.buf, 0, len);
        bufSize = len;
    }

    @Override
    public long getValue() {
        long hash;

        if (totalLength >= STRIPE_SIZE) {
            hash = Long.rotateLeft(v1, 1) + Long.rotateLeft(v2, 7)
                    + Long.rotateLeft(v3, 12) + Long.rotateLeft(v4, 18);

            hash = update(hash, v1);
            hash = update(hash, v2);
            hash = update(hash, v3);
            hash = update(hash, v4);
        } else {
            hash = seed + PRIME64_5;
        }

        hash += totalLength;

        int index = 0;

        while (index <= bufSize - 8) {
            hash = updateTail(hash, getLong(buf, index));
            index += 8;
        }

        if (index <= bufSize - 4) {
            hash = updateTail(hash, getInt(buf, index));
            index += 4;
        }

        while (index < bufSize) {
            hash = updateTail(hash, buf[index]);
            index++;
        }

        return finalShuffle(hash);
    }

    @Override
    public void reset() {
        v1 = seed + PRIME64_1 + PRIME64_2;
        v2 = seed + PRIME64_2;
        v3 = seed;
        v4 = seed - PRIME64_1;
        bufSize = 0;
        totalLength = 0;
    }

    private void updateBody(byte[] b, int off) {
        v1 = mix(v1, getLong(b, off));
        v2 = mix(v2, getLong(b, off + 8));
        v3 = mix(v3, getLong(b, off + 16));
        v4 = mix(v4, getLong(b, off + 24));
    }

    @Override
    public String toString() {
        return "0x" + Long.toHexString(getValue()).toUpperCase();
    }

    private static long getLong(byte[] b, int off) {
        long val = 0;

        for (int i = 0; i < 8; i++)
            val |= (long) (b[off + i] & 0xFF) << 8 * i;

        return val;
    }

    private static int getInt(byte[] b, int off) {
        int val = 0;

        for (int i = 0; i < 4; i++)
            val |= (b[off + i] & 0xFF) << 8 * i;

        return val;
    }

    private static long mix(long current, long value) {
        return Long.rotateLeft(current + value * PRIME64_2, 31) * PRIME64_1;
    }

    private static long update(long hash, long value) {
        long temp = hash ^ mix(0, value);
        return temp * PRIME64_1 + PRIME64_4;
    }

    private static long updateTail(long hash, long value) {
        long temp = hash ^ mix(0, value);
        return Long.rotateLeft(temp, 27) * PRIME64_1 + PRIME64_4;
    }

    private static long updateTail(long hash, int value) {
        long unsigned = value & 0xFFFF_FFFFL;
        long temp = hash ^ (unsigned * PRIME64_1);
        return Long.rotateLeft(temp, 23) * PRIME64_2 + PRIME64_3;
    }

    private static long updateTail(long hash, byte value) {
        int unsigned = value & 0xFF;
        long temp = hash ^ (unsigned * PRIME64_5);
        return Long.rotateLeft(temp, 11) * PRIME64_1;
    }

    private static long finalShuffle(long hash) {
        hash ^= hash >>> 33;
        hash *= PRIME64_2;
        hash ^= hash >>> 29;
        hash *= PRIME64_3;
        hash ^= hash >>> 32;
        return hash;
    }
}
