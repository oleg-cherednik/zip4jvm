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
import ru.olegcherednik.zip4jvm.engine.unzip.UnzipEngine;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.ReadBufferInputStream;
import ru.olegcherednik.zip4jvm.io.in.decorators.ChecksumCheckDataInput;
import ru.olegcherednik.zip4jvm.io.in.decorators.DataDescriptorDataInput;
import ru.olegcherednik.zip4jvm.io.in.decorators.LimitSizeDataInput;
import ru.olegcherednik.zip4jvm.io.in.decorators.SizeCheckDataInput;
import ru.olegcherednik.zip4jvm.io.in.decorators.UncloseableDataInput;
import ru.olegcherednik.zip4jvm.io.in.encrypted.EncryptedDataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.RandomAccessDataInput;
import ru.olegcherednik.zip4jvm.io.readers.LocalFileHeaderReader;
import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.AesVersionEnum;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.DataDescriptorEnum;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.function.ZipEntryInputStreamFunction;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;
import ru.olegcherednik.zip4jvm.utils.time.DosTimestampConverterUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_LOCAL_FILE_HEADER_OFFS;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_TOTAL_DISKS;

/**
 * @author Oleg Cherednik
 * @since 04.09.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipEntryBuilder {

    public static ZipEntry build(ZipFile.Entry entry, ZipEntrySettings entrySettings) {
        return new EntryBased(entry, entrySettings).build();
    }

    public static ZipEntry build(CentralDirectory.FileHeader fileHeader,
                                 SrcZip srcZip,
                                 Function<Charset, Charset> charsetCustomizer,
                                 boolean alt) {
        return new FileHeaderBased(fileHeader, srcZip, charsetCustomizer, alt).build();
    }

    public static ZipEntry symlink(Path symlinkTarget,
                                   String symlinkTargetRelativePath,
                                   String symlinkName,
                                   ZipEntrySettings entrySettings) {
        return Quietly.doRuntime(() -> {
            int dosLastModifiedTime = DosTimestampConverterUtils.javaToDosTime(System.currentTimeMillis());
            byte[] buf = symlinkTargetRelativePath.getBytes(Charsets.UTF_8);
            ZipEntryInputStreamFunction inputStreamFunction = (zipEntry, in) -> new ByteArrayInputStream(buf);
            ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.symlink(symlinkTarget);
            CompressionMethod compressionMethod = CompressionMethod.STORE;
            EncryptionMethod encryptionMethod = EncryptionMethod.OFF;
            DataDescriptorEnum dataDescriptorAvailability = entrySettings.getDataDescriptor();
            boolean dataDescriptorAvailable = dataDescriptorAvailability == DataDescriptorEnum.AUTO
                    || dataDescriptorAvailability.isIncludeDataDescriptor(compressionMethod, encryptionMethod);
            AesVersion aesVersion = entrySettings.getAesVersion().getVersion();

            ZipEntry zipEntry = new RegularFileZipEntry(symlinkName,
                                                        dosLastModifiedTime,
                                                        externalFileAttributes,
                                                        aesVersion,
                                                        compressionMethod,
                                                        CompressionLevel.NORMAL,
                                                        encryptionMethod,
                                                        inputStreamFunction);

            zipEntry.setDataDescriptorAvailable(dataDescriptorAvailable);
            zipEntry.setComment(entrySettings.getComment());
            zipEntry.setUtf8(entrySettings.isUtf8());
            zipEntry.setUncompressedSize(buf.length);

            return zipEntry;
        });
    }

    public static ZipEntry emptyDirectory(Path dir, String dirName, ZipEntrySettings entrySettings) {
        return Quietly.doRuntime(() -> {
            long lastModifiedTime = Files.getLastModifiedTime(dir).toMillis();
            int dosLastModifiedTime = DosTimestampConverterUtils.javaToDosTime(lastModifiedTime);
            ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.directory(dir);
            EmptyDirectoryZipEntry zipEntry = new EmptyDirectoryZipEntry(dirName,
                                                                         dosLastModifiedTime,
                                                                         externalFileAttributes);
            zipEntry.setComment(entrySettings.getComment());
            zipEntry.setUtf8(entrySettings.isUtf8());
            return zipEntry;
        });
    }

    public static ZipEntry regularFile(Path file, String fileName, ZipEntrySettings entrySettings) {
        return Quietly.doRuntime(() -> {
            long lastModifiedTime = Files.getLastModifiedTime(file).toMillis();
            int dosLastModifiedTime = DosTimestampConverterUtils.javaToDosTime(lastModifiedTime);
            long size = Files.size(file);

            CompressionMethod compressionMethod = size == 0 ? CompressionMethod.STORE
                                                            : entrySettings.getCompression().getMethod();
            CompressionLevel compressionLevel = entrySettings.getCompressionLevel();
            EncryptionMethod encryptionMethod = entrySettings.getEncryption().getMethod();
            ZipEntryInputStreamFunction inputStreamFunction = (zipEntry, in) -> Files.newInputStream(file);
            ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.regularFile(file);
            boolean dataDescriptorAvailable =
                    entrySettings.getDataDescriptor().isIncludeDataDescriptor(compressionMethod, encryptionMethod);
            AesVersion aesVersion = entrySettings.getAesVersion().getVersion();

            RegularFileZipEntry zipEntry = new RegularFileZipEntry(fileName,
                                                                   dosLastModifiedTime,
                                                                   externalFileAttributes,
                                                                   aesVersion,
                                                                   compressionMethod,
                                                                   compressionLevel,
                                                                   encryptionMethod,
                                                                   inputStreamFunction);

            zipEntry.setDataDescriptorAvailable(dataDescriptorAvailable);
            zipEntry.setZip64(entrySettings.isZip64());
            zipEntry.setPassword(entrySettings.getPassword());
            zipEntry.setComment(entrySettings.getComment());
            zipEntry.setUtf8(entrySettings.isUtf8());
            zipEntry.setUncompressedSize(size);

            return zipEntry;
        });
    }

    @RequiredArgsConstructor
    private static final class EntryBased {

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
            ExternalFileAttributes externalFileAttributes = entry.getExternalFileAttributes();
            ZipEntryInputStreamFunction inputStreamSup = (zipEntry, in) -> entry.getInputStream();

            ZipEntry zipEntry = new RegularFileZipEntry(symlinkName,
                                                        lastModifiedTime,
                                                        externalFileAttributes,
                                                        AesVersionEnum.AUTO.getVersion(),
                                                        CompressionMethod.STORE,
                                                        CompressionLevel.NORMAL,
                                                        EncryptionMethod.OFF,
                                                        inputStreamSup);

            zipEntry.setDataDescriptorAvailable(true);
            zipEntry.setComment(entrySettings.getComment());
            zipEntry.setUtf8(entrySettings.isUtf8());
            zipEntry.setUncompressedSize(entry.getUncompressedSize());

            return zipEntry;
        }

        private ZipEntry createEmptyDirectoryEntry() {
            String dirName = ZipUtils.getFileName(entry);
            int lastModifiedTime = DosTimestampConverterUtils.javaToDosTime(entry.getLastModifiedTime());
            ExternalFileAttributes externalFileAttributes = entry.getExternalFileAttributes();
            return new EmptyDirectoryZipEntry(dirName, lastModifiedTime, externalFileAttributes);
        }

        private ZipEntry createRegularFileEntry() {
            String fileName = ZipUtils.getFileName(entry);
            int lastModifiedTime = DosTimestampConverterUtils.javaToDosTime(entry.getLastModifiedTime());
            ExternalFileAttributes externalFileAttributes = entry.getExternalFileAttributes();

            CompressionMethod compressionMethod =
                    entry.getUncompressedSize() == 0 ? CompressionMethod.STORE
                                                     : entrySettings.getCompression().getMethod();
            CompressionLevel compressionLevel = entrySettings.getCompressionLevel();
            EncryptionMethod encryptionMethod = entrySettings.getEncryption().getMethod();
            ZipEntryInputStreamFunction inputStreamFunction = (zipEntry, in) -> entry.getInputStream();
            boolean dataDescriptorAvailable =
                    entrySettings.getDataDescriptor().isIncludeDataDescriptor(compressionMethod, encryptionMethod);
            AesVersion aesVersion = entrySettings.getAesVersion().getVersion();

            RegularFileZipEntry zipEntry = new RegularFileZipEntry(fileName,
                                                                   lastModifiedTime,
                                                                   externalFileAttributes,
                                                                   aesVersion,
                                                                   compressionMethod,
                                                                   compressionLevel,
                                                                   encryptionMethod,
                                                                   inputStreamFunction);

            zipEntry.setDataDescriptorAvailable(dataDescriptorAvailable);
            zipEntry.setZip64(entrySettings.isZip64());
            zipEntry.setPassword(entrySettings.getPassword());
            zipEntry.setComment(entrySettings.getComment());
            zipEntry.setUtf8(entrySettings.isUtf8());
            zipEntry.setUncompressedSize(entry.getUncompressedSize());

            return zipEntry;
        }

    }

    @RequiredArgsConstructor
    private static final class FileHeaderBased {

        private final CentralDirectory.FileHeader fileHeader;
        private final SrcZip srcZip;
        private final Function<Charset, Charset> charsetCustomizer;
        private final boolean alt;

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

            ZipEntryInputStreamFunction inputStreamFunction = alt ? this::createInputStream
                                                                  : (zipEntry, in) -> createInputStream(zipEntry);

            RegularFileZipEntry zipEntry = new RegularFileZipEntry(fileName,
                                                                   lastModifiedTime,
                                                                   externalFileAttributes,
                                                                   getAesVersion(),
                                                                   compressionMethod,
                                                                   compressionLevel,
                                                                   encryptionMethod,
                                                                   alt ? this::createInputStream : inputStreamFunction);

            zipEntry.setDataDescriptorAvailable(fileHeader.isDataDescriptorAvailable());
            zipEntry.setLzmaEosMarker(generalPurposeFlag.isLzmaEosMarker());
            zipEntry.setZip64(fileHeader.isZip64());
            zipEntry.setComment(fileHeader.getComment());
            zipEntry.setUtf8(fileHeader.getGeneralPurposeFlag().isUtf8());
            zipEntry.setStrongEncryption(generalPurposeFlag.isStrongEncryption());

            return zipEntry;
        }

        private AesVersion getAesVersion() {
            if (fileHeader.getCompressionMethod() == CompressionMethod.AES)
                return fileHeader.getExtraField().getAesRecord().getVersion();
            return AesVersionEnum.AUTO.getVersion();
        }

        private ZipEntry createEmptyDirectoryEntry() {
            String dirName = fileHeader.getFileName();
            int lastModifiedTime = fileHeader.getLastModifiedTime();
            ExternalFileAttributes externalFileAttributes = fileHeader.getExternalFileAttributes();
            return new EmptyDirectoryZipEntry(dirName, lastModifiedTime, externalFileAttributes);
        }

        @SuppressWarnings({ "resource", "PMD.CloseResource" })
        private InputStream createInputStream(ZipEntry zipEntry) throws IOException {
            RandomAccessDataInput in1 = UnzipEngine.createRandomAccessDataInput(srcZip);
            in1.seek(zipEntry.getLocalFileHeaderAbsOffs());

            DataInput in2 = in1;

            LocalFileHeader localFileHeader = new LocalFileHeaderReader(charsetCustomizer).read(in2);
            zipEntry.setDataDescriptorAvailable(localFileHeader.isDataDescriptorAvailable());
            // TODO check that localFileHeader matches fileHeader

            in2 = DataDescriptorDataInput.create(zipEntry, in2);
            in2 = LimitSizeDataInput.create(zipEntry.getCompressedSize(), in2);
            in2 = EncryptedDataInput.create(zipEntry.createDecoder(in2), in2);
            in2 = Compression.of(zipEntry.getCompressionMethod()).addCompressionDecorator(zipEntry, in2);
            in2 = SizeCheckDataInput.uncompressedSize(zipEntry, in2);
            in2 = ChecksumCheckDataInput.checksum(zipEntry, in2);

            return ReadBufferInputStream.create(in2);
        }

        private InputStream createInputStream(ZipEntry zipEntry, DataInput in) throws IOException {
            in = new UncloseableDataInput(in);

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

    }

}
