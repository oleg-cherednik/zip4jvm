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
package ru.olegcherednik.zip4jvm.io.out.data.compressed;

import ru.olegcherednik.zip4jvm.exception.CompressionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.out.data.BaseDataOutput;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import org.apache.commons.io.IOUtils;

import java.io.OutputStream;

/**
 * This class represents a compressed stream using given {@link CompressionMethod}.
 * It extends from the {@link OutputStream} to be able to use standard output
 * optimizations (e.g. from {@link IOUtils}).
 * <p>
 * This {@link OutputStream} does not close delegate {@link DataOutput} when
 * method {@link CompressedEntryDataOutput#close()} is invoked.
 *
 * @author Oleg Cherednik
 * @since 12.02.2020
 */
public class CompressedEntryDataOutput extends BaseDataOutput {

    public static DataOutput create(ZipEntry entry, DataOutput out) {
        CompressionMethod compressionMethod = entry.getCompressionMethod();
        CompressionLevel compressionLevel = entry.getCompressionLevel();

        if (compressionMethod == CompressionMethod.STORE)
            return new StoreEntryDataOutput(out);
        if (compressionMethod == CompressionMethod.DEFLATE)
            return new DeflateEntryDataOutput(out, compressionLevel);
        if (compressionMethod == CompressionMethod.BZIP2)
            return new Bzip2EntryDataOutput(out, compressionLevel);
        if (compressionMethod == CompressionMethod.LZMA)
            return new LzmaEntryDataOutput(out,
                                           compressionLevel,
                                           entry.isLzmaEosMarker(),
                                           entry.getUncompressedSize());
        if (compressionMethod == CompressionMethod.ZSTD)
            return new ZstdEntryDataOutput(out, compressionLevel);

        throw new CompressionNotSupportedException(compressionMethod);
    }

    protected CompressedEntryDataOutput(DataOutput out) {
        super(out);
    }

}
