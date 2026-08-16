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
package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.assertj.ZipEntryRegularFileAssert;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
import ru.olegcherednik.zip4jvm.model.settings.EncryptionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettingsProvider;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.function.Function;

import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirEmpty;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileKawasaki;
import static ru.olegcherednik.zip4jvm.TestData.fileMcdonnelDouglas;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileNameKawasaki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameMcdonnelDouglas;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSuzuki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameWiesmann;
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;
import static ru.olegcherednik.zip4jvm.TestData.fileSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.fileSuzuki;
import static ru.olegcherednik.zip4jvm.TestData.fileWiesmann;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirEmptyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileDucatiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileFerrariAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileHondaAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileKawasakiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileMcDonnellDouglasAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSaintPetersburgAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSuzukiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileWiesmannAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;
import static ru.olegcherednik.zip4jvm.utils.PathUtils.SLASH;

/**
 * @author Oleg Cherednik
 * @since 01.09.2019
 */
@Test
public class ZipFileTest extends BaseTest {

    private final Path zip = resolve("createZipArchiveAndAddFiles/" + fileNameZipSrc);

    public void shouldCreateZipFileWhenUseZipFileAndAddFiles() {
        ZipEntrySettings entrySettings = ZipEntrySettings.of(CompressionEnum.STORE);

        ZipIt.zip(zip).entrySettings(entrySettings).execute(zipFile -> {
            zipFile.add(fileBentley);
            zipFile.add(fileFerrari);
            zipFile.add(fileWiesmann);
        });

        assertThatZipFile(zip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyRegularFiles(3)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFile(fileNameFerrari, fileFerrariAssert)
                .withRegularFile(fileNameWiesmann, fileWiesmannAssert);
    }

    @Test(dependsOnMethods = "shouldCreateZipFileWhenUseZipFileAndAddFiles")
    public void shouldAddFilesToExistedZipWhenUseZipFile() {
        ZipEntrySettings entrySettings = ZipEntrySettings.of(CompressionEnum.STORE);

        ZipIt.zip(zip).entrySettings(entrySettings).execute(zipFile -> {
            zipFile.add(fileDucati);
            zipFile.add(fileHonda);
            zipFile.add(fileKawasaki);
            zipFile.add(fileSuzuki);
        });

        assertThatZipFile(zip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyRegularFiles(7)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFile(fileNameFerrari, fileFerrariAssert)
                .withRegularFile(fileNameWiesmann, fileWiesmannAssert)
                .withRegularFile(fileNameDucati, fileDucatiAssert)
                .withRegularFile(fileNameHonda, fileHondaAssert)
                .withRegularFile(fileNameKawasaki, fileKawasakiAssert)
                .withRegularFile(fileNameSuzuki, fileSuzukiAssert);
    }

    public void shouldCreateZipFileWithEntryCommentWhenUseZipFile() {
        Path zip = getZip();

        Function<String, ZipEntrySettings> func = fileName -> {
            if (fileNameBentley.equals(fileName))
                return ZipEntrySettings.builder()
                                       .compression(CompressionEnum.STORE)
                                       .comment(fileNameBentley).build();
            if (fileNameFerrari.equals(fileName))
                return ZipEntrySettings.builder()
                                       .compression(CompressionEnum.DEFLATE)
                                       .comment(fileNameFerrari).build();
            if (fileNameWiesmann.equals(fileName))
                return ZipEntrySettings.builder()
                                       .compression(CompressionEnum.STORE)
                                       .comment(fileNameWiesmann).build();
            return ZipEntrySettings.DEFAULT;
        };

        ZipIt.zip(zip).entrySettings(ZipEntrySettingsProvider.of(func)).execute(zipFile -> {
            zipFile.add(fileBentley);
            zipFile.add(fileFerrari);
            zipFile.add(fileWiesmann);
        });

        assertThatZipFile(zip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyRegularFiles(3)
                .withRegularFile(fileNameBentley, file -> {
                    file.matches(fileBentleyAssert);
                    ((ZipEntryRegularFileAssert) file).hasComment(fileNameBentley);
                })
                .withRegularFile(fileNameFerrari, file -> {
                    file.matches(fileFerrariAssert);
                    ((ZipEntryRegularFileAssert) file).hasComment(fileNameFerrari);
                })
                .withRegularFile(fileNameWiesmann, file -> {
                    file.matches(fileWiesmannAssert);
                    ((ZipEntryRegularFileAssert) file).hasComment(fileNameWiesmann);
                });
    }

    public void shouldCreateZipFileWithEntryDifferentEncryptionAndPasswordWhenUseZipFile() {
        char[] passwordFerrari = "1".toCharArray();
        char[] passwordWiesmann = "2".toCharArray();

        Function<String, ZipEntrySettings> func = fileName -> {
            if (fileNameBentley.equals(fileName))
                return ZipEntrySettings.of(CompressionEnum.STORE);
            if (fileNameFerrari.equals(fileName))
                return ZipEntrySettings.of(CompressionEnum.STORE, EncryptionEnum.PKWARE, passwordFerrari);
            if (fileNameWiesmann.equals(fileName))
                return ZipEntrySettings.of(CompressionEnum.STORE, EncryptionEnum.AES_256, passwordWiesmann);
            return ZipEntrySettings.DEFAULT.toBuilder().password(password).build();
        };

        Path zip = getZip();

        ZipIt.zip(zip).entrySettings(ZipEntrySettingsProvider.of(func)).execute(zipFile -> {
            zipFile.add(fileBentley);
            zipFile.add(fileFerrari);
            zipFile.add(fileWiesmann);
        });

        assertThatZipFile(zip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyRegularFiles(3)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFileEncrypted(fileNameFerrari, passwordFerrari, fileFerrariAssert)
                .withRegularFileEncrypted(fileNameWiesmann, passwordWiesmann, fileWiesmannAssert);
    }

    public void shouldCreateZipFileWithContentWhenUseZipFile() {
        Function<String, ZipEntrySettings> func = entryName -> {
            if (entryName.startsWith(dirNameBikes + SLASH))
                return ZipEntrySettings.of(CompressionEnum.DEFLATE);
            if (entryName.startsWith(dirNameCars + SLASH))
                return ZipEntrySettings.of(CompressionEnum.STORE);
            return ZipEntrySettings.of(CompressionEnum.DEFLATE, EncryptionEnum.PKWARE, password);
        };

        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(ZipEntrySettingsProvider.of(func)).build();

        Path zip = getZip();

        ZipIt.zip(zip).settings(settings).execute(zipFile -> {
            zipFile.add(dirBikes);
            zipFile.add(dirCars);
            zipFile.add(fileSaintPetersburg, fileMcdonnelDouglas);
        });

        assertThatZipFile(zip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyDirectoriesRegularFiles(2, 2)
                .withDirectory(dirNameBikes, dirBikesAssert)
                .withDirectory(dirNameCars, dirCarsAssert)
                .withRegularFileEncrypted(fileNameMcdonnelDouglas, password, fileMcDonnellDouglasAssert)
                .withRegularFileEncrypted(fileNameSaintPetersburg, password, fileSaintPetersburgAssert);
    }

    public void shouldCreateZipFileWithEmptyDirectoryWhenAddEmptyDirectory() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(ZipEntrySettings.builder().build())
                                          .build();

        Path zip = getZip();

        ZipIt.zip(zip).settings(settings).execute(zipFile -> zipFile.add(dirEmpty));

        assertThatZipFile(zip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyDirectories(1)
                .withDirectory(dirNameEmpty, dirEmptyAssert);
    }
}
