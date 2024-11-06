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
package ru.olegcherednik.zip4jvm.io.writers.entry;

import ru.olegcherednik.zip4jvm.io.out.data.ChecksumCalcDataOutput;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.data.EncryptedDataOutput;
import ru.olegcherednik.zip4jvm.io.out.data.SizeCalcDataOutput;
import ru.olegcherednik.zip4jvm.io.out.data.UncloseableDataOutput;
import ru.olegcherednik.zip4jvm.io.out.data.compressed.CompressedEntryDataOutput;
import ru.olegcherednik.zip4jvm.io.writers.LocalFileHeaderWriter;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.builders.LocalFileHeaderBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.UUID;

import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_ENTRY_SIZE;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_LOCAL_FILE_HEADER_OFFS;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_TOTAL_DISKS;

/**
 * @author Oleg Cherednik
 * @since 06.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@SuppressWarnings("PMD.CloseResource")
public class ZipEntryWriter implements Writer {

    private static final String COMPRESSED_DATA =
            ZipEntryWriter.class.getSimpleName() + ".entryCompressedDataOffs";

    protected final ZipEntry zipEntry;

    public static ZipEntryWriter create(ZipEntry entry, Path tempDir) {
        if (entry.isDataDescriptorAvailable())
            return new ZipEntryWithDataDescriptorWriter(entry);

        Path dir = tempDir.resolve(UUID.randomUUID().toString());
        return new ZipEntryWithoutDataDescriptorWriter(entry, dir);
    }

    protected final void writeLocalFileHeader(DataOutput out) throws IOException {
        zipEntry.setLocalFileHeaderRelativeOffs(out.getDiskOffs());
        LocalFileHeader localFileHeader = new LocalFileHeaderBuilder(zipEntry).build();
        new LocalFileHeaderWriter(localFileHeader).write(out);
    }

    protected final void updateZip64() {
        if (zipEntry.getCompressedSize() > MAX_ENTRY_SIZE)
            zipEntry.setZip64(true);
        if (zipEntry.getUncompressedSize() > MAX_ENTRY_SIZE)
            zipEntry.setZip64(true);
        if (zipEntry.getDiskNo() > MAX_TOTAL_DISKS)
            zipEntry.setZip64(true);
        if (zipEntry.getLocalFileHeaderRelativeOffs() > MAX_LOCAL_FILE_HEADER_OFFS)
            zipEntry.setZip64(true);
    }

    protected final void writePayload(DataOutput out) throws IOException {
        out.mark(COMPRESSED_DATA);

//        SizeCalcDataOutput scdo1 = SizeCalcDataOutput.compressedSize(zipEntry, out);
        out = new UncloseableDataOutput(out);
        out = EncryptedDataOutput.create(zipEntry, out);
        out = CompressedEntryDataOutput.create(zipEntry, out);
        out = SizeCalcDataOutput.uncompressedSize(zipEntry, out);
        out = ChecksumCalcDataOutput.checksum(zipEntry, out);

        try (InputStream in = zipEntry.getInputStream();
             OutputStream os = out) {
            IOUtils.copyLarge(in, os);
        }

        zipEntry.setCompressedSize(out.getWrittenBytesAmount(COMPRESSED_DATA));
    }

    // ---------- Writer ----------

    @Override
    public void write(DataOutput out) throws IOException {
        zipEntry.setDiskNo(out.getDiskNo());
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return '+' + zipEntry.getFileName();
    }

}
