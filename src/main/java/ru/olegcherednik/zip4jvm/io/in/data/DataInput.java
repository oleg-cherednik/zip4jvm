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
import ru.olegcherednik.zip4jvm.io.in.RandomAccess;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Represents any source that can be treated as data input for read byte, word,
 * dword or byte array
 *
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
public interface DataInput extends RandomAccess, Marker, Closeable {

    int BYTE_SIZE = 1;
    int WORD_SIZE = 2;
    int DWORD_SIZE = 4;
    int QWORD_SIZE = 8;

    // ---------- DataInputFile

//    default long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
//        throw new RuntimeException();
//    }

//    default void seek(int diskNo, long relativeOffs) {
//        throw new RuntimeException();
//    }

//    default void seek(String id) throws IOException {
//        throw new RuntimeException();
//    }

    // ---------- DataInputLocation

    long getAbsoluteOffs();

    default long getDiskRelativeOffs() {
        throw new RuntimeException();
    }

    default SrcZip getSrcZip() {
        throw new RuntimeException();
    }

    default SrcZip.Disk getDisk() {
        throw new RuntimeException();
    }

    // ----------

    ByteOrder getByteOrder();

    int read(byte[] buf, int offs, int len) throws IOException;

    default int read() throws IOException {
        byte[] buf = new byte[1];
        read(buf, 0, 1);
        return buf[0];
    }

    // TODO looks like should be available
    long size();

    int readByte();

    int readWord();

    long readDword();

    long readQword();

    byte[] readBytes(int total);

    String readString(int length, Charset charset);

    String readNumber(int bytes, int radix);

    // TODO signature should be read in normal order

    default int dwordSignatureSize() {
        return DWORD_SIZE;
    }

    default int wordSignatureSize() {
        return WORD_SIZE;
    }

    default int readWordSignature() {
        return readWord();
    }

    default int readDwordSignature() {
        return (int) readDword();
    }

    // ---------- RandomAccess ----------

    @Override
    default void backward(int bytes) {
        ValidationUtils.requireZeroOrPositive(bytes, "backward.bytes");

        seek(getAbsoluteOffs() - bytes);
    }

}
