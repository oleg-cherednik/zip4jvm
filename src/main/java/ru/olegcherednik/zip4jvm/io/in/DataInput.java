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
package ru.olegcherednik.zip4jvm.io.in;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.Marker;
import ru.olegcherednik.zip4jvm.utils.apache.ArrayUtils;

import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * This interface describes an abstract resource form which we can read data
 * consecutively. It does not support a random data access.
 *
 * @author Oleg Cherednik
 * @since 18.11.2024
 */
public interface DataInput extends Marker, ReadBuffer, Closeable {

    ByteOrder getByteOrder();

    long getAbsOffs();

    int readByte();

    int readWord();

    long readDword();

    long readQword();

    default String readString(int length, Charset charset) {
        byte[] buf = readBytes(length);
        return buf.length == 0 ? null : new String(buf, charset);
    }

    default byte[] readBytes(int total) {
        if (total <= 0)
            return ArrayUtils.EMPTY_BYTE_ARRAY;

        byte[] buf = new byte[total];
        int nowRead = read(buf, 0, buf.length);

        if (nowRead == IOUtils.EOF)
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        if (nowRead < total)
            return Arrays.copyOfRange(buf, 0, nowRead);
        return buf;
    }

    default BigInteger readBigInteger(int bytes) {
        return bytes <= 0 ? null : getByteOrder().readBigInteger(bytes, this);
    }

    long skip(long bytes);

    default int readWordSignature() {
        return readWord();
    }

    default int readDwordSignature() {
        return (int) readDword();
    }

    // ---------- AutoCloseable ----------

    @Override
    default void close() {
        /* nothing to close */
    }

}
