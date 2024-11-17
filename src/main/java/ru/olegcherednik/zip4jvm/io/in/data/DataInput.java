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
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Represents any source that can be treated as data input for read byte, word,
 * dword or byte array
 *
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
public abstract class DataInput extends InputStream implements Marker {

    public static final int BYTE_SIZE = 1;
    public static final int WORD_SIZE = 2;
    public static final int DWORD_SIZE = 4;
    public static final int QWORD_SIZE = 8;

    // ---------- DataInputFile

    public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
        throw new RuntimeException();
    }

    public void seek(int diskNo, long relativeOffs) {
        throw new RuntimeException();
    }

    public void seek(String id) throws IOException {
        throw new RuntimeException();
    }

    // ---------- DataInputLocation

    public abstract long getAbsoluteOffs();

    public long getDiskRelativeOffs() {
        throw new RuntimeException();
    }

    public SrcZip getSrcZip() {
        throw new RuntimeException();
    }

    public SrcZip.Disk getDisk() {
        throw new RuntimeException();
    }

    // ---------- RandomAccess

    // public abstract long skip(long bytes);

    public void backward(int bytes) {
        ValidationUtils.requireZeroOrPositive(bytes, "backward.bytes");

        seek(getAbsoluteOffs() - bytes);
    }

    public abstract void seek(long absoluteOffs);

    // ----------

    public abstract ByteOrder getByteOrder();

    public abstract int read(byte[] buf, int offs, int len) throws IOException;

    public int read() throws IOException {
        byte[] buf = new byte[1];
        read(buf, 0, 1);
        return buf[0];
    }

    // TODO looks like should be available
    public abstract long size();

    public abstract int readByte() throws IOException;

    public abstract int readWord() throws IOException;

    public abstract long readDword() throws IOException;

    public abstract long readQword() throws IOException;

    public abstract byte[] readBytes(int total) throws IOException;

    public abstract String readString(int length, Charset charset) throws IOException;

    public abstract String readNumber(int bytes, int radix) throws IOException;

    // TODO signature should be read in normal order

    public int dwordSignatureSize() {
        return DWORD_SIZE;
    }

    public int wordSignatureSize() {
        return WORD_SIZE;
    }

    public int readWordSignature() throws IOException {
        return readWord();
    }

    public int readDwordSignature() throws IOException {
        return (int) readDword();
    }

}
