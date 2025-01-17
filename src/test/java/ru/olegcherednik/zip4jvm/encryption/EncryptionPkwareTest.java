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
package ru.olegcherednik.zip4jvm.encryption;

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.exception.EmptyPasswordException;
import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.contentDirSrc;
import static ru.olegcherednik.zip4jvm.TestData.filesDirBikes;
import static ru.olegcherednik.zip4jvm.TestData.filesDirCars;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolidPkware;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplitPkware;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.passwordStr;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 28.07.2019
 */
@Slf4j
@Test
public class EncryptionPkwareTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(EncryptionPkwareTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(ROOT_DIR);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(ROOT_DIR);
    }

    public void shouldCreateNewZipWithFolderAndPkwareEncryption() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(Compression.DEFLATE, Encryption.PKWARE, password)
                                          .comment("password: " + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");

        ZipIt.zip(zip).settings(settings).add(contentDirSrc);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip, password).exists().root().matches(rootAssert);
    }

    public void shouldCreateNewZipWithSelectedFilesAndPkwareEncryption() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(Compression.DEFLATE, Encryption.PKWARE, password)
                                          .comment("password: " + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");

        ZipIt.zip(zip).settings(settings).add(filesDirCars);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip, password).exists().root().hasDirectories(0).hasRegularFiles(3);
        assertThatZipFile(zip, password).root().matches(dirCarsAssert);
    }

    public void shouldThrowExceptionWhenPkwareEncryptionAndEmptyPassword() throws IOException {
        assertThatThrownBy(() -> ZipEntrySettings.of(Compression.STORE, Encryption.PKWARE, null))
                .isExactlyInstanceOf(EmptyPasswordException.class);

        assertThatThrownBy(() -> ZipEntrySettings.of(Compression.STORE, Encryption.PKWARE, ArrayUtils.EMPTY_CHAR_ARRAY))
                .isExactlyInstanceOf(EmptyPasswordException.class);
    }

    public void shouldUnzipWhenStoreSolidPkware() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);

        UnzipIt.zip(zipStoreSolidPkware).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    public void shouldUnzipWhenStoreSplitPkware() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);

        UnzipIt.zip(zipStoreSplitPkware).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    public void shouldThrowExceptionWhenUnzipPkwareEncryptedZipWithIncorrectPassword() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);

        char[] password = UUID.randomUUID().toString().toCharArray();
        UnzipSettings settings = UnzipSettings.builder()
                                              .password(password)
                                              .asyncOff()
                                              .build();

        assertThatThrownBy(() -> UnzipIt.zip(zipStoreSplitPkware).dstDir(dstDir).settings(settings).extract())
                .isExactlyInstanceOf(IncorrectZipEntryPasswordException.class);
    }

    public void shouldUnzipWhenZip64ContainsOnlyOneCrcByteMatch() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);
        Path zip = Paths.get("src/test/resources/zip/zip64_crc1byte_check.zip").toAbsolutePath();

        UnzipIt.zip(zip).dstDir(dstDir).password("Shu1an@2019GTS".toCharArray()).extract();
        assertThatDirectory(dstDir).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatDirectory(dstDir).regularFile("hello.txt").exists().hasSize(11).hasContent("hello,itsme");
    }

    public void shouldCreateSingleZipWithFilesWhenLzmaCompressionAndPkwareEncryption() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.LZMA)
                                                         .encryption(Encryption.PKWARE, password)
                                                         .lzmaEosMarker(true).build();
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(entrySettings)
                                          .comment("password: " + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");

        ZipIt.zip(zip).settings(settings).add(filesDirBikes);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip, password).root().matches(dirBikesAssert);
    }

}
