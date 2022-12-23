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
package ru.olegcherednik.zip4jvm.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.CompressionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.in.buf.Bzip2DataInput;
import ru.olegcherednik.zip4jvm.io.in.buf.EnhancedDeflateDataInput;
import ru.olegcherednik.zip4jvm.io.in.buf.InflateDataInput;
import ru.olegcherednik.zip4jvm.io.in.buf.StoreDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.entry.Bzip2EntryInputStream;
import ru.olegcherednik.zip4jvm.io.in.entry.EnhancedDeflateEntryInputStream;
import ru.olegcherednik.zip4jvm.io.in.entry.EntryInputStream;
import ru.olegcherednik.zip4jvm.io.in.entry.InflateEntryInputStream;
import ru.olegcherednik.zip4jvm.io.in.entry.LzmaEntryInputStream;
import ru.olegcherednik.zip4jvm.io.in.entry.StoreEntryInputStream;
import ru.olegcherednik.zip4jvm.io.in.entry.ZstdEntryInputStream;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

/**
 * This matches with {@link  CompressionMethod}, but here we have only supported methods.
 *
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
@RequiredArgsConstructor
public enum Compression {

    STORE(CompressionMethod.STORE, StoreDataInput::new, StoreEntryInputStream::new),
    DEFLATE(CompressionMethod.DEFLATE, InflateDataInput::new, InflateEntryInputStream::new),
    ENHANCED_DEFLATE(CompressionMethod.ENHANCED_DEFLATE, EnhancedDeflateDataInput::new, EnhancedDeflateEntryInputStream::new),
    BZIP2(CompressionMethod.BZIP2, Bzip2DataInput::new, Bzip2EntryInputStream::new),
    LZMA(CompressionMethod.LZMA, null, LzmaEntryInputStream::new),
    ZSTD(CompressionMethod.ZSTD, null, ZstdEntryInputStream::new);

    @Getter
    private final CompressionMethod method;
    private final CreateDataInput createDataInput;
    private final CreateEntryInputStream createEntryInputStream;

    public DataInput createDataInput(DataInput in, int uncompressedSize) {
        if (createDataInput != null)
            return createDataInput.create(in, uncompressedSize);
        throw new CompressionNotSupportedException(this);
    }

    public EntryInputStream createEntryInputStream(DataInput in, ZipEntry zipEntry) {
        if (createEntryInputStream != null)
            return createEntryInputStream.create(in, zipEntry);
        throw new CompressionNotSupportedException(this);
    }

    public static Compression parseCompressionMethod(CompressionMethod compressionMethod) {
        for (Compression compression : values())
            if (compression.method == compressionMethod)
                return compression;

        throw new CompressionNotSupportedException(compressionMethod);
    }

    private interface CreateDataInput {

        DataInput create(DataInput in, int uncompressedSize);
    }

    private interface CreateEntryInputStream {

        EntryInputStream create(DataInput in, ZipEntry zipEntry);
    }

}
