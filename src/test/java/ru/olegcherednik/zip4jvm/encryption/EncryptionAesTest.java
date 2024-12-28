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
import ru.olegcherednik.zip4jvm.ZipInfo;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.exception.EmptyPasswordException;
import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.contentDirSrc;
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;
import static ru.olegcherednik.zip4jvm.TestData.filesDirBikes;
import static ru.olegcherednik.zip4jvm.TestData.filesDirCars;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolidAes;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplitAes;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.fileNamePasswordProvider;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.passwordStr;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 29.07.2019
 */
@Test
public class EncryptionAesTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(EncryptionAesTest.class);
    private static final String PASSWORD_KEY = "password: ";

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(ROOT_DIR);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(ROOT_DIR);
    }

    public void shouldCreateNewZipWithFolderAndAes256Encryption() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(Compression.STORE, Encryption.AES_256, password)
                                          .comment(PASSWORD_KEY + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);

        ZipIt.zip(zip).settings(settings).add(contentDirSrc);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip, password).exists().root().matches(rootAssert);
    }

    public void shouldCreateNewZipWithFolderAndAes192Encryption() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(Compression.STORE, Encryption.AES_192, password)
                                          .comment(PASSWORD_KEY + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);

        ZipIt.zip(zip).settings(settings).add(contentDirSrc);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip, password).exists().root().matches(rootAssert);
    }

    public void shouldCreateNewZipWithFolderAndAes128Encryption() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(Compression.STORE, Encryption.AES_128, password)
                                          .comment(PASSWORD_KEY + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);

        ZipIt.zip(zip).settings(settings).add(contentDirSrc);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip, password).exists().root().matches(rootAssert);
    }

    public void shouldCreateNewZipWithSelectedFilesAndAesEncryption() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(Compression.STORE, Encryption.AES_256, password)
                                          .comment(PASSWORD_KEY + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);

        ZipIt.zip(zip).settings(settings).add(filesDirCars);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip, password).exists().root().matches(dirCarsAssert);

        ZipInfo.zip(zip).decompose(zip.getParent().resolve("decompose"));
    }

    public void shouldThrowExceptionWhenAesEncryptionAndNullOrEmptyPassword() throws IOException {
        assertThatThrownBy(() -> ZipEntrySettings.of(Compression.STORE, Encryption.AES_256, null))
                .isExactlyInstanceOf(EmptyPasswordException.class);

        assertThatThrownBy(() -> ZipEntrySettings.of(Compression.STORE,
                                                     Encryption.AES_256,
                                                     ArrayUtils.EMPTY_CHAR_ARRAY))
                .isExactlyInstanceOf(EmptyPasswordException.class);
    }

    public void shouldUnzipWhenStoreSolidAes() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);

        UnzipSettings settings = UnzipSettings.builder().passwordProvider(fileNamePasswordProvider).build();

        UnzipIt.zip(zipStoreSolidAes).dstDir(dstDir).settings(settings).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    public void shouldUnzipWhenStoreSplitAes() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);

        UnzipSettings settings = UnzipSettings.builder().passwordProvider(fileNamePasswordProvider).build();

        UnzipIt.zip(zipStoreSplitAes).dstDir(dstDir).settings(settings).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    public void shouldThrowExceptionWhenUnzipAesEncryptedZipWithIncorrectPassword() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);

        char[] password = UUID.randomUUID().toString().toCharArray();
        UnzipSettings settings = UnzipSettings.builder()
                                              .password(password)
                                              .asyncOff()
                                              .build();

        assertThatThrownBy(() -> UnzipIt.zip(zipStoreSplitAes).dstDir(dstDir).settings(settings).extract())
                .isExactlyInstanceOf(IncorrectZipEntryPasswordException.class);
    }

    public void shouldCreateSingleZipWithFilesWhenLzmaCompressionAndAesEncryption() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.LZMA)
                                                         .encryption(Encryption.AES_256, password)
                                                         .lzmaEosMarker(true).build();
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(entrySettings)
                                          .comment(PASSWORD_KEY + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);

        ZipIt.zip(zip).settings(settings).add(filesDirBikes);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip, password).root().matches(dirBikesAssert);
    }

}
