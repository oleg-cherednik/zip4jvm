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

import ru.olegcherednik.zip4jvm.io.Endianness;
import ru.olegcherednik.zip4jvm.io.in.RandomAccess;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import java.nio.charset.Charset;

/**
 * Represents any source that can be treated as data input for read byte, word,
 * dword or byte array
 *
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
public interface DataInput extends RandomAccess, Mark, ReadBuffer {

    int byteSize();

    int wordSize();

    int dwordSize();

    int qwordSize();

    long getAbsoluteOffs();

    // TODO looks like should be available
    long size();

    int readByte();

    int readWord();

    long readDword();

    long readQword();

    byte[] readBytes(int total);

    String readString(int length, Charset charset);

    String readNumber(int bytes, int radix);

    Endianness getEndianness();

    // TODO signature should be read in normal order

    default int dwordSignatureSize() {
        return dwordSize();
    }

    default int wordSignatureSize() {
        return wordSize();
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
