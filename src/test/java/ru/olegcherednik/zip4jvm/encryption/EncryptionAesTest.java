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

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.ZipInfo;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.exception.EmptyPasswordException;
import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
import ru.olegcherednik.zip4jvm.model.settings.EncryptionEnum;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.contentDirSrc;
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
public class EncryptionAesTest extends BaseTest {

    private static final String PASSWORD_KEY = "password: ";

    public void shouldCreateNewZipWithFolderAndAes256Encryption() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(CompressionEnum.STORE, EncryptionEnum.AES_256, password)
                                          .comment(PASSWORD_KEY + passwordStr).build();

        Path zip = getZip();

        ZipIt.zip(zip).settings(settings).add(contentDirSrc);
        assertThatZipFile(zip).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(zip, password).exists().root().matches(rootAssert);
    }

    public void shouldCreateNewZipWithFolderAndAes192Encryption() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(CompressionEnum.STORE, EncryptionEnum.AES_192, password)
                                          .comment(PASSWORD_KEY + passwordStr).build();

        Path zip = getZip();

        ZipIt.zip(zip).settings(settings).add(contentDirSrc);

        assertThatZipFile(zip, password)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().matches(rootAssert);
    }

    public void shouldCreateNewZipWithFolderAndAes128Encryption() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(CompressionEnum.STORE, EncryptionEnum.AES_128, password)
                                          .comment(PASSWORD_KEY + passwordStr).build();

        Path zip = getZip();

        ZipIt.zip(zip).settings(settings).add(contentDirSrc);

        assertThatZipFile(zip, password)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().matches(rootAssert);
    }

    public void shouldCreateNewZipWithSelectedFilesAndAesEncryption() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(CompressionEnum.STORE, EncryptionEnum.AES_256, password)
                                          .comment(PASSWORD_KEY + passwordStr).build();

        Path zip = getZip();

        ZipIt.zip(zip).settings(settings).add(filesDirCars);

        assertThatZipFile(zip, password)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().matches(dirCarsAssert);

        ZipInfo.zip(zip).decompose(zip.getParent().resolve("decompose"));
    }

    public void shouldThrowExceptionWhenAesEncryptionAndNullOrEmptyPassword() {
        assertThatThrownBy(() -> ZipEntrySettings.of(CompressionEnum.STORE, EncryptionEnum.AES_256, null))
                .isExactlyInstanceOf(EmptyPasswordException.class);

        assertThatThrownBy(() -> ZipEntrySettings.of(CompressionEnum.STORE,
                                                     EncryptionEnum.AES_256,
                                                     ArrayUtils.EMPTY_CHAR_ARRAY))
                .isExactlyInstanceOf(EmptyPasswordException.class);
    }

    public void shouldUnzipWhenStoreSolidAes() {
        Path dstDir = getTestRoot();

        UnzipSettings settings = UnzipSettings.builder().passwordProvider(fileNamePasswordProvider).build();

        UnzipIt.zip(zipStoreSolidAes).dstDir(dstDir).settings(settings).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    public void shouldUnzipWhenStoreSplitAes() {
        Path dstDir = getTestRoot();

        UnzipSettings settings = UnzipSettings.builder().passwordProvider(fileNamePasswordProvider).build();

        UnzipIt.zip(zipStoreSplitAes).dstDir(dstDir).settings(settings).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    public void shouldThrowExceptionWhenUnzipAesEncryptedZipWithIncorrectPassword() {
        Path dstDir = getTestRoot();

        char[] password = UUID.randomUUID().toString().toCharArray();
        UnzipSettings settings = UnzipSettings.builder()
                                              .password(password)
                                              .asyncOff()
                                              .build();

        assertThatThrownBy(() -> UnzipIt.zip(zipStoreSplitAes).dstDir(dstDir).settings(settings).extract())
                .isExactlyInstanceOf(IncorrectZipEntryPasswordException.class);
    }

    public void shouldCreateSingleZipWithFilesWhenLzmaCompressionAndAesEncryption() {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(CompressionEnum.LZMA)
                                                         .encryption(EncryptionEnum.AES_256, password)
                                                         .lzmaEosMarker(true).build();
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(entrySettings)
                                          .comment(PASSWORD_KEY + passwordStr).build();

        Path zip = getZip();

        ZipIt.zip(zip).settings(settings).add(filesDirBikes);

        assertThatZipFile(zip, password)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().matches(dirBikesAssert);
    }

}
