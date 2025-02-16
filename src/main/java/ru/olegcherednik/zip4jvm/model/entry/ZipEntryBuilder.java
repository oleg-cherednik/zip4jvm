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
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;
import ru.olegcherednik.zip4jvm.utils.time.DosTimestampConverterUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

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
                                 Function<Charset, Charset> charsetCustomizer,
                                 boolean alt) {
        return new FileHeaderBasedZipEntryBuilder(fileHeader, srcZip, charsetCustomizer).build();
    }

    public static ZipEntry symlink(Path symlinkTarget,
                                   String symlinkTargetRelativePath,
                                   String symlinkName,
                                   ZipEntrySettings entrySettings) {
        return new SymlinkZipEntryBuilder(symlinkTarget, symlinkTargetRelativePath, symlinkName, entrySettings).build();
    }

    public static ZipEntry emptyDirectory(Path dir, String dirName, ZipEntrySettings entrySettings) {
        long lastModifiedTime = Quietly.doRuntime(() -> Files.getLastModifiedTime(dir).toMillis());
        int dosLastModifiedTime = DosTimestampConverterUtils.javaToDosTime(lastModifiedTime);
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
        int dosLastModifiedTime = DosTimestampConverterUtils.javaToDosTime(lastModifiedTime);
        long size = Quietly.doRuntime(() -> Files.size(file));

        CompressionMethod compressionMethod = size == 0 ? CompressionMethod.STORE
                                                        : entrySettings.getCompression().getMethod();
        CompressionLevel compressionLevel = entrySettings.getCompressionLevel();
        EncryptionMethod encryptionMethod = entrySettings.getEncryption().getMethod();
        boolean dataDescriptorAvailable =
                entrySettings.getDataDescriptor().isIncludeDataDescriptor(compressionMethod, encryptionMethod);
        AesVersion aesVersion = entrySettings.getAesVersion().getVersion();

        RegularFileZipEntry zipEntry = new RegularFileZipEntry(fileName,
                                                               dosLastModifiedTime,
                                                               ExternalFileAttributes.regularFile(file),
                                                               aesVersion,
                                                               compressionMethod,
                                                               compressionLevel,
                                                               encryptionMethod);

        zipEntry.setDataDescriptorAvailable(dataDescriptorAvailable);
        zipEntry.setZip64(entrySettings.isZip64());
        zipEntry.setPassword(entrySettings.getPassword());
        zipEntry.setComment(entrySettings.getComment());
        zipEntry.setUtf8(entrySettings.isUtf8());
        zipEntry.setUncompressedSize(size);
        zipEntry.setInputStreamSup(() -> Files.newInputStream(file));

        return zipEntry;
    }

}
