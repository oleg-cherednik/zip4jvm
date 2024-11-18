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
import ru.olegcherednik.zip4jvm.io.in.data.xxx.XxxDataInput;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ThreadLocalBuffer;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Represents any source that can be treated as data input for read byte, word,
 * dword or byte array
 *
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
public abstract class DataInput extends InputStream implements Marker, XxxDataInput {

    public static final int BYTE_SIZE = 1;
    public static final int WORD_SIZE = 2;
    public static final int DWORD_SIZE = 4;
    public static final int QWORD_SIZE = 8;

    public abstract long getAbsOffs();

    public long availableLong() throws IOException {
        return available();
    }

    // signature

    public int readWordSignature() throws IOException {
        return readWord();
    }

    public int readDwordSignature() throws IOException {
        return (int) readDword();
    }

    public boolean isDwordSignature(int expected) throws IOException {
        long offs = getAbsOffs();
        int actual = readDwordSignature();
        backward((int) (getAbsOffs() - offs));
        return actual == expected;
    }

    // ---

    public abstract void seek(long absOffs) throws IOException;

    // ---------- InputStream ----------

    @Override
    public final int available() throws IOException {
        return (int) Math.min(availableLong(), Integer.MAX_VALUE);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        throw new NotImplementedException("DataInput.read(byte[], int, int)");
    }

    @Override
    public final int read() throws IOException {
        byte[] buf = ThreadLocalBuffer.getOne();
        read(buf, 0, buf.length);
        return buf[0] & 0xFF;
    }

    // ---------- DataInputFile

    public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
        throw new RuntimeException();
    }

    public void seek(int diskNo, long relativeOffs) throws IOException {
        throw new RuntimeException();
    }

    public void seek(String id) throws IOException {
        seek(getMark(id));
    }

    // ---------- DataInputLocation

    public long getDiskRelativeOffs() {
        throw new NotImplementedException();
    }

    public SrcZip getSrcZip() {
        throw new NotImplementedException();
    }

    public SrcZip.Disk getDisk() {
        throw new NotImplementedException();
    }

    // ---------- RandomAccess

    public void backward(int bytes) throws IOException {
        ValidationUtils.requireZeroOrPositive(bytes, "backward.bytes");

        seek(getAbsOffs() - bytes);
    }

    // ----------

    public abstract ByteOrder getByteOrder();

    public abstract int readByte() throws IOException;

    public abstract int readWord() throws IOException;

    public abstract long readDword() throws IOException;

    public abstract long readQword() throws IOException;

    public byte[] readBytes(int total) throws IOException {
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

    public String readString(int length, Charset charset) throws IOException {
        byte[] buf = readBytes(length);
        return buf.length == 0 ? null : new String(buf, charset);
    }

    public String readNumber(int bytes, int radix) throws IOException {
        throw new NotImplementedException();
    }

}
