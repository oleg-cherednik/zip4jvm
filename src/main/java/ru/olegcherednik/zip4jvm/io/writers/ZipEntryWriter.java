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
package ru.olegcherednik.zip4jvm.io.writers;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.data.EncryptedDataOutput;
import ru.olegcherednik.zip4jvm.io.out.entry.PayloadCalculationOutputStream;
import ru.olegcherednik.zip4jvm.io.out.entry.compressed.CompressedEntryOutputStream;
import ru.olegcherednik.zip4jvm.io.out.entry.xxx.DataDescriptorOut;
import ru.olegcherednik.zip4jvm.io.out.entry.xxx.LocalFileHeaderOut;
import ru.olegcherednik.zip4jvm.io.out.entry.xxx.UpdateZip64;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 26.02.2023
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.CloseResource")
public final class ZipEntryWriter implements Writer {

    private static final String COMPRESSED_DATA =
            ZipEntryWriter.class.getSimpleName() + ".entryCompressedDataOffs";

    private final ZipEntry zipEntry;

    @Override
    public void write(DataOutput out) throws IOException {
        // 1. compression
        // 2. encryption
        zipEntry.setDiskNo(out.getDiskNo());

        /*
        The series of
        [local file header]
        [encryption header]
        [file data]
        [data descriptor]
         */

        new LocalFileHeaderOut().write(zipEntry, out);
        foo(out);
        new UpdateZip64().update(zipEntry);
        new DataDescriptorOut().write(zipEntry, out);
    }

    private void foo(DataOutput out) throws IOException {
        out.mark(COMPRESSED_DATA);

        EncryptedDataOutput encryptedDataOutput = EncryptedDataOutput.create(zipEntry, out);
        CompressedEntryOutputStream cos = CompressedEntryOutputStream.create(zipEntry, encryptedDataOutput);

        encryptedDataOutput.writeEncryptionHeader();

        try (InputStream in = zipEntry.getInputStream();
             PayloadCalculationOutputStream os = new PayloadCalculationOutputStream(zipEntry, cos)) {
            IOUtils.copyLarge(in, os);
        }

        encryptedDataOutput.encodingAccomplished();
        zipEntry.setCompressedSize(out.getWrittenBytesAmount(COMPRESSED_DATA));
    }

    @Override
    public String toString() {
        return '+' + zipEntry.getFileName();
    }

}
