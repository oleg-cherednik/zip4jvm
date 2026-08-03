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

import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.DataDescriptorChoose;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.charset.Charsets;
import ru.olegcherednik.zip4jvm.model.settings.CompressionLevelEnum;
import ru.olegcherednik.zip4jvm.model.settings.DataDescriptorEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.utils.time.DosTimeConverter;

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
        int dosLastModifiedTime = DosTimeConverter.javaToDosTime(System.currentTimeMillis());
        byte[] buf = symlinkTargetRelativePath.getBytes(Charsets.UTF_8);
        Compression compression = Compression.STORE;
        Encryption encryption = Encryption.OFF;
        DataDescriptorEnum dataDescriptor = entrySettings.getDataDescriptor();
        boolean dataDescriptorAvailable = dataDescriptor == DataDescriptorEnum.AUTO
                || DataDescriptorChoose.isInclude(compression, encryption, dataDescriptor);

        ZipEntry zipEntry = new RegularFileZipEntry(symlinkName,
                                                    dosLastModifiedTime,
                                                    ExternalFileAttributes.symlink(symlinkTarget),
                                                    AesVersion.of(entrySettings.getAesVersion()),
                                                    compression,
                                                    CompressionLevelEnum.NORMAL,
                                                    encryption);

        zipEntry.setDataDescriptorAvailable(dataDescriptorAvailable);
        zipEntry.setComment(entrySettings.getComment());
        zipEntry.setUtf8(entrySettings.isUtf8());
        zipEntry.setUncompressedSize(buf.length);
        zipEntry.setInputStreamSupplier(() -> new ByteArrayInputStream(buf));

        return zipEntry;
    }

}
