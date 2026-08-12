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
package ru.olegcherednik.zip4jvm.encryption;

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.exception.EmptyPasswordException;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
import ru.olegcherednik.zip4jvm.model.settings.EncryptionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

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

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @BeforeClass
    public void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldCreateNewZipWithFolderAndPkwareEncryption() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(CompressionEnum.DEFLATE, EncryptionEnum.PKWARE, password)
                                          .comment("password: " + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("src.zip");

        ZipIt.zip(zip).settings(settings).add(contentDirSrc);

        assertThatZipFile(zip, password)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().matches(rootAssert);
    }

    public void shouldCreateNewZipWithSelectedFilesAndPkwareEncryption() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(CompressionEnum.DEFLATE, EncryptionEnum.PKWARE, password)
                                          .comment("password: " + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("src.zip");

        ZipIt.zip(zip).settings(settings).add(filesDirCars);

        assertThatZipFile(zip, password)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyRegularFiles(3).matches(dirCarsAssert);
    }

    public void shouldThrowExceptionWhenPkwareEncryptionAndEmptyPassword() {
        assertThatThrownBy(() -> ZipEntrySettings.of(CompressionEnum.STORE, EncryptionEnum.PKWARE, null))
                .isExactlyInstanceOf(EmptyPasswordException.class);

        assertThatThrownBy(() -> ZipEntrySettings.of(CompressionEnum.STORE,
                                                     EncryptionEnum.PKWARE,
                                                     ArrayUtils.EMPTY_CHAR_ARRAY))
                .isExactlyInstanceOf(EmptyPasswordException.class);
    }

    public void shouldUnzipWhenStoreSolidPkware() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);

        UnzipIt.zip(zipStoreSolidPkware).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    public void shouldUnzipWhenStoreSplitPkware() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);

        UnzipIt.zip(zipStoreSplitPkware).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    //    public void shouldThrowExceptionWhenUnzipPkwareEncryptedZipWithIncorrectPassword() throws IOException {
    //        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
    //
    //        char[] password = UUID.randomUUID().toString().toCharArray();
    //        UnzipSettings settings = UnzipSettings.builder()
    //                                              .password(password)
    //                                              .asyncOff()
    //                                              .build();
    //
    //        assertThatThrownBy(() -> UnzipIt.zip(zipStoreSplitPkware).dstDir(dstDir).settings(settings).extract())
    //                .isExactlyInstanceOf(IncorrectZipEntryPasswordException.class);
    //    }

    public void shouldUnzipWhenZip64ContainsOnlyOneCrcByteMatch() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path zip = Paths.get("src/test/resources/zip/zip64_crc1byte_check.zip").toAbsolutePath();

        UnzipIt.zip(zip).dstDir(dstDir).password("Shu1an@2019GTS".toCharArray()).extract();

        assertThatDirectory(dstDir).hasOnlyRegularFiles(1)
                                   .regularFile("hello.txt").hasSize(11).hasContent("hello,itsme");
    }

    public void shouldCreateSingleZipWithFilesWhenLzmaCompressionAndPkwareEncryption() {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(CompressionEnum.LZMA)
                                                         .encryption(EncryptionEnum.PKWARE, password)
                                                         .lzmaEosMarker(true).build();
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(entrySettings)
                                          .comment("password: " + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("src.zip");

        ZipIt.zip(zip).settings(settings).add(filesDirBikes);

        assertThatZipFile(zip, password)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().matches(dirBikesAssert);
    }

}
