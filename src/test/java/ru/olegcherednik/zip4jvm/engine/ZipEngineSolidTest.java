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
package ru.olegcherednik.zip4jvm.engine;

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.engine.zip.ZipEngine;
import ru.olegcherednik.zip4jvm.exception.EntryDuplicationException;
import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.model.charset.Charsets;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
import ru.olegcherednik.zip4jvm.model.settings.EncryptionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettingsProvider;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.utils.function.InputStreamSupplier;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileKawasaki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileNameKawasaki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSuzuki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameWiesmann;
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;
import static ru.olegcherednik.zip4jvm.TestData.fileSuzuki;
import static ru.olegcherednik.zip4jvm.TestData.fileWiesmann;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolid;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileDucatiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileFerrariAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileHondaAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileKawasakiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSuzukiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileWiesmannAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;
import static ru.olegcherednik.zip4jvm.utils.PathUtils.SLASH;

/**
 * @author Oleg Cherednik
 * @since 12.09.2019
 */
@Test
public class ZipEngineSolidTest extends BaseTest {

    private final Path srcZip = resolve("src/" + fileNameZipSrc);

    private final char[] fileHondaPassword = fileNameHonda.toCharArray();
    private final char[] fileSuzukiPassword = fileNameSuzuki.toCharArray();

    @BeforeClass
    public void createSolidArchive() {
        Function<String, ZipEntrySettings> func = entryName -> {
            if (fileNameBentley.equals(entryName))
                return ZipEntrySettings.of(CompressionEnum.STORE);
            if (fileNameFerrari.equals(entryName))
                return ZipEntrySettings.of(CompressionEnum.DEFLATE);
            if (fileNameWiesmann.equals(entryName))
                return ZipEntrySettings.of(CompressionEnum.DEFLATE, EncryptionEnum.PKWARE, password);
            if (fileNameHonda.equals(entryName))
                return ZipEntrySettings.of(CompressionEnum.DEFLATE, EncryptionEnum.AES_256, fileHondaPassword);
            return ZipEntrySettings.DEFAULT;
        };

        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(ZipEntrySettingsProvider.of(func)).build();

        ZipIt.zip(srcZip).settings(settings).execute(zipFile -> {
            zipFile.add(fileBentley);
            zipFile.add(fileFerrari);
            zipFile.add(fileWiesmann);
            zipFile.add(fileHonda);
        });

        assertThatZipFile(srcZip, password)
                .isSolid()
                .root().hasOnlyRegularFiles(4)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFile(fileNameFerrari, fileFerrariAssert)
                .withRegularFile(fileNameWiesmann, fileWiesmannAssert)
                .withRegularFileEncrypted(fileNameHonda, fileHondaPassword, fileHondaAssert);
    }

    @SuppressWarnings("resource")
    public void shouldThrowIllegalArgumentExceptionWhenArgumentIsNull() {
        assertThatThrownBy(() -> new ZipEngine(null, ZipSettings.DEFAULT))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ZipEngine.zip");
        assertThatThrownBy(() -> new ZipEngine(zipStoreSolid, null))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ZipEngine.settings");
    }

    public void shouldAddFilesToExistedZipWhenUseZipFile() {
        Path zip = Zip4jvmSuite.copy(getTestRoot(), srcZip);

        Function<String, ZipEntrySettings> func = fileName -> {
            if (fileNameKawasaki.equals(fileName))
                return ZipEntrySettings.of(CompressionEnum.STORE, EncryptionEnum.PKWARE, password);
            if (fileNameSuzuki.equals(fileName))
                return ZipEntrySettings.of(CompressionEnum.DEFLATE, EncryptionEnum.AES_256, fileSuzukiPassword);
            return ZipEntrySettings.DEFAULT;
        };

        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(ZipEntrySettingsProvider.of(func))
                                          .build();

        ZipIt.zip(zip).settings(settings).execute(zipFile -> {
            zipFile.add(fileKawasaki);
            zipFile.add(fileSuzuki);
        });

        assertThatZipFile(zip, password)
                .isSolid()
                .root().hasOnlyRegularFiles(6)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFile(fileNameFerrari, fileFerrariAssert)
                .withRegularFileEncrypted(fileNameHonda, fileHondaPassword, fileHondaAssert)
                .withRegularFile(fileNameKawasaki, fileKawasakiAssert)
                .withRegularFileEncrypted(fileNameSuzuki, fileSuzukiPassword, fileSuzukiAssert);
    }

