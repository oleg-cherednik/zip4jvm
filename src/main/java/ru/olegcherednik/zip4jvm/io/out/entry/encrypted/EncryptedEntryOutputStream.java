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
package ru.olegcherednik.zip4jvm.io.out.entry.encrypted;

import ru.olegcherednik.zip4jvm.exception.CompressionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.out.data.EncoderDataOutput;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 12.02.2020
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EncryptedEntryOutputStream extends OutputStream {

    public static EncryptedEntryOutputStream create(ZipEntry entry, EncoderDataOutput encoderDataOutput)
            throws IOException {

        CompressionMethod compressionMethod = entry.getCompressionMethod();
        CompressionLevel compressionLevel = entry.getCompressionLevel();

        if (compressionMethod == CompressionMethod.STORE)
            return new StoreEntryOutputStream(encoderDataOutput);
        if (compressionMethod == CompressionMethod.DEFLATE)
            return new DeflateEntryOutputStream(compressionLevel, encoderDataOutput);
        if (compressionMethod == CompressionMethod.BZIP2)
            return new Bzip2EntryOutputStream(compressionLevel, encoderDataOutput);
        if (compressionMethod == CompressionMethod.LZMA)
            return new LzmaEntryOutputStream(compressionLevel,
                                             entry.isLzmaEosMarker(),
                                             entry.getUncompressedSize(),
                                             encoderDataOutput);
        if (compressionMethod == CompressionMethod.ZSTD)
            return new ZstdEntryOutputStream(compressionLevel, encoderDataOutput);

        throw new CompressionNotSupportedException(compressionMethod);
    }

    @Override
    public final void write(int b) throws IOException {
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

}
