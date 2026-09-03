/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.DataDescriptorChoose;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.settings.CompressionLevelEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.time.DosTimeConverter;

import lombok.Builder;

/**
 * @author Oleg Cherednik
 * @since 16.02.2025
 */
@Builder
class EntryBasedZipEntryBuilder {

    private final ZipFile.Entry entry;
    private final ZipEntrySettings entrySettings;

    ZipEntry build() {
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
        int lastModifiedTime = DosTimeConverter.javaToDosTime(entry.getLastModifiedTime());

        ZipEntry zipEntry = new RegularFileZipEntry(symlinkName,
                                                    lastModifiedTime,
                                                    entry.getExternalFileAttributes(),
                                                    AesVersion.of(entrySettings.getAesVersion()),
                                                    Compression.STORE,
                                                    CompressionLevelEnum.NORMAL,
                                                    Encryption.OFF);

        zipEntry.setDataDescriptorAvailable(true);
        zipEntry.setComment(entrySettings.getComment());
        zipEntry.setUtf8(entrySettings.isUtf8());
        zipEntry.setUncompressedSize(entry.getUncompressedSize());
        zipEntry.setInputStreamSupplier(entry::getInputStream);

        return zipEntry;
    }

    private ZipEntry createEmptyDirectoryEntry() {
        String dirName = ZipUtils.getFileName(entry);
        int lastModifiedTime = DosTimeConverter.javaToDosTime(entry.getLastModifiedTime());
        ExternalFileAttributes externalFileAttributes = entry.getExternalFileAttributes();
        return new EmptyDirectoryZipEntry(dirName, lastModifiedTime, externalFileAttributes);
    }

    private ZipEntry createRegularFileEntry() {
        int lastModifiedTime = DosTimeConverter.javaToDosTime(entry.getLastModifiedTime());

        Compression compression =
                entry.getUncompressedSize() == 0 ? Compression.STORE
                                                 : Compression.of(entrySettings.getCompression());
        Encryption encryption = Encryption.of(entrySettings.getEncryption());
        boolean dataDescriptorAvailable =
                DataDescriptorChoose.isInclude(compression, encryption, entrySettings.getDataDescriptor());

        RegularFileZipEntry zipEntry = new RegularFileZipEntry(ZipUtils.getFileName(entry),
                                                               lastModifiedTime,
                                                               entry.getExternalFileAttributes(),
                                                               AesVersion.of(entrySettings.getAesVersion()),
                                                               compression,
                                                               entrySettings.getCompressionLevel(),
                                                               encryption);

        zipEntry.setDataDescriptorAvailable(dataDescriptorAvailable);
        zipEntry.setZip64(entrySettings.isZip64());
        zipEntry.setPassword(entrySettings.getPassword());
        zipEntry.setComment(entrySettings.getComment());
        zipEntry.setUtf8(entrySettings.isUtf8());
        zipEntry.setStrongEncryption(encryption.isStrong());
        zipEntry.setUncompressedSize(entry.getUncompressedSize());
        zipEntry.setInputStreamSupplier(entry::getInputStream);

        return zipEntry;
    }

}
