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
package ru.olegcherednik.zip4jvm.io.out.entry;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.data.DecoderDataOutput;
import ru.olegcherednik.zip4jvm.io.out.data.DecoderDataOutputDecorator;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 12.02.2020
 */
public abstract class EntryOutputStream extends EntryMetadataOutputStream {

    protected final DecoderDataOutput out;

    public static EntryOutputStream create(ZipEntry zipEntry, ZipModel zipModel, DataOutput out) throws IOException {
        EntryOutputStream os = createOutputStream(zipEntry, out);

        // TODO move it to the separate method
        zipModel.addEntry(zipEntry);
        zipEntry.setLocalFileHeaderRelativeOffs(out.getRelativeOffs());

        os.writeLocalFileHeader();
        os.writeEncryptionHeader();
        return os;
    }

    private static EntryOutputStream createOutputStream(ZipEntry zipEntry, DataOutput out) throws IOException {
        CompressionMethod compressionMethod = zipEntry.getCompressionMethod();
        zipEntry.setDiskNo(out.getDiskNo());

        if (compressionMethod == CompressionMethod.STORE)
            return new StoreEntryOutputStream(zipEntry, out);
        if (compressionMethod == CompressionMethod.DEFLATE)
            return new DeflateEntryOutputStream(zipEntry, out);
        if (compressionMethod == CompressionMethod.BZIP2)
            return new Bzip2EntryOutputStream(zipEntry, out);
        if (compressionMethod == CompressionMethod.LZMA)
            return new LzmaEntryOutputStream(zipEntry, out);
        if (compressionMethod == CompressionMethod.ZSTD)
            return new ZstdEntryOutputStream(zipEntry, out);

        throw new Zip4jvmException("Compression is not supported: " + compressionMethod);
    }

    protected EntryOutputStream(ZipEntry zipEntry, DataOutput out) {
        super(zipEntry, out);
        this.out = new DecoderDataOutputDecorator(out, zipEntry.createEncoder());
    }

    private void writeEncryptionHeader() throws IOException {
        out.writeEncryptionHeader();
    }

    @Override
    public void close() throws IOException {
        out.encodingAccomplished();
        super.close();
    }
}
