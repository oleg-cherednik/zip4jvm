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

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.writers.DataDescriptorWriter;
import ru.olegcherednik.zip4jvm.io.writers.LocalFileHeaderWriter;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.builders.LocalFileHeaderBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_ENTRY_SIZE;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_LOCAL_FILE_HEADER_OFFS;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_TOTAL_DISKS;

/**
 * This stream writes all {@link ZipEntry} related metadata like {@link DataDescriptor}. These data are not encrypted;
 * therefore this stream cannot be used to write {@link ZipEntry} payload (that could be encrypted).
 *
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
public final class EntryMetadataOutputStream extends OutputStream {

    private static final String COMPRESSED_DATA =
            EntryMetadataOutputStream.class.getSimpleName() + ".entryCompressedDataOffs";

    protected final ZipEntry zipEntry;
    private final Checksum checksum = new CRC32();

    private final DataOutput out;

    private long uncompressedSize;

    public EntryMetadataOutputStream(ZipEntry zipEntry, DataOutput out) {
        this.zipEntry = zipEntry;
        this.out = out;
    }

    public void writeLocalFileHeader() throws IOException {
        zipEntry.setLocalFileHeaderRelativeOffs(out.getRelativeOffs());
        LocalFileHeader localFileHeader = new LocalFileHeaderBuilder(zipEntry).build();
        new LocalFileHeaderWriter(localFileHeader).write(out);
        out.mark(COMPRESSED_DATA);
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] { (byte) b }, 0, 1);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        checksum.update(buf, offs, len);
        uncompressedSize += Math.max(0, len);
    }

    @Override
    public void close() throws IOException {
        zipEntry.setChecksum(checksum.getValue());
        zipEntry.setUncompressedSize(uncompressedSize);
        zipEntry.setCompressedSize(out.getWrittenBytesAmount(COMPRESSED_DATA));
        updateZip64();
        writeDataDescriptor();
    }

    private void updateZip64() {
        if (zipEntry.getCompressedSize() > MAX_ENTRY_SIZE)
            zipEntry.setZip64(true);
        if (zipEntry.getUncompressedSize() > MAX_ENTRY_SIZE)
            zipEntry.setZip64(true);
        if (zipEntry.getDiskNo() > MAX_TOTAL_DISKS)
            zipEntry.setZip64(true);
        if (zipEntry.getLocalFileHeaderRelativeOffs() > MAX_LOCAL_FILE_HEADER_OFFS)
            zipEntry.setZip64(true);
    }

    private void writeDataDescriptor() throws IOException {
        if (!zipEntry.isDataDescriptorAvailable())
            return;

        DataDescriptor dataDescriptor = new DataDescriptor(checksum.getValue(),
                                                           zipEntry.getCompressedSize(),
                                                           zipEntry.getUncompressedSize());
        DataDescriptorWriter.get(zipEntry.isZip64(), dataDescriptor).write(out);
    }

    @Override
    public String toString() {
        return out.toString();
    }

}
