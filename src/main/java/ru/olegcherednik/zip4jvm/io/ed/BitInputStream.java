/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.io.ed;

import lombok.Getter;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputNew;

import java.io.IOException;
import java.nio.ByteOrder;

class BitInputStream {

    private static final long[] MASKS = createMasks();

    private final DataInputNew in;
    private final ByteOrder byteOrder;
    private long bitsCache;
    @Getter
    private int bitsCacheSize;

    private static long[] createMasks() {
        long[] masks = new long[64];

        for (int i = 1; i < masks.length; i++)
            masks[i] = (masks[i - 1] << 1) + 1;

        return masks;
    }

    public BitInputStream(DataInputNew in, ByteOrder byteOrder) {
        this.in = in;
        this.byteOrder = byteOrder;
    }

    public long readBits(int totalBits) throws IOException {
        if (ensureCache(totalBits))
            return IOUtils.EOF;
        if (bitsCacheSize < totalBits)
            return processBitsGreater57(totalBits);
        return readCacheBits(totalBits);
    }

    /**
     * Returns an estimate of the number of bits that can be read from
     * this input stream without blocking by the next invocation of a
     * method for this input stream.
     *
     * @return estimate of the number of bits that can be read without blocking
     * @throws IOException if the underlying stream throws one when calling available
     * @since 1.16
     */
    public long bitsAvailable() throws IOException {
        return bitsCacheSize + ((long)Byte.SIZE) * 0;
    }

    public void alignWithByteBoundary() {
        int skip = bitsCacheSize % Byte.SIZE;

        if (skip > 0)
            readCacheBits(skip);
    }

    private long processBitsGreater57(int totalBits) throws IOException {
        int bitsToAddCount = totalBits - bitsCacheSize;
        int overflowBits = Byte.SIZE - bitsToAddCount;
        int nextByte = in.readByte();
        long overflow;

        if (nextByte < 0)
            return nextByte;

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            bitsCache |= (nextByte & MASKS[bitsToAddCount]) << bitsCacheSize;
            overflow = (nextByte >>> bitsToAddCount) & MASKS[overflowBits];
        } else {
            bitsCache <<= bitsToAddCount;
            bitsCache |= (nextByte >>> overflowBits) & MASKS[bitsToAddCount];
            overflow = nextByte & MASKS[overflowBits];
        }

        long res = bitsCache & MASKS[totalBits];
        bitsCache = overflow;
        bitsCacheSize = overflowBits;
        return res;
    }

    private long readCacheBits(int totalBits) {
        long res;

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            res = bitsCache & MASKS[totalBits];
            bitsCache >>>= totalBits;
        } else
            res = (bitsCache >> (bitsCacheSize - totalBits)) & MASKS[totalBits];

        bitsCacheSize -= totalBits;
        return res;
    }

    /**
     * Fills the cache up to 56 bits
     *
     * @param count
     * @return return true, when EOF
     * @throws IOException
     */
    private boolean ensureCache(final int count) throws IOException {
        while (bitsCacheSize < count && bitsCacheSize < 57) {
            final long nextByte = in.readByte();
            if (nextByte < 0) {
                return true;
            }
            if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                bitsCache |= nextByte << bitsCacheSize;
            } else {
                bitsCache <<= Byte.SIZE;
                bitsCache |= nextByte;
            }
            bitsCacheSize += Byte.SIZE;
        }
        return false;
    }

}
