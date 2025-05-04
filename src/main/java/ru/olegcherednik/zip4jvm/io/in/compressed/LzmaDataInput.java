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
package ru.olegcherednik.zip4jvm.io.in.compressed;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.ReadBufferInputStream;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import org.tukaani.xz.LZMAInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 02.02.2020
 */
public final class LzmaDataInput extends CompressedDataInput {

    private static final int HEADER_SIZE = 5;

    public static LzmaDataInput create(DataInput in) {
        return Quietly.doRuntime(() -> new LzmaDataInput(createInputStream(in), in));
    }

    private LzmaDataInput(InputStream lzma, DataInput in) {
        super(lzma, in);
    }

    // ---------- static ----------

    private static LZMAInputStream createInputStream(DataInput in) throws IOException {
        in.skip(1); // major version
        in.skip(1); // minor version
        int headerSize = in.readWord();

        if (headerSize != HEADER_SIZE)
            throw new Zip4jvmException(String.format("LZMA header size expected %d bytes: actual is %d bytes",
                                                     HEADER_SIZE, headerSize));

        byte propByte = (byte) in.readByte();
        int dictSize = (int) in.readDword();

        /*
         * Notes: actually, we should do like this (this is by specification: see 5.8.9). But SecureZip
         * does not set this bit in GeneralPurposeFlag.lzmaEosMarker, but it uses EOS in the data at the same time.
         * In this case LZMAInputStream throws an exception.
         *
         * I have checked, in case we ignore GeneralPurposeFlag.lzmaEosMarker and provide -1 to the lib, it works
         * with existed or not existed EOS marker in the data.
         */
        // long uncompressedSize = zipEntry.isLzmaEosMarker() ? -1 : zipEntry.getUncompressedSize();
        long uncompressedSize = -1;
        return new LZMAInputStream(new ReadBufferInputStream(in), uncompressedSize, propByte, dictSize);
    }

}
