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
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 12.02.2020
 */
public abstract class EntryOutputStream extends OutputStream {

    protected final ZipEntry zipEntry;
    protected final DataOutput out;
    protected final DecoderDataOutput decoderDataOutput;
    protected final EntryMetadataOutputStream emos;

    public static EntryOutputStream create(ZipEntry zipEntry, DataOutput out) throws IOException {
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

        throw new Zip4jvmException("Compression '%s' is not supported", compressionMethod);
    }

    protected EntryOutputStream(ZipEntry zipEntry, DataOutput out) {
        this.zipEntry = zipEntry;
        this.out = out;
        decoderDataOutput = new DecoderDataOutputDecorator(out, zipEntry.createEncoder());
        emos = new EntryMetadataOutputStream(zipEntry, out);
    }

    public final void writeLocalFileHeader() throws IOException {
        emos.writeLocalFileHeader();
    }

    public final void writeEncryptionHeader() throws IOException {
        decoderDataOutput.writeEncryptionHeader();
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
        decoderDataOutput.encodingAccomplished();
        emos.close();
    }

    @Override
    public String toString() {
        return out.toString();
    }
}
