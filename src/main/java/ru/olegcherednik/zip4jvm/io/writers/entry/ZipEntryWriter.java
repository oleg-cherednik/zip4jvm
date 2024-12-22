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

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.compressed.CompressedEntryDataOutput;
import ru.olegcherednik.zip4jvm.io.out.decorators.ChecksumCalcDataOutput;
import ru.olegcherednik.zip4jvm.io.out.decorators.SizeCalcDataOutput;
import ru.olegcherednik.zip4jvm.io.out.decorators.UncloseableDataOutput;
import ru.olegcherednik.zip4jvm.io.out.encrypted.EncryptedDataOutput;
import ru.olegcherednik.zip4jvm.io.writers.LocalFileHeaderWriter;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.builders.LocalFileHeaderBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
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

    protected final ZipEntry zipEntry;

    public static ZipEntryWriter create(ZipEntry entry, Path tempDir) {
        if (entry.isDataDescriptorAvailable())
            return new ZipEntryWithDataDescriptorWriter(entry);

        Path dir = tempDir.resolve(UUID.randomUUID().toString());
        return new ZipEntryWithoutDataDescriptorWriter(entry, dir);
    }

    protected final void writeLocalFileHeader(DataOutput out) throws IOException {
        zipEntry.setLocalFileHeaderDiskOffs(out.getDiskOffs());
        // TODO add setLocalFileHeaderAbsOffs()
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
        if (zipEntry.getLocalFileHeaderDiskOffs() > MAX_LOCAL_FILE_HEADER_OFFS)
            zipEntry.setZip64(true);
    }

    protected final void writePayload(DataOutput out) throws IOException {
        out = new UncloseableDataOutput(out);
        out = SizeCalcDataOutput.compressedSize(zipEntry, out);
        out = EncryptedDataOutput.create(zipEntry, out);
        out = CompressedEntryDataOutput.create(zipEntry, out);
        out = SizeCalcDataOutput.uncompressedSize(zipEntry, out);
        out = ChecksumCalcDataOutput.checksum(zipEntry, out);

        ZipUtils.copyLarge(zipEntry.createInputStream(), out);
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
