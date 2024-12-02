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
import ru.olegcherednik.zip4jvm.engine.UnzipEngine;
import ru.olegcherednik.zip4jvm.io.in.ConsecutiveAccessDataInput;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.RandomAccessDataInput;
import ru.olegcherednik.zip4jvm.io.in.ReadBufferInputStream;
import ru.olegcherednik.zip4jvm.io.in.decorators.BaseDecoratorDataInput;
import ru.olegcherednik.zip4jvm.io.in.decorators.ChecksumCheckDataInput;
import ru.olegcherednik.zip4jvm.io.in.decorators.DataDescriptorDataInput;
import ru.olegcherednik.zip4jvm.io.in.decorators.LimitSizeDataInput;
import ru.olegcherednik.zip4jvm.io.in.decorators.SizeCheckDataInput;
import ru.olegcherednik.zip4jvm.io.in.decorators.UncloseableDataInput;
import ru.olegcherednik.zip4jvm.io.in.encrypted.EncryptedDataInput;
import ru.olegcherednik.zip4jvm.io.readers.DataDescriptorReader;
import ru.olegcherednik.zip4jvm.io.readers.LocalFileHeaderReader;
import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.AesVersionEnum;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.DataDescriptorEnum;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ByteUtils;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.function.ZipEntryInputStreamFunction;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;
import ru.olegcherednik.zip4jvm.utils.time.DosTimestampConverterUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.PureJavaCrc32;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

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
                                 Function<Charset, Charset> charsetCustomizer) {
        return new FileHeaderBased(fileHeader, srcZip, charsetCustomizer).build();
    }

    public static ZipEntry build(LocalFileHeader localFileHeader,
                                 SrcZip srcZip,
                                 Function<Charset, Charset> charsetCustomizer,
                                 ConsecutiveAccessDataInput in) {
        return new LocalFileHeaderBased(localFileHeader, srcZip, charsetCustomizer).build(in);
    }

    public static ZipEntry symlink(Path symlinkTarget,
                                   String symlinkTargetRelativePath,
                                   String symlinkName,
                                   ZipEntrySettings entrySettings) {
        return Quietly.doQuietly(() -> {
            int dosLastModifiedTime = DosTimestampConverterUtils.javaToDosTime(System.currentTimeMillis());
            byte[] buf = symlinkTargetRelativePath.getBytes(Charsets.UTF_8);
            ZipEntryInputStreamFunction inputStreamFunction = zipEntry -> new ByteArrayInputStream(buf);
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
        return Quietly.doQuietly(() -> {
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
        return Quietly.doQuietly(() -> {
            long lastModifiedTime = Files.getLastModifiedTime(file).toMillis();
            int dosLastModifiedTime = DosTimestampConverterUtils.javaToDosTime(lastModifiedTime);
            long size = Files.size(file);

            CompressionMethod compressionMethod = size == 0 ? CompressionMethod.STORE
                                                            : entrySettings.getCompression().getMethod();
            CompressionLevel compressionLevel = entrySettings.getCompressionLevel();
            EncryptionMethod encryptionMethod = entrySettings.getEncryption().getMethod();
            ZipEntryInputStreamFunction inputStreamFunction = zipEntry -> Files.newInputStream(file);
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
            ZipEntryInputStreamFunction inputStreamSup = zipEntry -> entry.getInputStream();

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
            ZipEntryInputStreamFunction inputStreamFunction = zipEntry -> entry.getInputStream();
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

        public ZipEntry build() {
            boolean regularFile = ZipUtils.isRegularFile(fileHeader.getFileName());
            ZipEntry zipEntry = regularFile ? createRegularFileEntry() : createEmptyDirectoryEntry();
            zipEntry.setChecksum(fileHeader.getCrc32());
            zipEntry.setUncompressedSize(getUncompressedSize());
            zipEntry.setCompressedSize(getCompressedSize());
            zipEntry.setDiskNo(getDisk());
            zipEntry.setLocalFileHeaderRelativeOffs(getLocalFileHeaderOffs());
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
            ZipEntryInputStreamFunction inputStreamFunction = getInputStreamFunction();

            RegularFileZipEntry zipEntry = new RegularFileZipEntry(fileName,
                                                                   lastModifiedTime,
                                                                   externalFileAttributes,
                                                                   getAesVersion(),
                                                                   compressionMethod,
                                                                   compressionLevel,
                                                                   encryptionMethod,
                                                                   inputStreamFunction);

            zipEntry.setDataDescriptorAvailable(generalPurposeFlag.isDataDescriptorAvailable());
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

        @SuppressWarnings("resource")
        private ZipEntryInputStreamFunction getInputStreamFunction() {
            return zipEntry -> {
                RandomAccessDataInput in1 = UnzipEngine.createDataInput(srcZip);
                in1.seek(in1.convertToAbsoluteOffs(zipEntry.getDiskNo(), zipEntry.getLocalFileHeaderRelativeOffs()));

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
            };
        }

        private int getDisk() {
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

    @RequiredArgsConstructor
    private static final class LocalFileHeaderBased {

        private final LocalFileHeader localFileHeader;
        private final SrcZip srcZip;
        private final Function<Charset, Charset> charsetCustomizer;

        public ZipEntry build(ConsecutiveAccessDataInput in) {
            boolean regularFile = ZipUtils.isRegularFile(localFileHeader.getFileName());
            ZipEntry zipEntry = regularFile ? createRegularFileEntry(in) : createEmptyDirectoryEntry();
            zipEntry.setChecksum(localFileHeader.getCrc32());
            zipEntry.setUncompressedSize(getUncompressedSize());
            zipEntry.setCompressedSize(getCompressedSize());
            // zipEntry.setDiskNo(getDisk());
            // zipEntry.setLocalFileHeaderRelativeOffs(getLocalFileHeaderOffs());
            return zipEntry;
        }

        private ZipEntry createRegularFileEntry(ConsecutiveAccessDataInput in) {
            String fileName = ZipUtils.normalizeFileName(localFileHeader.getFileName());
            int lastModifiedTime = localFileHeader.getLastModifiedTime();
            GeneralPurposeFlag generalPurposeFlag = localFileHeader.getGeneralPurposeFlag();

            CompressionMethod compressionMethod = localFileHeader.getOriginalCompressionMethod();
            CompressionLevel compressionLevel = generalPurposeFlag.getCompressionLevel();
            EncryptionMethod encryptionMethod = EncryptionMethod.OFF;//localFileHeader.getEncryptionMethod();
            ExternalFileAttributes externalFileAttributes = null;//localFileHeader.getExternalFileAttributes();
            ZipEntryInputStreamFunction inputStreamFunction = getInputStreamFunction(in);


            RegularFileZipEntry zipEntry = new RegularFileZipEntry(fileName,
                                                                   lastModifiedTime,
                                                                   externalFileAttributes,
                                                                   getAesVersion(),
                                                                   compressionMethod,
                                                                   compressionLevel,
                                                                   encryptionMethod,
                                                                   inputStreamFunction);

            zipEntry.setDataDescriptorAvailable(generalPurposeFlag.isDataDescriptorAvailable());
            zipEntry.setLzmaEosMarker(generalPurposeFlag.isLzmaEosMarker());
//            zipEntry.setZip64(localFileHeader.isZip64());
//            zipEntry.setComment(localFileHeader.getComment());
            zipEntry.setUtf8(localFileHeader.getGeneralPurposeFlag().isUtf8());
            zipEntry.setStrongEncryption(generalPurposeFlag.isStrongEncryption());

            return zipEntry;
        }

        private AesVersion getAesVersion() {
            if (localFileHeader.getCompressionMethod() == CompressionMethod.AES
                    && localFileHeader.getExtraField() instanceof PkwareExtraField)
                return ((PkwareExtraField) localFileHeader.getExtraField()).getAesRecord().getVersion();
            return AesVersionEnum.AUTO.getVersion();
        }

        private ZipEntry createEmptyDirectoryEntry() {
            String dirName = localFileHeader.getFileName();
            int lastModifiedTime = localFileHeader.getLastModifiedTime();
//            ExternalFileAttributes externalFileAttributes = localFileHeader.getExternalFileAttributes();
            return new EmptyDirectoryZipEntry(dirName, lastModifiedTime, null);
        }

        private static ZipEntryInputStreamFunction getInputStreamFunction(DataInput in) {
            return zipEntry -> {
                DataInput in2 = new UncloseableDataInput(in);

                PostDataDescriptorDataInput pdddii = null;

                if (zipEntry.isDataDescriptorAvailable()) {
                    pdddii = PostDataDescriptorDataInput.create(zipEntry.getFileName(), in2);
                    in2 = pdddii;
                } else {
                    in2 = LimitSizeDataInput.create(zipEntry.getCompressedSize(), in2);
                }

                PostDataDescriptorDataInput aa = pdddii;

//                in2 = DataDescriptorDataInput.create(zipEntry, in2);
                // TODO size is unknown when it's set in DataDescriptor

                in2 = EncryptedDataInput.create(zipEntry.createDecoder(in2), in2);
                in2 = Compression.of(zipEntry.getCompressionMethod()).addCompressionDecorator(zipEntry, in2);
                in2 = ChecksumCheckDataInput.checksum(new LongSupplier() {
                    @Override
                    public long getAsLong() {
                        return aa == null ? 0 : aa.getDataDescriptor().getCrc32();
                    }
                }, "abc", in2);

                if (zipEntry.isDataDescriptorAvailable()) {
                } else {
                    in2 = SizeCheckDataInput.uncompressedSize(zipEntry, in2);
                    in2 = ChecksumCheckDataInput.checksum(zipEntry, in2);
                }

                return ReadBufferInputStream.create(in2);
            };
        }

        public static class PostDataDescriptorDataInput extends BaseDecoratorDataInput {

            private final LongConsumer compressedSize;
            private final LongConsumer crc32;
            @Getter
            private DataDescriptor dataDescriptor;
            private long sig;

            public static PostDataDescriptorDataInput create(String fileName, DataInput in) {
                SizeCheckDataInput scdi = SizeCheckDataInput.compressedSize(fileName, in);
//                ChecksumCheckDataInput ccdi = ChecksumCheckDataInput.checksum(fileName, scdi);
                PostDataDescriptorDataInput pdddi = new PostDataDescriptorDataInput(scdi::setExpectedSize,
                                                                                    null,//ccdi::setExpectedCrc32,
                                                                                    scdi);

                return pdddi;
            }

            protected PostDataDescriptorDataInput(LongConsumer compressedSize, LongConsumer crc32, DataInput in) {
                super(in);
                this.compressedSize = compressedSize;
                this.crc32 = crc32;
            }

            // ---------- ReadBuffer ----------

            @Override
            public int read(byte[] buf, int offs, int len) throws IOException {
                int res = 0;

                if (sig == 0) {
                    int readNow = super.read(buf, offs, Math.min(len, ByteUtils.DWORD_SIZE));

                    if (readNow == IOUtils.EOF)
                        return IOUtils.EOF;
                    if (readNow < ByteUtils.DWORD_SIZE)
                        return readNow;

                    sig = (long) (buf[offs] & 0xFF) << 24 | (sig & 0xFFFFFF00L) >> 8;

                    if (sig == DataDescriptor.SIGNATURE) {
                        dataDescriptor = DataDescriptorReader.get(true, false).read(in);
                        return IOUtils.EOF;
                    }

                    offs += readNow;
                    res += readNow;
                }

                while (res < len && dataDescriptor == null) {
                    int readNow = super.read(buf, offs, 1);

                    if (readNow == IOUtils.EOF || readNow == 0)
                        break;

                    sig = (long) (buf[offs] & 0xFF) << 24 | (sig & 0xFFFFFF00L) >> 8;

                    if (sig == DataDescriptor.SIGNATURE)
                        dataDescriptor = DataDescriptorReader.get(true, false).read(in);

                    res++;
                    offs++;
                }

                return res == 0 ? IOUtils.EOF : res;
            }

            // ---------- AutoCloseable ----------

            @Override
            public void close() throws IOException {
                if (dataDescriptor != null) {
                    compressedSize.accept(dataDescriptor.getCompressedSize() + DataDescriptor.SIGNATURE_SIZE);
                    //crc32.accept(dataDescriptor.getCrc32());
                }

                super.close();
            }

        }

//        private int getDisk() {
//            if (localFileHeader.getDiskNo() == MAX_TOTAL_DISKS)
//                return (int) localFileHeader.getExtraField().getExtendedInfo().getDiskNo();
//            return localFileHeader.getDiskNo();
//        }

        private long getCompressedSize() {
            if (localFileHeader.getCompressedSize() == ZipModel.LOOK_IN_EXTRA_FIELD)
                return ((PkwareExtraField) localFileHeader.getExtraField()).getExtendedInfo().getCompressedSize();
            return localFileHeader.getCompressedSize();
        }

        private long getUncompressedSize() {
            if (localFileHeader.getUncompressedSize() == ZipModel.LOOK_IN_EXTRA_FIELD)
                return ((PkwareExtraField) localFileHeader.getExtraField()).getExtendedInfo().getUncompressedSize();
            return localFileHeader.getUncompressedSize();
        }

//        private long getLocalFileHeaderOffs() {
//            if (localFileHeader.getLocalFileHeaderRelativeOffs() == MAX_LOCAL_FILE_HEADER_OFFS)
//                return localFileHeader.getExtraField().getExtendedInfo().getLocalFileHeaderRelativeOffs();
//            return localFileHeader.getLocalFileHeaderRelativeOffs();
//        }

    }

}
