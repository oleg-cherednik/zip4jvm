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
import ru.olegcherednik.zip4jvm.io.out.data.EncoderDataOutput;
import ru.olegcherednik.zip4jvm.io.out.entry.EntryMetadataOutputStream;
import ru.olegcherednik.zip4jvm.io.out.entry.PayloadCalculationOutputStream;
import ru.olegcherednik.zip4jvm.io.out.entry.SequenceOutputStream;
import ru.olegcherednik.zip4jvm.io.out.entry.encrypted.EncryptedEntryOutputStream;
import ru.olegcherednik.zip4jvm.io.out.entry.xxx.LocalFileHeaderOut;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static ru.olegcherednik.zip4jvm.io.out.entry.EntryMetadataOutputStream.COMPRESSED_DATA;

/**
 * @author Oleg Cherednik
 * @since 26.02.2023
 */
@RequiredArgsConstructor
public final class ZipEntryWriter implements Writer {

    private final ZipEntry zipEntry;

    @Override
    public void write(DataOutput out) throws IOException {
        EncoderDataOutput encoderDataOutput = new EncoderDataOutput(zipEntry.createEncoder(), out);
        EntryMetadataOutputStream emos = new EntryMetadataOutputStream(zipEntry, out);
        EncryptedEntryOutputStream eos = EncryptedEntryOutputStream.create(zipEntry, encoderDataOutput, emos);
        PayloadCalculationOutputStream os = new PayloadCalculationOutputStream(zipEntry, eos);

        zipEntry.setDiskNo(out.getDiskNo());

        new LocalFileHeaderOut().write(zipEntry, out);
        out.mark(COMPRESSED_DATA);
        encoderDataOutput.writeEncryptionHeader();

        try (InputStream in = zipEntry.getInputStream();
             SequenceOutputStream sos = new SequenceOutputStream(os)) {

            IOUtils.copyLarge(in, sos);

            // zipEntry.setCompressedSize(out.getWrittenBytesAmount(COMPRESSED_DATA));
            // updateZip64();
            // writeDataDescriptor();

            int a = 0;
            a++;
        }

//        ZipUtils.copyLarge(zipEntry.getInputStream(), sos);
    }

    @Override
    public String toString() {
        return '+' + zipEntry.getFileName();
    }
}
