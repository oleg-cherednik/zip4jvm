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

import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.DataDescriptorEnum;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.charset.Charsets;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.utils.time.DosTimestampConverterUtils;

import lombok.Builder;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 16.02.2025
 */
@Builder
class SymlinkZipEntryBuilder {

    private final Path symlinkTarget;
    private final String symlinkTargetRelativePath;
    private final String symlinkName;
    private final ZipEntrySettings entrySettings;

    public ZipEntry build() {
        int dosLastModifiedTime = DosTimestampConverterUtils.javaToDosTime(System.currentTimeMillis());
        byte[] buf = symlinkTargetRelativePath.getBytes(Charsets.UTF_8);
        CompressionMethod compressionMethod = CompressionMethod.STORE;
        EncryptionMethod encryptionMethod = EncryptionMethod.OFF;
        DataDescriptorEnum dataDescriptorAvailability = entrySettings.getDataDescriptor();
        boolean dataDescriptorAvailable = dataDescriptorAvailability == DataDescriptorEnum.AUTO
                || dataDescriptorAvailability.isIncludeDataDescriptor(compressionMethod, encryptionMethod);
        AesVersion aesVersion = entrySettings.getAesVersion().getVersion();

        ZipEntry zipEntry = new RegularFileZipEntry(symlinkName,
                                                    dosLastModifiedTime,
                                                    ExternalFileAttributes.symlink(symlinkTarget),
                                                    aesVersion,
                                                    compressionMethod,
                                                    CompressionLevel.NORMAL,
                                                    encryptionMethod);

        zipEntry.setDataDescriptorAvailable(dataDescriptorAvailable);
        zipEntry.setComment(entrySettings.getComment());
        zipEntry.setUtf8(entrySettings.isUtf8());
        zipEntry.setUncompressedSize(buf.length);
        zipEntry.setInputStreamSup(() -> new ByteArrayInputStream(buf));

        return zipEntry;
    }

}
