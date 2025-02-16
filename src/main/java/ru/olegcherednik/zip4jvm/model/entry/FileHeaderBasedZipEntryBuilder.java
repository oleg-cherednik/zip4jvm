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

import ru.olegcherednik.zip4jvm.engine.unzip.UnzipExtractEngine;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.ReadBufferInputStream;
import ru.olegcherednik.zip4jvm.io.in.decorators.ChecksumCheckDataInput;
import ru.olegcherednik.zip4jvm.io.in.decorators.DataDescriptorDataInput;
import ru.olegcherednik.zip4jvm.io.in.decorators.LimitSizeDataInput;
import ru.olegcherednik.zip4jvm.io.in.decorators.SizeCheckDataInput;
import ru.olegcherednik.zip4jvm.io.in.encrypted.EncryptedDataInput;
import ru.olegcherednik.zip4jvm.io.in.file.consecutive.ConsecutiveAccessDataInput;
import ru.olegcherednik.zip4jvm.io.readers.LocalFileHeaderReader;
import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.AesVersionEnum;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import lombok.Builder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.Function;

import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_LOCAL_FILE_HEADER_OFFS;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_TOTAL_DISKS;

/**
 * @author Oleg Cherednik
 * @since 16.02.2025
 */
@Builder
class FileHeaderBasedZipEntryBuilder {

    private final CentralDirectory.FileHeader fileHeader;
    private final SrcZip srcZip;
    private final Function<Charset, Charset> charsetCustomizer;

    public ZipEntry build() {
        boolean regularFile = ZipUtils.isRegularFile(fileHeader.getFileName());
        ZipEntry zipEntry = regularFile ? createRegularFileEntry() : createEmptyDirectoryEntry();
        zipEntry.setChecksum(fileHeader.getCrc32());
        zipEntry.setUncompressedSize(getUncompressedSize());
        zipEntry.setCompressedSize(getCompressedSize());

        int diskNo = getDiskNo();
        zipEntry.setDiskNo(getDiskNo());

        long localFileHeaderDiskOffs = getLocalFileHeaderOffs();
        zipEntry.setLocalFileHeaderDiskOffs(localFileHeaderDiskOffs);
        zipEntry.setLocalFileHeaderAbsOffs(srcZip.getAbsOffs(diskNo, localFileHeaderDiskOffs));

        return zipEntry;
    }

    private ZipEntry createRegularFileEntry() {
        String fileName = ZipUtils.normalizeFileName(fileHeader.getFileName());
        int lastModifiedTime = fileHeader.getLastModifiedTime();
        GeneralPurposeFlag generalPurposeFlag = fileHeader.getGeneralPurposeFlag();

        CompressionMethod compressionMethod = fileHeader.getOriginalCompressionMethod();
        CompressionLevel compressionLevel = generalPurposeFlag.getCompressionLevel();
        EncryptionMethod encryptionMethod = fileHeader.getEncryptionMethod();
        ExternalFileAttributes externalFileAttributes = fileHeader.getExternalFileAttributes();

        RegularFileZipEntry zipEntry = new RegularFileZipEntry(fileName,
                                                               lastModifiedTime,
                                                               externalFileAttributes,
                                                               getAesVersion(),
                                                               compressionMethod,
                                                               compressionLevel,
                                                               encryptionMethod);

        zipEntry.setDataDescriptorAvailable(fileHeader.isDataDescriptorAvailable());
        zipEntry.setLzmaEosMarker(generalPurposeFlag.isLzmaEosMarker());
        zipEntry.setZip64(fileHeader.isZip64());
        zipEntry.setComment(fileHeader.getComment());
        zipEntry.setUtf8(fileHeader.getGeneralPurposeFlag().isUtf8());
        zipEntry.setStrongEncryption(generalPurposeFlag.isStrongEncryption());
        zipEntry.setInputStreamSup(() -> createInputStream(zipEntry));

        return zipEntry;
    }

    private ZipEntry createEmptyDirectoryEntry() {
        String dirName = fileHeader.getFileName();
        int lastModifiedTime = fileHeader.getLastModifiedTime();
        ExternalFileAttributes externalFileAttributes = fileHeader.getExternalFileAttributes();
        return new EmptyDirectoryZipEntry(dirName, lastModifiedTime, externalFileAttributes);
    }

    private InputStream createInputStream(ZipEntry zipEntry) throws IOException {
        DataInput in = createDataInput(zipEntry);

        LocalFileHeader localFileHeader = new LocalFileHeaderReader(charsetCustomizer).read(in);
        zipEntry.setDataDescriptorAvailable(localFileHeader.isDataDescriptorAvailable());
        // TODO check that localFileHeader matches fileHeader

        in = DataDescriptorDataInput.create(zipEntry, in);
        in = LimitSizeDataInput.create(zipEntry.getCompressedSize(), in);
        in = EncryptedDataInput.create(zipEntry.createDecoder(in), in);
        in = Compression.of(zipEntry.getCompressionMethod()).addCompressionDecorator(zipEntry, in);
        in = SizeCheckDataInput.uncompressedSize(zipEntry, in);
        in = ChecksumCheckDataInput.checksum(zipEntry, in);

        return ReadBufferInputStream.create(in);
    }

    private DataInput createDataInput(ZipEntry zipEntry) throws IOException {
        ConsecutiveAccessDataInput in = UnzipExtractEngine.createConsecutiveAccessDataInput(srcZip);
        in.seekForward(zipEntry.getLocalFileHeaderAbsOffs());
        return in;
    }

    private int getDiskNo() {
        if (fileHeader.getDiskNo() == MAX_TOTAL_DISKS)
            return (int) fileHeader.getExtraField().getExtendedInfo().getDiskNo();
        return fileHeader.getDiskNo();
    }

    private long getCompressedSize() {
        if (fileHeader.getCompressedSize() == ZipModel.LOOK_IN_EXTRA_FIELD)
            return fileHeader.getExtraField().getExtendedInfo().getCompressedSize();
        return fileHeader.getCompressedSize();
    }

    private long getUncompressedSize() {
        if (fileHeader.getUncompressedSize() == ZipModel.LOOK_IN_EXTRA_FIELD)
            return fileHeader.getExtraField().getExtendedInfo().getUncompressedSize();
        return fileHeader.getUncompressedSize();
    }

    private long getLocalFileHeaderOffs() {
        if (fileHeader.getLocalFileHeaderRelativeOffs() == MAX_LOCAL_FILE_HEADER_OFFS)
            return fileHeader.getExtraField().getExtendedInfo().getLocalFileHeaderRelativeOffs();
        return fileHeader.getLocalFileHeaderRelativeOffs();
    }

    private AesVersion getAesVersion() {
        if (fileHeader.getCompressionMethod() == CompressionMethod.AES)
            return fileHeader.getExtraField().getAesRecord().getVersion();
        return AesVersionEnum.AUTO.getVersion();
    }

}
