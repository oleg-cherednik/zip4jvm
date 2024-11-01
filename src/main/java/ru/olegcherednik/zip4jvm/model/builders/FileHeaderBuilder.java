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
package ru.olegcherednik.zip4jvm.model.builders;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.Version;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_LOCAL_FILE_HEADER_OFFS;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_TOTAL_DISKS;
import static ru.olegcherednik.zip4jvm.model.builders.LocalFileHeaderBuilder.LOOK_IN_EXTRA_FIELD;

/**
 * @author Oleg Cherednik
 * @since 30.08.2019
 */
@RequiredArgsConstructor
final class FileHeaderBuilder {

    private final ZipEntry zipEntry;

    public CentralDirectory.FileHeader build() throws IOException {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        fileHeader.setVersionMadeBy(Version.of(Version.FileSystem.MS_DOS_OS2_NT_FAT, 20));
        fileHeader.setVersionToExtract(Version.of(Version.FileSystem.MS_DOS_OS2_NT_FAT, 20));
        fileHeader.setGeneralPurposeFlag(createGeneralPurposeFlag());
        fileHeader.setCompressionMethod(zipEntry.getCompressionMethodForBuilder());
        fileHeader.setLastModifiedTime(zipEntry.getLastModifiedTime());
        fileHeader.setCrc32(zipEntry.getEncryptionMethod().getChecksum(zipEntry));
        fileHeader.setCompressedSize(getSize(zipEntry.getCompressedSize()));
        fileHeader.setUncompressedSize(getSize(zipEntry.getUncompressedSize()));
        fileHeader.setCommentLength(0);
        fileHeader.setDiskNo(getDisk());
        fileHeader.setInternalFileAttributes(zipEntry.getInternalFileAttributes());
        fileHeader.setExternalFileAttributes(zipEntry.getExternalFileAttributes());
        fileHeader.setLocalFileHeaderRelativeOffs(getLocalFileHeaderRelativeOffs());
        fileHeader.setFileName(zipEntry.getFileName());
        fileHeader.setExtraField(createExtraField());
        fileHeader.setComment(zipEntry.getComment());

        return fileHeader;
    }

    private GeneralPurposeFlag createGeneralPurposeFlag() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        generalPurposeFlag.setCompressionLevel(zipEntry.getCompressionLevel());
        generalPurposeFlag.setDataDescriptorAvailable(zipEntry.isDataDescriptorAvailable());
        generalPurposeFlag.setUtf8(zipEntry.isUtf8());
        generalPurposeFlag.setEncrypted(zipEntry.isEncrypted());
        generalPurposeFlag.setStrongEncryption(zipEntry.isStrongEncryption());
        generalPurposeFlag.setLzmaEosMarker(zipEntry.isLzmaEosMarker());

        return generalPurposeFlag;
    }

    private PkwareExtraField createExtraField() {
        return PkwareExtraField.builder()
                               .addRecord(createExtendedInfo())
                               .addRecord(new AesExtraDataRecordBuilder(zipEntry).build()).build();
    }

    private Zip64.ExtendedInfo createExtendedInfo() {
        if (zipEntry.isZip64())
            return Zip64.ExtendedInfo.builder()
                                     .compressedSize(zipEntry.getCompressedSize())
                                     .uncompressedSize(zipEntry.getUncompressedSize())
                                     .diskNo(zipEntry.getDiskNo())
                                     .localFileHeaderRelativeOffs(zipEntry.getLocalFileHeaderRelativeOffs()).build();
        return Zip64.ExtendedInfo.NULL;
    }

    private long getSize(long size) {
        return zipEntry.isZip64() ? LOOK_IN_EXTRA_FIELD : size;
    }

    private int getDisk() {
        return zipEntry.isZip64() ? MAX_TOTAL_DISKS : zipEntry.getDiskNo();
    }

    private long getLocalFileHeaderRelativeOffs() {
        return zipEntry.isZip64() ? MAX_LOCAL_FILE_HEADER_OFFS : zipEntry.getLocalFileHeaderRelativeOffs();
    }

}
