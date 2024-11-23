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
package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.Marker;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * This interface describes and abstract resource form which we can read data
 * consecutively. It does not support a random data access.
 *
 * @author Oleg Cherednik
 * @since 18.11.2024
 */
public interface DataInput extends Marker, ReadBuffer, Closeable {

    ByteOrder getByteOrder();

    long getAbsOffs();

    int readByte() throws IOException;

    int readWord() throws IOException;

    long readDword() throws IOException;

    long readQword() throws IOException;

    default String readString(int length, Charset charset) throws IOException {
        byte[] buf = readBytes(length);
        return buf.length == 0 ? null : new String(buf, charset);
    }

    default byte[] readBytes(int total) throws IOException {
        if (total <= 0)
            return ArrayUtils.EMPTY_BYTE_ARRAY;

        byte[] buf = new byte[total];
        int n = read(buf, 0, buf.length);

        if (n == IOUtils.EOF)
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        if (n < total)
            return Arrays.copyOfRange(buf, 0, n);
        return buf;
    }

    default BigInteger readBigInteger(int bytes) throws IOException {
        return bytes <= 0 ? null : getByteOrder().readBigInteger(bytes, this);
    }

    long skip(long bytes) throws IOException;

    default int readWordSignature() throws IOException {
        return readWord();
    }

    default int readDwordSignature() throws IOException {
        return (int) readDword();
    }

}
