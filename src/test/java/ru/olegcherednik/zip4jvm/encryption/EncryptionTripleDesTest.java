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
import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.secureZipStoreSolid3des168StrongZip;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;


/**
 * @author Oleg Cherednik
 * @since 21.07.2026
 */
@Test
public class EncryptionTripleDesTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();
    private static final String PASSWORD_KEY = "password: ";

    @BeforeClass
    public static void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    //    public void shouldCreateNewZipWithFolderAndAes256Encryption() {
    //        ZipSettings settings = ZipSettings.builder()
    //                                          .entrySettings(CompressionEnum.STORE, EncryptionEnum.AES_256, password)
    //                                          .comment(PASSWORD_KEY + passwordStr).build();
    //
    //        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
    //
    //        ZipIt.zip(zip).settings(settings).add(contentDirSrc);
    //        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(1);
    //        assertThatZipFile(zip, password).exists().root().matches(rootAssert);
    //    }

    //    public void shouldCreateNewZipWithFolderAndAes192Encryption() {
    //        ZipSettings settings = ZipSettings.builder()
    //                                          .entrySettings(CompressionEnum.STORE, EncryptionEnum.AES_192, password)
    //                                          .comment(PASSWORD_KEY + passwordStr).build();
    //
    //        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
    //
    //        ZipIt.zip(zip).settings(settings).add(contentDirSrc);
    //        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(1);
    //        assertThatZipFile(zip, password).exists().root().matches(rootAssert);
    //    }

    //    public void shouldCreateNewZipWithFolderAndAes128Encryption() {
    //        ZipSettings settings = ZipSettings.builder()
    //                                          .entrySettings(CompressionEnum.STORE, EncryptionEnum.AES_128, password)
    //                                          .comment(PASSWORD_KEY + passwordStr).build();
    //
    //        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
    //
    //        ZipIt.zip(zip).settings(settings).add(contentDirSrc);
    //        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(1);
    //        assertThatZipFile(zip, password).exists().root().matches(rootAssert);
    //    }

    //    public void shouldCreateNewZipWithSelectedFilesAndAesEncryption() {
    //        ZipSettings settings = ZipSettings.builder()
    //                                          .entrySettings(CompressionEnum.STORE, EncryptionEnum.AES_256, password)
    //                                          .comment(PASSWORD_KEY + passwordStr).build();
    //
    //        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
    //
    //        ZipIt.zip(zip).settings(settings).add(filesDirCars);
    //        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(1);
    //        assertThatZipFile(zip, password).exists().root().matches(dirCarsAssert);
    //
    //        ZipInfo.zip(zip).decompose(zip.getParent().resolve("decompose"));
    //    }

    //    public void shouldThrowExceptionWhenAesEncryptionAndNullOrEmptyPassword() {
    //        assertThatThrownBy(() -> ZipEntrySettings.of(CompressionEnum.STORE, EncryptionEnum.AES_256, null))
    //                .isExactlyInstanceOf(EmptyPasswordException.class);
    //
    //        assertThatThrownBy(() -> ZipEntrySettings.of(CompressionEnum.STORE,
    //                                                     EncryptionEnum.AES_256,
    //                                                     ArrayUtils.EMPTY_CHAR_ARRAY))
    //                .isExactlyInstanceOf(EmptyPasswordException.class);
    //    }

    public void shouldUnzipWhenStoreSolid3Des168() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        UnzipIt.zip(secureZipStoreSolid3des168StrongZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    //    public void shouldUnzipWhenStoreSplitAes() {
    //        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
    //
    //        UnzipSettings settings = UnzipSettings.builder().passwordProvider(fileNamePasswordProvider).build();
    //
    //        UnzipIt.zip(zipStoreSplitAes).dstDir(dstDir).settings(settings).extract();
    //        assertThatDirectory(dstDir).matches(rootAssert);
    //    }
    //
    public void shouldThrowExceptionWhenUnzip3desEncryptedZipWithIncorrectPassword() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        char[] password = UUID.randomUUID().toString().toCharArray();

        assertThatThrownBy(() -> UnzipIt.zip(secureZipStoreSolid3des168StrongZip)
                                        .dstDir(dstDir).password(password).extract())
                .isExactlyInstanceOf(IncorrectZipEntryPasswordException.class);
    }
    //
    //    public void shouldCreateSingleZipWithFilesWhenLzmaCompressionAndAesEncryption() {
    //        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
    //                                                         .compression(CompressionEnum.LZMA)
    //                                                         .encryption(EncryptionEnum.AES_256, password)
    //                                                         .lzmaEosMarker(true).build();
    //        ZipSettings settings = ZipSettings.builder()
    //                                          .entrySettings(entrySettings)
    //                                          .comment(PASSWORD_KEY + passwordStr).build();
    //
    //        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
    //
    //        ZipIt.zip(zip).settings(settings).add(filesDirBikes);
    //        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(1);
    //        assertThatZipFile(zip, password).root().matches(dirBikesAssert);
    //    }


    //    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();
    //
    //    @BeforeClass
    //    public static void createDir() throws IOException {
    //        Zip4jvmSuite.createDir(DIR_ROOT);
    //    }
    //
    //    @AfterClass(enabled = Zip4jvmSuite.clear)
    //    public static void removeDir() throws IOException {
    //        Zip4jvmSuite.removeDir(DIR_ROOT);
    //    }
    //
    //    public void shouldUnzipWhenStoreSolid3Des() throws IOException {
    //        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
    //
    //        UnzipSettings settings = UnzipSettings.builder().password(password).build();
    //
    //        UnzipIt.zip(Paths.get("d:/zip4jvm/3des/3des.zip")).dstDir(destDir).settings(settings).extract();
    ////  UnzipIt.zip(Paths.get("d:/zip4jvm/securezip/aes/aes128.zip")).destDir(destDir).settings(settings).extract();
    ////  UnzipIt.zip(Paths.get("d:/zip4jvm/securezip/aes/aes192.zip")).destDir(destDir).settings(settings).extract();
    ////  UnzipIt.zip(Paths.get("d:/zip4jvm/securezip/aes/aes256.zip")).destDir(destDir).settings(settings).extract();
    ////  assertThatDirectory(destDir).matches(rootAssert);
    //    }
    //
    //    public static void main(String[] args) throws IOException {
    //        Path zip = Paths.get("d:/zip4jvm/3des/3des_store_168.zip");
    //        Path destDir = Paths.get("d:/zip4jvm/3des/3des_store_168");
    //        UnzipIt.zip(zip).dstDir(destDir).password("5oquil2oo2vb63e8ionujny6".toCharArray()).extract();
    //
    //    }

}
