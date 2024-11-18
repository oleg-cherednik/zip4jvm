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

import ru.olegcherednik.zip4jvm.engine.UnzipEngine;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.readers.DataDescriptorReader;
import ru.olegcherednik.zip4jvm.io.readers.LocalFileHeaderReader;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * This writer copy existed {@link ZipEntry} block from one zip file to another as is. This block is not modified during
 * the copy.
 *
 * @author Oleg Cherednik
 * @since 12.09.2019
 */
@RequiredArgsConstructor
public class ExistedEntryWriter implements Writer {

    private final ZipModel srcZipModel;
    private final String entryName;
    private final ZipModel destZipModel;
    private final char[] password;

    // ---------- Writer ----------

    @Override
    public void write(DataOutput out) throws IOException {
        ZipEntry entry = srcZipModel.getZipEntryByFileName(entryName);
        // TODO it seems that this should not be done, because we just copy encrypted/not encrypted entry
        entry.setPassword(entry.isEncrypted() ? password : null);

        long offs = out.getDiskOffs();
        int diskNo = out.getDiskNo();

        try (DataInput in = UnzipEngine.createDataInput(srcZipModel.getSrcZip())) {
            CopyEntryInputStream is = new CopyEntryInputStream(entry, in);

            if (!destZipModel.hasEntry(entryName))
                destZipModel.addEntry(entry);

            is.copyLocalFileHeader(out);
            is.copyEncryptionHeaderAndData(out);
            is.copyDataDescriptor(out);
            // TODO probably should set compressed size here
        }

        entry.setLocalFileHeaderRelativeOffs(offs);
        entry.setDiskNo(diskNo);
    }

    @Override
    public String toString() {
        return "->" + entryName;
    }

    @RequiredArgsConstructor
    private static final class CopyEntryInputStream {

        private final ZipEntry zipEntry;
        private final DataInput in;

        public void copyLocalFileHeader(DataOutput out) throws IOException {
            long absOffs = in.convertToAbsoluteOffs(zipEntry.getDiskNo(),
                                                    zipEntry.getLocalFileHeaderRelativeOffs());
            in.seek(absOffs);
            LocalFileHeader localFileHeader = new LocalFileHeaderReader(Charsets.UNMODIFIED).read(in);
            zipEntry.setDataDescriptorAvailable(localFileHeader.getGeneralPurposeFlag().isDataDescriptorAvailable());
            new LocalFileHeaderWriter(localFileHeader).write(out);
        }

        public void copyEncryptionHeaderAndData(DataOutput out) throws IOException {
            long size = zipEntry.getCompressedSize();
            byte[] buf = new byte[1024 * 4];

            while (size > 0) {
                int n = in.read(buf, 0, (int) Math.min(buf.length, size));

                if (n == IOUtils.EOF)
                    throw new Zip4jvmException("Unexpected end of file");

                out.write(buf, 0, n);
                size -= n;
            }
        }

        public void copyDataDescriptor(DataOutput out) throws IOException {
            if (zipEntry.isDataDescriptorAvailable()) {
                DataDescriptor dataDescriptor = DataDescriptorReader.get(zipEntry.isZip64()).read(in);
                DataDescriptorWriter.get(zipEntry.isZip64(), dataDescriptor).write(out);
            }
        }

        @Override
        public String toString() {
            return ZipUtils.toString(in.getAbsOffs());
        }

    }
}
