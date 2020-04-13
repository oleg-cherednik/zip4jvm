/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.io.bzip2;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;
import java.nio.ByteOrder;

/**
 * Reads bits from an InputStream.
 *
 * @NotThreadSafe
 * @since 1.10
 */
class BitInputStream {

    private static final int MAXIMUM_CACHE_SIZE = 63; // bits in long minus sign bit
    private static final long[] MASKS = createMasks();

    private static long[] createMasks() {
        long[] masks = new long[MAXIMUM_CACHE_SIZE + 1];

        for (int i = 1; i < masks.length; i++)
            masks[i] = (masks[i - 1] << 1) + 1;

        return masks;
    }

    private final DataInput in;
    private final ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    private long bitsCached = 0;
    private int bitsCachedSize = 0;

    public BitInputStream(DataInput in) {
        this.in = in;
    }

    public long readBits(final int count) throws IOException {
        if (count < 0 || count > MAXIMUM_CACHE_SIZE)
            throw new IllegalArgumentException("count must not be negative or greater than " + MAXIMUM_CACHE_SIZE);

        if (ensureCache(count))
            return IOUtils.EOF;

        if (bitsCachedSize < count) {
            return processBitsGreater57(count);
        }
        return readCachedBits(count);
    }

    public boolean readBit() throws IOException {
        return readBits(1) != 0;
    }

    private long processBitsGreater57(final int count) throws IOException {
        final long bitsOut;
        int overflowBits = 0;
        long overflow = 0L;

        // bitsCachedSize >= 57 and left-shifting it 8 bits would cause an overflow
        int bitsToAddCount = count - bitsCachedSize;
        overflowBits = Byte.SIZE - bitsToAddCount;
        final long nextByte = in.readByte();
        if (nextByte < 0) {
            return nextByte;
        }
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            long bitsToAdd = nextByte & MASKS[bitsToAddCount];
            bitsCached |= bitsToAdd << bitsCachedSize;
            overflow = (nextByte >>> bitsToAddCount) & MASKS[overflowBits];
        } else {
            bitsCached <<= bitsToAddCount;
            long bitsToAdd = (nextByte >>> overflowBits) & MASKS[bitsToAddCount];
            bitsCached |= bitsToAdd;
            overflow = nextByte & MASKS[overflowBits];
        }
        bitsOut = bitsCached & MASKS[count];
        bitsCached = overflow;
        bitsCachedSize = overflowBits;
        return bitsOut;
    }

    private long readCachedBits(int count) {
        final long bitsOut;
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            bitsOut = bitsCached & MASKS[count];
            bitsCached >>>= count;
        } else {
            bitsOut = (bitsCached >> (bitsCachedSize - count)) & MASKS[count];
        }
        bitsCachedSize -= count;
        return bitsOut;
    }

    private boolean ensureCache(int count) throws IOException {
        while (bitsCachedSize < count && bitsCachedSize < 57) {
            long nextByte = in.readByte();

            if (nextByte < 0)
                return true;
            if (byteOrder == ByteOrder.LITTLE_ENDIAN)
                bitsCached |= nextByte << bitsCachedSize;
            else {
                bitsCached <<= Byte.SIZE;
                bitsCached |= nextByte;
            }

            bitsCachedSize += Byte.SIZE;
        }

        return false;
    }

    @Override
    public String toString() {
        return in.toString();
    }
}
