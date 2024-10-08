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
package ru.olegcherednik.zip4jvm.io.lzma.rangecoder;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.lzma.LzmaCorruptedInputException;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.02.2020
 */
public class RangeDecoder extends RangeCoder {

    private final DataInput in;
    private int range = -1;
    private int code;

    public RangeDecoder(DataInput in) throws IOException {
        this.in = in;

        if (in.readByte() != 0x00)
            throw new LzmaCorruptedInputException();

        code = readCode(in);
    }

    private static int readCode(DataInput in) throws IOException {
        int code = 0;

        for (int i = 0; i < 4; ++i)
            code = code << 8 | in.readByte();

        return code;
    }

    public int decodeBit(short[] probs, int index) throws IOException {
        normalize();

        int prob = probs[index];
        int bound = (range >>> BIT_MODEL_TOTAL_BITS) * prob;
        int bit;

        // Compare code and bound as if they were unsigned 32-bit integers.
        if ((code ^ 0x80000000) < (bound ^ 0x80000000)) {
            range = bound;
            probs[index] = (short) (prob + ((BIT_MODEL_TOTAL - prob) >>> MOVE_BITS));
            bit = 0;
        } else {
            range -= bound;
            code -= bound;
            probs[index] = (short) (prob - (prob >>> MOVE_BITS));
            bit = 1;
        }

        return bit;
    }

    public boolean isFinished() {
        return code == 0;
    }

    public int decodeBitTree(short[] probs) throws IOException {
        int symbol = 1;

        do {
            symbol = (symbol << 1) | decodeBit(probs, symbol);
        } while (symbol < probs.length);

        return symbol - probs.length;
    }

    public int decodeReverseBitTree(short[] probs) throws IOException {
        int symbol = 1;
        int i = 0;
        int result = 0;

        do {
            int bit = decodeBit(probs, symbol);
            symbol = (symbol << 1) | bit;
            result |= bit << i++;
        } while (symbol < probs.length);

        return result;
    }

    public int decodeDirectBits(int count) throws IOException {
        int result = 0;

        do {
            normalize();

            range >>>= 1;
            int t = (code - range) >>> 31;
            code -= range & (t - 1);
            result = (result << 1) | (1 - t);
        } while (--count != 0);

        return result;
    }

    public void normalize() throws IOException {
        if ((range & TOP_MASK) == 0) {
            code = (code << SHIFT_BITS) | in.readByte();
            range <<= SHIFT_BITS;
        }
    }

    @Override
    public void close() throws IOException {
        if (in instanceof Closeable)
            ((Closeable) in).close();
    }
}
