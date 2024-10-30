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

import ru.olegcherednik.zip4jvm.io.out.DataOutputStream;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.data.EncryptedDataOutput;
import ru.olegcherednik.zip4jvm.io.out.data.WriteFileDataOutput;
import ru.olegcherednik.zip4jvm.io.out.entry.PayloadCalculationOutputStream;
import ru.olegcherednik.zip4jvm.io.out.entry.compressed.CompressedEntryOutputStream;
import ru.olegcherednik.zip4jvm.io.out.entry.xxx.LocalFileHeaderOut;
import ru.olegcherednik.zip4jvm.io.out.entry.xxx.UpdateZip64;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.PureJavaCrc32;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.Checksum;

/**
 * @author Oleg Cherednik
 * @since 26.02.2023
 */
@RequiredArgsConstructor
public final class ZipEntryNoDataDescriptorWriter implements Writer {

    private static final String COMPRESSED_DATA =
            ZipEntryNoDataDescriptorWriter.class.getSimpleName() + ".entryCompressedDataOffs";

    private final ZipEntry entry;

    @Override
    public void write(DataOutput out) throws IOException {
        // 1. compression
        // 2. encryption
        entry.setDiskNo(out.getDiskNo());

        /*
        The series of
        [local file header]
        [encryption header]
        [file data]
        [data descriptor]
         */

        Path tmpFile = Paths.get("d:/zip4jvm/foo/bar/" + entry.getFileName());
        Files.deleteIfExists(tmpFile);

        byte[] data = IOUtils.toByteArray(entry.getInputStream());
        Checksum checksum = new PureJavaCrc32();
        checksum.update(data, 0, data.length);
        System.out.println("CRC32 Checksum: "+ checksum.getValue());
        entry.setChecksum(checksum.getValue());

        try (WriteFileDataOutput tmpOut = new WriteFileDataOutput()) {
            tmpOut.createFile(tmpFile);
            foo(tmpOut);
        }

        new LocalFileHeaderOut().write(entry, out);

        try (InputStream in = Files.newInputStream(tmpFile)) {
            OutputStream os = new DataOutputStream(out);
            IOUtils.copyLarge(in, new DataOutputStream(out));
            os.flush();
        }

        new UpdateZip64().update(entry);
    }

    private void foo(DataOutput out) throws IOException {
        out.mark(COMPRESSED_DATA);

        EncryptedDataOutput encryptedDataOutput = EncryptedDataOutput.create(entry, out);
        CompressedEntryOutputStream cos = CompressedEntryOutputStream.create(entry, encryptedDataOutput);

        encryptedDataOutput.writeEncryptionHeader();

        try (InputStream in = entry.getInputStream();
             PayloadCalculationOutputStream os = new PayloadCalculationOutputStream(entry, cos)) {
            IOUtils.copyLarge(in, os);
            out.close();
        }

        encryptedDataOutput.encodingAccomplished();
        entry.setCompressedSize(out.getWrittenBytesAmount(COMPRESSED_DATA));
    }

    @Override
    public String toString() {
        return '+' + entry.getFileName();
    }
}
