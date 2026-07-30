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
package ru.olegcherednik.zip4jvm.io.writers;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Flag;
import ru.olegcherednik.zip4jvm.exception.EncryptionNotSupportedException;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.in.file.random.SplitRandomAccessDataInput;
import ru.olegcherednik.zip4jvm.io.out.file.SolidDataOutput;
import ru.olegcherednik.zip4jvm.io.readers.EncryptedCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.Version;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.charset.UnmodifiedCharsetProvider;
import ru.olegcherednik.zip4jvm.model.password.SinglePasswordProvider;
import ru.olegcherednik.zip4jvm.model.settings.CompressionLevelEnum;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;

/**
 * @author Oleg Cherednik
 * @since 30.07.2026
 */
@Test
@SuppressWarnings("PMD.CloseResource")
public class EncryptedCentralDirectoryWriterTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @BeforeClass
    public static void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    @DataProvider(name = "compressions")
    public static Object[][] compressions() {
        return new Object[][] { { Compression.STORE }, { Compression.DEFLATE } };
    }

    @Test(dataProvider = "compressions")
    public void shouldReadCentralDirectoryWhenWrittenEncrypted(Compression compression) throws IOException {
        CentralDirectory expected = createCentralDirectory();
        Path file = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(compression.name() + ".data");

        EncryptedCentralDirectoryWriter writer = createWriter(expected, compression);
        write(writer, file);

        Zip64.ExtensibleDataSector extensibleDataSector = writer.getExtensibleDataSector();
        assertThat(extensibleDataSector.getCompression()).isSameAs(compression);
        assertThat(extensibleDataSector.getEncryptionAlgorithm()).isSameAs(EncryptionAlgorithm.TRIPLE_DES_168);
        assertThat(extensibleDataSector.getBitLength()).isEqualTo(168);
        assertThat(extensibleDataSector.getFlags()).isSameAs(Flag.PASSWORD_KEY);
        assertThat(extensibleDataSector.getCompressedSize()).isEqualTo(Files.size(file));
        assertThat(extensibleDataSector.getUncompressedSize()).isPositive();

        CentralDirectory actual = read(file, expected.getFileHeaders().size(), extensibleDataSector, password);

        assertThat(actual.getFileHeaders()).hasSameSizeAs(expected.getFileHeaders());

        for (int i = 0; i < expected.getFileHeaders().size(); i++) {
            CentralDirectory.FileHeader expectedFileHeader = expected.getFileHeaders().get(i);
            CentralDirectory.FileHeader actualFileHeader = actual.getFileHeaders().get(i);

            assertThat(actualFileHeader.getFileName()).isEqualTo(expectedFileHeader.getFileName());
            assertThat(actualFileHeader.getCompression()).isSameAs(expectedFileHeader.getCompression());
            assertThat(actualFileHeader.getCrc32()).isEqualTo(expectedFileHeader.getCrc32());
            assertThat(actualFileHeader.getCompressedSize()).isEqualTo(expectedFileHeader.getCompressedSize());
            assertThat(actualFileHeader.getUncompressedSize()).isEqualTo(expectedFileHeader.getUncompressedSize());
            assertThat(actualFileHeader.getLocalFileHeaderRelativeOffs())
                    .isEqualTo(expectedFileHeader.getLocalFileHeaderRelativeOffs());
        }

        assertThat(actual.getDecryptionHeader()).isNotNull();
        assertThat(actual.getDecryptionHeader().getEncryptionAlgorithm()).isSameAs(EncryptionAlgorithm.TRIPLE_DES_168);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public void shouldThrowIncorrectPasswordExceptionWhenReadWithIncorrectPassword() throws IOException {
        CentralDirectory centralDirectory = createCentralDirectory();
        Path file = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("central_directory.data");

        EncryptedCentralDirectoryWriter writer = createWriter(centralDirectory, Compression.STORE);
        write(writer, file);

        Zip64.ExtensibleDataSector extensibleDataSector = writer.getExtensibleDataSector();
        int totalEntries = centralDirectory.getFileHeaders().size();

        assertThatThrownBy(() -> read(file, totalEntries, extensibleDataSector, "unknown".toCharArray()))
                .isInstanceOf(IncorrectPasswordException.class);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public void shouldThrowEncryptionNotSupportedExceptionWhenEncryptionIsNotStrong() throws IOException {
        CentralDirectory centralDirectory = createCentralDirectory();
        Path file = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("central_directory.data");

        EncryptedCentralDirectoryWriter writer = new EncryptedCentralDirectoryWriter(centralDirectory,
                                                                                    Compression.STORE,
                                                                                    CompressionLevelEnum.NORMAL,
                                                                                    Encryption.AES_256,
                                                                                    password);

        assertThatThrownBy(() -> write(writer, file)).isExactlyInstanceOf(EncryptionNotSupportedException.class);
    }

    // ----------

    private static EncryptedCentralDirectoryWriter createWriter(CentralDirectory centralDirectory,
                                                                Compression compression) {
        return new EncryptedCentralDirectoryWriter(centralDirectory,
                                                   compression,
                                                   CompressionLevelEnum.NORMAL,
                                                   Encryption.TRIPLE_DES_168,
                                                   password);
    }

    private static void write(EncryptedCentralDirectoryWriter writer, Path file) throws IOException {
        try (SolidDataOutput out = new SolidDataOutput(ByteOrder.LITTLE_ENDIAN, file)) {
            writer.write(out);
        }
    }

    private static CentralDirectory read(Path file,
                                         long totalEntries,
                                         Zip64.ExtensibleDataSector extensibleDataSector,
                                         char[] password) throws IOException {
        EncryptedCentralDirectoryReader reader =
                new EncryptedCentralDirectoryReader(totalEntries,
                                                    UnmodifiedCharsetProvider.INSTANCE,
                                                    extensibleDataSector,
                                                    new SinglePasswordProvider(password));

        try (SplitRandomAccessDataInput in = new SplitRandomAccessDataInput(SrcZip.of(file))) {
            return reader.read(in);
        }
    }

    private static CentralDirectory createCentralDirectory() {
        CentralDirectory centralDirectory = new CentralDirectory();
        centralDirectory.setFileHeaders(Arrays.asList(createFileHeader("one.txt", 1, 0),
                                                      createFileHeader("dir/two.txt", 2, 100)));
        return centralDirectory;
    }

    private static CentralDirectory.FileHeader createFileHeader(String fileName, int i, long offs) {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();
        fileHeader.setVersionMadeBy(Version.of(Version.FileSystem.MS_DOS_OS2_NT_FAT, 20));
        fileHeader.setVersionToExtract(Version.of(Version.FileSystem.MS_DOS_OS2_NT_FAT, 20));
        fileHeader.setCompression(Compression.DEFLATE);
        fileHeader.setCrc32(0xAABBCCDDL + i);
        fileHeader.setCompressedSize(100L * i);
        fileHeader.setUncompressedSize(200L * i);
        fileHeader.setLocalFileHeaderRelativeOffs(offs);
        fileHeader.setFileName(fileName);
        return fileHeader;
    }

}
