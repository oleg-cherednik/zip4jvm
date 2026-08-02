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
package ru.olegcherednik.zip4jvm.utils;

import ru.olegcherednik.zip4jvm.io.in.ReadBuffer;
import ru.olegcherednik.zip4jvm.io.out.WriteBuffer;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.EOFException;
import java.math.BigInteger;

/**
 * @author Oleg Cherednik
 * @since 23.11.2024
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ByteUtils {

    public static final int BYTE_SIZE = 1;
    public static final int WORD_SIZE = 2;
    public static final int DWORD_SIZE = 4;
    public static final int QWORD_SIZE = 8;

    public static int getByte(long val, int i) {
        return (int) (val >> 8 * i) & 0xFF;
    }

    // ---------- read ----------

    public static int readByte(ReadBuffer in) {
        return read(in);
    }

    public static int readWord(ReadBuffer in) {
        int val = 0;

        for (int i = 0; i < 2; i++)
            val = read(in) << 8 * i | val;

        return val & 0xFFFF;
    }

    public static long readDword(ReadBuffer in) {
        long val = 0;

        for (int i = 0; i < 4; i++)
            val = (long) read(in) << 8 * i | val;

        return val & 0xFFFFFFFFL;
    }

    public static long readDword(byte[] buf, int offs) {
        long val = 0;

        for (int i = 0; i < 4; i++)
            val = (long) (buf[offs + i] & 0xFF) << 8 * i | val;

        return val & 0xFFFFFFFFL;
    }

    public static long readQword(ReadBuffer in) {
        long val = 0;

        for (int i = 0; i < 8; i++)
            val = (long) read(in) << 8 * i | val;

        return val;
    }

    public static BigInteger readBigInteger(int size, ReadBuffer in) {
        byte[] buf = new byte[size];

        for (int i = buf.length - 1; i >= 0; i--)
            buf[i] = (byte) read(in);

        return new BigInteger(buf);
    }

    private static int read(ReadBuffer in) {
        return Quietly.doRuntime(() -> {
            int b = in.read();

            if (b == IOUtils.EOF)
                throw new EOFException("End Of File");

            return b & 0xFF;
        });
    }

    // ---------- write ----------

    public static void writeByte(int val, WriteBuffer out) {
        Quietly.doRuntime(() -> {
            out.write(val);
        });
    }

    public static void writeWord(int val, WriteBuffer out) {
        Quietly.doRuntime(() -> {
            for (int i = 0; i < 2; i++)
                out.write(getByte(val, i));
        });
    }

    public static void writeDword(long val, WriteBuffer out) {
        Quietly.doRuntime(() -> {
            for (int i = 0; i < 4; i++)
                out.write(getByte(val, i));
        });
    }

    public static void writeQword(long val, WriteBuffer out) {
        Quietly.doRuntime(() -> {
            for (int i = 0; i < 8; i++)
                out.write(getByte(val, i));
        });
    }

}
