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
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.DataDescriptorChoose;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.charset.CharsetProvider;
import ru.olegcherednik.zip4jvm.model.settings.CompressionLevelEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;
import ru.olegcherednik.zip4jvm.utils.time.DosTimeConverter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 04.09.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipEntryBuilder {

    public static ZipEntry build(ZipFile.Entry entry, ZipEntrySettings entrySettings) {
        return new EntryBasedZipEntryBuilder(entry, entrySettings).build();
    }

    public static ZipEntry build(CentralDirectory.FileHeader fileHeader,
                                 SrcZip srcZip,
                                 CharsetProvider charsetProvider) {
        return new FileHeaderBasedZipEntryBuilder(fileHeader, srcZip, charsetProvider).build();
    }

    public static ZipEntry symlink(Path symlinkTarget,
                                   String symlinkTargetRelativePath,
                                   String symlinkName,
                                   ZipEntrySettings entrySettings) {
        return new SymlinkZipEntryBuilder(symlinkTarget, symlinkTargetRelativePath, symlinkName, entrySettings).build();
    }

    public static ZipEntry emptyDirectory(Path dir, String dirName, ZipEntrySettings entrySettings) {
        long lastModifiedTime = Quietly.doRuntime(() -> Files.getLastModifiedTime(dir).toMillis());
        int dosLastModifiedTime = DosTimeConverter.javaToDosTime(lastModifiedTime);
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.directory(dir);
        EmptyDirectoryZipEntry zipEntry = new EmptyDirectoryZipEntry(dirName,
                                                                     dosLastModifiedTime,
                                                                     externalFileAttributes);
        zipEntry.setComment(entrySettings.getComment());
        zipEntry.setUtf8(entrySettings.isUtf8());
        return zipEntry;
    }

    public static ZipEntry regularFile(Path file, String fileName, ZipEntrySettings entrySettings) {
        long lastModifiedTime = Quietly.doRuntime(() -> Files.getLastModifiedTime(file).toMillis());
        int dosLastModifiedTime = DosTimeConverter.javaToDosTime(lastModifiedTime);
        long size = Quietly.doRuntime(() -> Files.size(file));

        Compression compression = size == 0 ? Compression.STORE
                                            : Compression.of(entrySettings.getCompression());
        CompressionLevelEnum compressionLevel = entrySettings.getCompressionLevel();
        Encryption encryption = Encryption.of(entrySettings.getEncryption());
        boolean dataDescriptorAvailable =
                DataDescriptorChoose.isInclude(compression, encryption, entrySettings.getDataDescriptor());

        RegularFileZipEntry zipEntry = new RegularFileZipEntry(fileName,
                                                               dosLastModifiedTime,
                                                               ExternalFileAttributes.regularFile(file),
                                                               AesVersion.of(entrySettings.getAesVersion()),
                                                               compression,
                                                               compressionLevel,
                                                               encryption);

        zipEntry.setDataDescriptorAvailable(dataDescriptorAvailable);
        zipEntry.setZip64(entrySettings.isZip64());
        zipEntry.setPassword(entrySettings.getPassword());
        zipEntry.setComment(entrySettings.getComment());
        zipEntry.setUtf8(entrySettings.isUtf8());
        zipEntry.setStrongEncryption(encryption.isStrong());
        zipEntry.setUncompressedSize(size);
        zipEntry.setInputStreamSupplier(() -> PathUtils.newInputStream(file));

        return zipEntry;
    }

}
