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

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
final class BitInputStream {

    private static final long[] MASKS = createMasks();

    private static long[] createMasks() {
        long[] masks = new long[64];

        for (int i = 1; i < masks.length; i++)
            masks[i] = (masks[i - 1] << 1) + 1;

        return masks;
    }

    private final DataInput in;
    private long bitsCache;
    private int bitsCacheSize;

    public BitInputStream(DataInput in) {
        this.in = in;
    }

    public long readBits(int totalBits) throws IOException {
        if (ensureCache(totalBits))
            return IOUtils.EOF;
        if (bitsCacheSize < totalBits)
            return processBitsGreater57(totalBits);
        return readCachedBits(totalBits);
    }

    public boolean readBit() throws IOException {
        return readBits(1) != 0;
    }

    private long processBitsGreater57(int totalBits) throws IOException {
        int bitsToAddCount = totalBits - bitsCacheSize;
        int overflowBits = Byte.SIZE - bitsToAddCount;
        int nextByte = in.readByte();

        if (nextByte < 0)
            return nextByte;

        bitsCache <<= bitsToAddCount;
        bitsCache |= (nextByte >>> overflowBits) & MASKS[bitsToAddCount];

        long res = bitsCache & MASKS[totalBits];
        bitsCache = nextByte & MASKS[overflowBits];
        bitsCacheSize = overflowBits;

        return res;
    }

    private long readCachedBits(int totalBits) {
        final long bitsOut;
        bitsOut = (bitsCache >> (bitsCacheSize - totalBits)) & MASKS[totalBits];
        bitsCacheSize -= totalBits;
        return bitsOut;
    }

    private boolean ensureCache(int totalBits) throws IOException {
        while (bitsCacheSize < totalBits && bitsCacheSize < 57) {
            long nextByte = in.readByte();

            if (nextByte < 0)
                return true;

            bitsCache <<= Byte.SIZE;
            bitsCache |= nextByte;
            bitsCacheSize += Byte.SIZE;
        }

        return false;
    }

    @Override
    public String toString() {
        return in.toString();
    }
}
