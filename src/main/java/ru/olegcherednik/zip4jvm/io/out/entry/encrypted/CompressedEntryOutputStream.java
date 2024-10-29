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
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents a compressed stream using given {@link CompressionMethod}.
 * It extends from the {@link OutputStream} to be able to use standard output
 * optimizations (e.g. from {@link IOUtils}).
 *
 * @author Oleg Cherednik
 * @since 12.02.2020
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class CompressedEntryOutputStream extends OutputStream {

    public static CompressedEntryOutputStream create(ZipEntry entry, DataOutput out) throws IOException {
        CompressionMethod compressionMethod = entry.getCompressionMethod();
        CompressionLevel compressionLevel = entry.getCompressionLevel();

        if (compressionMethod == CompressionMethod.STORE)
            return new StoreEntryOutputStream(out);
        if (compressionMethod == CompressionMethod.DEFLATE)
            return new DeflateEntryOutputStream(out, compressionLevel);
        if (compressionMethod == CompressionMethod.BZIP2)
            return new Bzip2EntryOutputStream(out, compressionLevel);
        if (compressionMethod == CompressionMethod.LZMA)
            return new LzmaEntryOutputStream(out,
                                             compressionLevel,
                                             entry.isLzmaEosMarker(),
                                             entry.getUncompressedSize());
        if (compressionMethod == CompressionMethod.ZSTD)
            return new ZstdEntryOutputStream(out, compressionLevel);

        throw new CompressionNotSupportedException(compressionMethod);
    }

    @Override
    public final void write(int b) throws IOException {
        write(new byte[] { (byte) b }, 0, 1);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        throw new NotImplementedException("CompressedEntryOutputStream.write(byte[], int, int)");
    }

}
