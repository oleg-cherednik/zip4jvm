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

import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.Version;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 27.08.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderBuilder {

    public static final long LOOK_IN_DATA_DESCRIPTOR = 0;
    public static final long LOOK_IN_EXTRA_FIELD = Zip64.LIMIT_DWORD;

    private final ZipEntry zipEntry;

    public LocalFileHeader build() {
        LocalFileHeader localFileHeader = new LocalFileHeader();
        localFileHeader.setVersionToExtract(Version.of(Version.FileSystem.MS_DOS_OS2_NT_FAT, 20));
        localFileHeader.setGeneralPurposeFlag(createGeneralPurposeFlag());
        localFileHeader.setCompressionMethod(zipEntry.getCompressionMethodForBuilder());
        localFileHeader.setLastModifiedTime(zipEntry.getLastModifiedTime());
        localFileHeader.setCrc32(getCrc32());
        localFileHeader.setCompressedSize(getSize(zipEntry.getCompressedSize()));
        localFileHeader.setUncompressedSize(getSize(zipEntry.getUncompressedSize()));
        localFileHeader.setFileName(zipEntry.getFileName());
        localFileHeader.setExtraField(createExtraField());
        return localFileHeader;
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
        if (zipEntry.isDataDescriptorAvailable())
            return Zip64.ExtendedInfo.NULL;
        if (zipEntry.isZip64())
            return Zip64.ExtendedInfo.builder()
                                     .compressedSize(zipEntry.getCompressedSize())
                                     .uncompressedSize(zipEntry.getUncompressedSize()).build();
        return Zip64.ExtendedInfo.NULL;
    }

    private long getCrc32() {
        if (zipEntry.isDataDescriptorAvailable())
            return LOOK_IN_DATA_DESCRIPTOR;
        return zipEntry.getEncryptionMethod().getChecksum(zipEntry);
    }

    private long getSize(long size) {
        if (zipEntry.isDataDescriptorAvailable())
            return LOOK_IN_DATA_DESCRIPTOR;
        if (zipEntry.isZip64())
            return ZipModel.LOOK_IN_EXTRA_FIELD;
        return size;
    }

}
