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
package ru.olegcherednik.zip4jvm.model.entry;

import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.AesVersionEnum;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.time.DosTimestampConverterUtils;

import lombok.Builder;

/**
 * @author Oleg Cherednik
 * @since 16.02.2025
 */
@Builder
class EntryBasedZipEntryBuilder {

    private final ZipFile.Entry entry;
    private final ZipEntrySettings entrySettings;

    public ZipEntry build() {
        ZipEntry zipEntry = createZipEntry();
        zipEntry.setComment(entrySettings.getComment());
        zipEntry.setUtf8(entrySettings.isUtf8());
        return zipEntry;
    }

    private ZipEntry createZipEntry() {
        if (entry.isSymlink())
            return createSymlinkEntry();
        if (entry.isDir())
            return createEmptyDirectoryEntry();
        return createRegularFileEntry();
    }

    private ZipEntry createSymlinkEntry() {
        String symlinkName = ZipUtils.getFileName(entry);
        int lastModifiedTime = DosTimestampConverterUtils.javaToDosTime(entry.getLastModifiedTime());

        ZipEntry zipEntry = new RegularFileZipEntry(symlinkName,
                                                    lastModifiedTime,
                                                    entry.getExternalFileAttributes(),
                                                    AesVersionEnum.AUTO.getVersion(),
                                                    CompressionMethod.STORE,
                                                    CompressionLevel.NORMAL,
                                                    EncryptionMethod.OFF);

        zipEntry.setDataDescriptorAvailable(true);
        zipEntry.setComment(entrySettings.getComment());
        zipEntry.setUtf8(entrySettings.isUtf8());
        zipEntry.setUncompressedSize(entry.getUncompressedSize());
        zipEntry.setInputStreamSup(entry::getInputStream);

        return zipEntry;
    }

    private ZipEntry createEmptyDirectoryEntry() {
        String dirName = ZipUtils.getFileName(entry);
        int lastModifiedTime = DosTimestampConverterUtils.javaToDosTime(entry.getLastModifiedTime());
        ExternalFileAttributes externalFileAttributes = entry.getExternalFileAttributes();
        return new EmptyDirectoryZipEntry(dirName, lastModifiedTime, externalFileAttributes);
    }

    private ZipEntry createRegularFileEntry() {
        int lastModifiedTime = DosTimestampConverterUtils.javaToDosTime(entry.getLastModifiedTime());

        CompressionMethod compressionMethod =
                entry.getUncompressedSize() == 0 ? CompressionMethod.STORE
                                                 : entrySettings.getCompression().getMethod();
        EncryptionMethod encryptionMethod = entrySettings.getEncryption().getMethod();
        boolean dataDescriptorAvailable =
                entrySettings.getDataDescriptor().isIncludeDataDescriptor(compressionMethod, encryptionMethod);

        RegularFileZipEntry zipEntry = new RegularFileZipEntry(ZipUtils.getFileName(entry),
                                                               lastModifiedTime,
                                                               entry.getExternalFileAttributes(),
                                                               entrySettings.getAesVersion().getVersion(),
                                                               compressionMethod,
                                                               entrySettings.getCompressionLevel(),
                                                               encryptionMethod);

        zipEntry.setDataDescriptorAvailable(dataDescriptorAvailable);
        zipEntry.setZip64(entrySettings.isZip64());
        zipEntry.setPassword(entrySettings.getPassword());
        zipEntry.setComment(entrySettings.getComment());
        zipEntry.setUtf8(entrySettings.isUtf8());
        zipEntry.setUncompressedSize(entry.getUncompressedSize());
        zipEntry.setInputStreamSup(entry::getInputStream);

        return zipEntry;
    }

}