    public void shouldThrowExceptionWhenAddDuplicateEntry() {
        Path zip = Zip4jvmSuite.copy(getTestRoot(), srcZip);

        assertThatThrownBy(() -> {
            ZipIt.zip(zip).execute(zipFile -> zipFile.add(fileBentley));
        }).isExactlyInstanceOf(EntryDuplicationException.class);
    }

    public void shouldThrowExceptionWhenAddNullEntry() {
        Path zip = Zip4jvmSuite.copy(getTestRoot(), srcZip);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = new ZipEngine(zip, ZipSettings.DEFAULT)) {
                zipFile.add((ZipFile.Entry) null);
            }
        }).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test(dataProvider = "fileNames")
    public void shouldThrowExceptionWhenRemoveWithBlankName(String prefixEntryName) {
        Path zip = Zip4jvmSuite.copy(getTestRoot(), srcZip);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = new ZipEngine(zip, ZipSettings.DEFAULT)) {
                zipFile.removeEntryByName(prefixEntryName);
            }
        }).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    public void shouldAddDirectoryWhenZipExists() {
        Path zip = Zip4jvmSuite.copy(getTestRoot(), srcZip);

        ZipIt.zip(zip).execute(zipFile -> {
            zipFile.add(dirBikes);
            zipFile.add(dirCars);
        });

        assertThatZipFile(zip, password)
                .isSolid()
                .root().hasOnlyDirectoriesRegularFiles(2, 4)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFile(fileNameFerrari, fileFerrariAssert)
                .withRegularFile(fileNameWiesmann, fileWiesmannAssert)
                .withRegularFileEncrypted(fileNameHonda, fileHondaPassword, fileHondaAssert)
                .withDirectory(dirNameBikes, dirBikesAssert)
                .withDirectory(dirNameCars, dirCarsAssert);
    }

    public void shouldRemoveExistedEntityWhenNormalizeName() {
        Path zip = Zip4jvmSuite.copy(getTestRoot(), srcZip);
        ZipIt.zip(zip).add(dirBikes);

        ZipIt.zip(zip).execute(zipFile -> zipFile.removeEntryByName(dirNameBikes + SLASH + fileNameHonda));

        assertThatZipFile(zip, password)
                .isSolid()
                .root().hasOnlyDirectoriesRegularFiles(1, 4)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFile(fileNameFerrari, fileFerrariAssert)
                .withRegularFile(fileNameWiesmann, fileWiesmannAssert)
                .withRegularFileEncrypted(fileNameHonda, fileHondaPassword, fileHondaAssert)
                .withDirectory(dirNameBikes, dir -> dir.hasOnlyRegularFiles(3)
                                                       .withRegularFile(fileNameDucati, fileDucatiAssert)
                                                       .withRegularFile(fileNameKawasaki, fileKawasakiAssert)
                                                       .withRegularFile(fileNameSuzuki, fileSuzukiAssert));
    }

    public void shouldRemoveEntryWhenNotNormalizeName() {
        Path zip = Zip4jvmSuite.copy(getTestRoot(), srcZip);
        ZipIt.zip(zip).add(dirBikes);

        ZipIt.zip(zip).execute(zipFile -> zipFile.removeEntryByName(dirNameBikes + '\\' + fileNameHonda));

        assertThatZipFile(zip, password)
                .isSolid()
                .root().hasOnlyDirectoriesRegularFiles(1, 4)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFile(fileNameFerrari, fileFerrariAssert)
                .withRegularFile(fileNameWiesmann, fileWiesmannAssert)
                .withRegularFileEncrypted(fileNameHonda, fileHondaPassword, fileHondaAssert)
                .withDirectory(dirNameBikes, dir -> dir.hasOnlyRegularFiles(3)
                                                       .withRegularFile(fileNameDucati, fileDucatiAssert)
                                                       .withRegularFile(fileNameKawasaki, fileKawasakiAssert)
                                                       .withRegularFile(fileNameSuzuki, fileSuzukiAssert));
    }

    public void shouldRemoveEntryWhenRelativeName() {
        Path zip = Zip4jvmSuite.copy(getTestRoot(), srcZip);
        ZipIt.zip(zip).add(dirBikes);

        ZipIt.zip(zip).execute(zipFile -> zipFile.removeEntryByName(dirNameBikes + '\\' + fileNameHonda));

        assertThatZipFile(zip, password)
                .isSolid()
                .root().hasOnlyDirectoriesRegularFiles(1, 4)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFile(fileNameFerrari, fileFerrariAssert)
                .withRegularFile(fileNameWiesmann, fileWiesmannAssert)
                .withRegularFileEncrypted(fileNameHonda, fileHondaPassword, fileHondaAssert)
                .withDirectory(dirNameBikes, dir -> dir.hasOnlyRegularFiles(3)
                                                       .withRegularFile(fileNameDucati, fileDucatiAssert)
                                                       .withRegularFile(fileNameKawasaki, fileKawasakiAssert)
                                                       .withRegularFile(fileNameSuzuki, fileSuzukiAssert));
    }

    public void shouldRemoveDirectoryWhenNoDirectoryMarker() {
        Path zip = Zip4jvmSuite.copy(getTestRoot(), srcZip);
        ZipIt.zip(zip).add(dirBikes);

        ZipIt.zip(zip).execute(zipFile -> zipFile.removeEntryByNamePrefix(dirNameBikes));

        assertThatZipFile(zip, password)
                .isSolid()
                .root().hasOnlyRegularFiles(4)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFile(fileNameFerrari, fileFerrariAssert)
                .withRegularFile(fileNameWiesmann, fileWiesmannAssert);
    }

    public void shouldThrowExceptionWhenRemoveNotExistedEntry() {
        Path zip = Zip4jvmSuite.copy(getTestRoot(), srcZip);

        assertThatThrownBy(() -> {
            ZipIt.zip(zip).execute(zipFile -> zipFile.removeEntryByName(fileNameKawasaki));
        }).isExactlyInstanceOf(EntryNotFoundException.class);
    }

    public void shouldThrowExceptionWhenCopyNullEntry() {
        Path zip = Zip4jvmSuite.copy(getTestRoot(), srcZip);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = new ZipEngine(zip, ZipSettings.DEFAULT)) {
                zipFile.copy(null);
            }
        }).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test(dataProvider = "fileNames")
    public void shouldThrowExceptionWhenRemoveWithBlankFileName(String fileName) {
        Path zip = Zip4jvmSuite.copy(getTestRoot(), srcZip);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = new ZipEngine(zip, ZipSettings.DEFAULT)) {
                zipFile.removeEntryByName(fileName);
            }
        }).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test(dataProvider = "fileNames")
    public void shouldThrowExceptionWhenRemoveWithBlankFileNamePrefix(String fileNamePrefix) {
        Path zip = Zip4jvmSuite.copy(getTestRoot(), srcZip);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = new ZipEngine(zip, ZipSettings.DEFAULT)) {
                zipFile.removeEntryByNamePrefix(fileNamePrefix);
            }
        }).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @DataProvider(name = "fileNames")
    public static Object[][] fileNames() {
        return new Object[][] {
                { null },
                { "" },
                { "  " } };
    }

    public void shouldCreateZipFileWhenUseZipFileAndAddFilesUsingSupplier() {
        Function<String, ZipEntrySettings> func = fileName -> {
            if (fileNameBentley.equals(fileName))
                return ZipEntrySettings.of(CompressionEnum.STORE);
            if (fileNameFerrari.equals(fileName))
                return ZipEntrySettings.of(CompressionEnum.DEFLATE);
            if (fileNameWiesmann.equals(fileName))
                return ZipEntrySettings.of(CompressionEnum.DEFLATE, EncryptionEnum.PKWARE, password);
            if (fileNameHonda.equals(fileName))
                return ZipEntrySettings.of(CompressionEnum.DEFLATE, EncryptionEnum.AES_256, password);
            return ZipEntrySettings.DEFAULT;
        };

        Path zip = getTestRoot().resolve("src.zip");
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(ZipEntrySettingsProvider.of(func)).build();

        ZipIt.zip(zip).settings(settings).execute(zipFile -> {
            zipFile.add(fileBentley, fileNameBentley);
            zipFile.add(fileFerrari, fileNameFerrari);
            zipFile.add(fileWiesmann, fileNameWiesmann);
            zipFile.add(fileHonda, fileNameHonda);
        });

        assertThatZipFile(zip, password)
                .isSolid()
                .root().hasOnlyRegularFiles(4)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFile(fileNameFerrari, fileFerrariAssert)
                .withRegularFile(fileNameWiesmann, fileWiesmannAssert)
                .withRegularFile(fileNameHonda, fileHondaAssert);
    }

    public void shouldCreateZipFileWhenUseZipFileAndAddFilesWithText() {
        final String one = "one.txt";
        final String two = "two.txt";
        final String three = "three.txt";
        final String four = "four.txt";

        Function<String, ZipEntrySettings> func = fileName -> {
            if (one.equals(fileName))
                return ZipEntrySettings.of(CompressionEnum.STORE);
            if (two.equals(fileName))
                return ZipEntrySettings.of(CompressionEnum.DEFLATE);
            if (three.equals(fileName))
                return ZipEntrySettings.of(CompressionEnum.DEFLATE, EncryptionEnum.PKWARE, password);
            if (four.equals(fileName))
                return ZipEntrySettings.of(CompressionEnum.DEFLATE, EncryptionEnum.AES_256, password);
            return ZipEntrySettings.DEFAULT;
        };

        ZipFile.Entry entryOne = createRegularFileEntry(one);
        ZipFile.Entry entryTwo = createRegularFileEntry(two);
        ZipFile.Entry entryThree = createRegularFileEntry(three);
        ZipFile.Entry entryFour = createRegularFileEntry(four);

        Path zip = getZip();
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(ZipEntrySettingsProvider.of(func)).build();

        ZipIt.zip(zip).settings(settings).execute(zipFile -> {
            zipFile.add(entryOne);
            zipFile.add(entryTwo);
            zipFile.add(entryThree);
            zipFile.add(entryFour);
        });

        assertThatZipFile(zip, password)
                .isSolid()
                .root().hasOnlyRegularFiles(4)
                .withRegularFile(one, file -> file.hasContent(one))
                .withRegularFile(two, file -> file.hasContent(two))
                .withRegularFile(three, file -> file.hasContent(three))
                .withRegularFile(four, file -> file.hasContent(four));
    }

    private static ZipFile.Entry createRegularFileEntry(String fileName) {
        return ZipFile.Entry.regularFile(new InputStreamSupplier() {
                                             @Override
                                             public long getSize() {
                                                 return fileName.length();
                                             }

                                             @Override
                                             public InputStream get() {
                                                 return IOUtils.toInputStream(fileName, Charsets.UTF_8);
                                             }
                                         },
                                         fileName);
    }

}
