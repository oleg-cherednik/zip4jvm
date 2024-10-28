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

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.out.data.EncoderDataOutput;
import ru.olegcherednik.zip4jvm.io.out.entry.EntryMetadataOutputStream;
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

    private final EncoderDataOutput encoderDataOutput;
    protected final EntryMetadataOutputStream emos;

    public static EncryptedEntryOutputStream create(ZipEntry entry,
                                                    EncoderDataOutput encoderDataOutput,
                                                    EntryMetadataOutputStream emos) throws IOException {

        CompressionMethod compressionMethod = entry.getCompressionMethod();
        CompressionLevel compressionLevel = entry.getCompressionLevel();

        if (compressionMethod == CompressionMethod.STORE)
            return new StoreEntryOutputStream(encoderDataOutput, emos);
        if (compressionMethod == CompressionMethod.DEFLATE)
            return new DeflateEntryOutputStream(compressionLevel, encoderDataOutput, emos);
        if (compressionMethod == CompressionMethod.BZIP2)
            return new Bzip2EntryOutputStream(compressionLevel, encoderDataOutput, emos);
        if (compressionMethod == CompressionMethod.LZMA)
            return new LzmaEntryOutputStream(compressionLevel,
                                             entry.isLzmaEosMarker(),
                                             entry.getUncompressedSize(),
                                             encoderDataOutput,
                                             emos);
        if (compressionMethod == CompressionMethod.ZSTD)
            return new ZstdEntryOutputStream(compressionLevel, encoderDataOutput, emos);

        throw new Zip4jvmException("Compression '%s' is not supported", compressionMethod);
    }

    @Override
    public final void write(int b) throws IOException {
        emos.write(b);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        emos.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        emos.close();
    }

    @Override
    public String toString() {
        return emos.toString();
    }
}
