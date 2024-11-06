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

import ru.olegcherednik.zip4jvm.io.out.DataOutputStream;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.data.SolidDataOutput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.ChecksumUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 26.02.2023
 */
@SuppressWarnings("PMD.CloseResource")
final class ZipEntryWithoutDataDescriptorWriter extends ZipEntryWriter {

    private final Path tempDir;

    ZipEntryWithoutDataDescriptorWriter(ZipEntry zipEntry, Path tempDir) {
        super(zipEntry);
        this.tempDir = tempDir;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        super.write(out);

        Path tempFile = tempDir.resolve(zipEntry.getFileName());
        Files.deleteIfExists(tempFile);

        zipEntry.setChecksum(ChecksumUtils.crc32(zipEntry.getInputStream()));

        try (SolidDataOutput tmpOut = new SolidDataOutput(out.getByteOrder(), tempFile)) {
            writePayload(tmpOut);
        }

        writeLocalFileHeader(out);

        try (InputStream in = Files.newInputStream(tempFile)) {
            OutputStream os = new DataOutputStream(out);
            IOUtils.copyLarge(in, new DataOutputStream(out));
            os.flush();
        }

        updateZip64();
        FileUtils.deleteQuietly(tempDir.toFile());
    }

}
