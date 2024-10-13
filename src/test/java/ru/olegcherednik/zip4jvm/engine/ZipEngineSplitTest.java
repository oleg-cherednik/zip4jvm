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
package ru.olegcherednik.zip4jvm.engine;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.exception.EntryDuplicationException;
import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
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
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameCars;
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
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_2MB;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 12.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipEngineSplitTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipEngineSplitTest.class);
    private static final Path srcZip = rootDir.resolve("src/src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @BeforeClass
    public static void createSplitArchive() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(entrySettingsProvider())
                                          .splitSize(SIZE_1MB).build();

        try (ZipFile.Writer zipFile = ZipIt.zip(srcZip).settings(settings).open()) {
            zipFile.add(fileBentley);
            zipFile.add(fileFerrari);
            zipFile.add(fileWiesmann);
            zipFile.add(fileHonda);
        }

        assertThatDirectory(srcZip.getParent()).exists().hasDirectories(0).hasRegularFiles(3);
        assertThatZipFile(srcZip, password).exists().root().hasDirectories(0).hasRegularFiles(4);
        assertThatZipFile(srcZip, password).regularFile(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(srcZip, password).regularFile(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(srcZip, password).regularFile(fileNameWiesmann).matches(fileWiesmannAssert);
        assertThatZipFile(srcZip, fileNameHonda.toCharArray()).regularFile(fileNameHonda).matches(fileHondaAssert);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    private static Function<String, ZipEntrySettings> entrySettingsProvider() {
        return fileName -> {
            if (fileNameBentley.equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
            if (fileNameFerrari.equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if (fileNameWiesmann.equals(fileName))
                return ZipEntrySettings.builder()
                                       .encryption(Encryption.PKWARE, password)
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if (fileNameHonda.equals(fileName))
                return ZipEntrySettings.builder()
                                       .encryption(Encryption.AES_256, fileNameHonda.toCharArray())
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            return ZipEntrySettings.DEFAULT;
        };
    }

    public void shouldThrowNullPointerExceptionWhenArgumentIsNull() {
        assertThatThrownBy(() -> new ZipEngine(null,
                                               ZipSettings.DEFAULT)).isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ZipEngine(zipStoreSolid, null)).isExactlyInstanceOf(NullPointerException.class);
    }

    public void shouldAddFilesToExistedZipWhenUseZipFile() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), srcZip);

        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
            if (fileNameKawasaki.equals(fileName))
                return ZipEntrySettings.builder()
                                       .compression(Compression.STORE, CompressionLevel.NORMAL)
                                       .encryption(Encryption.PKWARE, password).build();
            if (fileNameSuzuki.equals(fileName))
                return ZipEntrySettings.builder()
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                       .encryption(Encryption.AES_256, fileNameSuzuki.toCharArray()).build();
            return ZipEntrySettings.DEFAULT;
        };

        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(entrySettingsProvider)
                                          .splitSize(SIZE_1MB).build();

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).settings(settings).open()) {
            zipFile.add(fileKawasaki);
            zipFile.add(fileSuzuki);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(3);
        assertThatZipFile(zip, password).exists().root().hasDirectories(0).hasRegularFiles(6);
        assertThatZipFile(zip, password).regularFile(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(zip, password).regularFile(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(zip, fileNameHonda.toCharArray()).regularFile(fileNameHonda).matches(fileHondaAssert);
        assertThatZipFile(zip, password).regularFile(fileNameKawasaki).matches(fileKawasakiAssert);
        assertThatZipFile(zip, fileNameSuzuki.toCharArray()).regularFile(fileNameSuzuki).matches(fileSuzukiAssert);
    }

    public void shouldThrowExceptionWhenAddDuplicateEntry() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), srcZip);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
                zipFile.add(fileBentley);
            }
        }).isExactlyInstanceOf(EntryDuplicationException.class);
    }

    public void shouldThrowExceptionWhenAddNullEntry() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), srcZip);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = new ZipEngine(zip, ZipSettings.DEFAULT)) {
                zipFile.add((ZipFile.Entry) null);
            }
        }).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test(dataProvider = "fileNames")
    public void shouldThrowExceptionWhenRemoveWithBlankName(String prefixEntryName) throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), srcZip);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = new ZipEngine(zip, ZipSettings.DEFAULT)) {
                zipFile.removeEntryByName(prefixEntryName);
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

    public void shouldAddDirectoryWhenZipExists() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), srcZip);

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.add(dirBikes);
            zipFile.add(dirCars);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(6);
        assertThatZipFile(zip, password).exists().root().hasDirectories(2).hasRegularFiles(4);
        assertThatZipFile(zip, password).regularFile(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(zip, password).regularFile(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(zip, password).regularFile(fileNameWiesmann).matches(fileWiesmannAssert);
        assertThatZipFile(zip, fileNameHonda.toCharArray()).regularFile(fileNameHonda).matches(fileHondaAssert);
        assertThatZipFile(zip, password).directory(zipDirNameBikes).matches(dirBikesAssert);
        assertThatZipFile(zip, password).directory(zipDirNameCars).matches(dirCarsAssert);
    }

    public void shouldRemoveExistedEntityWhenNormalizeName() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), srcZip);
        ZipIt.zip(zip).add(dirBikes);

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.removeEntryByName(zipDirNameBikes + fileNameHonda);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(4);
        assertThatZipFile(zip, password).exists().root().hasDirectories(1).hasRegularFiles(4);
        assertThatZipFile(zip, password).regularFile(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(zip, password).regularFile(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(zip, password).regularFile(fileNameWiesmann).matches(fileWiesmannAssert);
        assertThatZipFile(zip, fileNameHonda.toCharArray()).regularFile(fileNameHonda).matches(fileHondaAssert);
        assertThatZipFile(zip, password).directory(zipDirNameBikes).hasDirectories(0).hasRegularFiles(3);
        assertThatZipFile(zip, password).regularFile(zipDirNameBikes + fileNameDucati).matches(fileDucatiAssert);
        assertThatZipFile(zip, password).regularFile(zipDirNameBikes + fileNameKawasaki).matches(fileKawasakiAssert);
        assertThatZipFile(zip, password).regularFile(zipDirNameBikes + fileNameSuzuki).matches(fileSuzukiAssert);
    }

    public void shouldRemoveEntryWhenNotNormalizeName() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), srcZip);
        ZipIt.zip(zip).add(dirBikes);

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.removeEntryByName(dirNameBikes + '\\' + fileNameHonda);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(4);
        assertThatZipFile(zip, password).exists().root().hasDirectories(1).hasRegularFiles(4);
        assertThatZipFile(zip, password).regularFile(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(zip, password).regularFile(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(zip, password).regularFile(fileNameWiesmann).matches(fileWiesmannAssert);
        assertThatZipFile(zip, fileNameHonda.toCharArray()).regularFile(fileNameHonda).matches(fileHondaAssert);
        assertThatZipFile(zip, password).directory(zipDirNameBikes).hasDirectories(0).hasRegularFiles(3);
        assertThatZipFile(zip, password).regularFile(zipDirNameBikes + fileNameDucati).matches(fileDucatiAssert);
        assertThatZipFile(zip, password).regularFile(zipDirNameBikes + fileNameKawasaki).matches(fileKawasakiAssert);
        assertThatZipFile(zip, password).regularFile(zipDirNameBikes + fileNameSuzuki).matches(fileSuzukiAssert);
    }

    public void shouldRemoveDirectoryWhenNoDirectoryMarker() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), srcZip);
        ZipIt.zip(zip).add(dirBikes);

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.removeEntryByNamePrefix(dirNameBikes);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(3);
        assertThatZipFile(zip, password).exists().root().hasDirectories(0).hasRegularFiles(4);
        assertThatZipFile(zip, password).regularFile(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(zip, password).regularFile(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(zip, password).regularFile(fileNameWiesmann).matches(fileWiesmannAssert);
    }

    public void shouldThrowExceptionWhenRemoveNotExistedEntry() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), srcZip);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
                zipFile.removeEntryByName(fileNameKawasaki);
            }
        }).isExactlyInstanceOf(EntryNotFoundException.class);
    }

    public void shouldThrowExceptionWhenCopyNullEntry() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), srcZip);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = new ZipEngine(zip, ZipSettings.DEFAULT)) {
                zipFile.copy(null);
            }
        }).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test(dataProvider = "fileNames")
    public void shouldThrowExceptionWhenRemoveWithBlankFileName(String fileName) throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), srcZip);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = new ZipEngine(zip, ZipSettings.DEFAULT)) {
                zipFile.removeEntryByName(fileName);
            }
        }).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test(dataProvider = "fileNames")
    public void shouldThrowExceptionWhenRemoveWithBlankFileNamePrefix(String fileNamePrefix) throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), srcZip);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = new ZipEngine(zip, ZipSettings.DEFAULT)) {
                zipFile.removeEntryByNamePrefix(fileNamePrefix);
            }
        }).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    // TODO add files to existed split archive and set new split size
    // TODO add files to existed split archive and convert to solid

    public void shouldCreateZipFileWhenUseZipFileAndAddFilesUsingSupplier() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
            if (fileNameBentley.equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
            if (fileNameFerrari.equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if (fileNameWiesmann.equals(fileName))
                return ZipEntrySettings.builder()
                                       .encryption(Encryption.PKWARE, password)
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if (fileNameHonda.equals(fileName))
                return ZipEntrySettings.builder()
                                       .encryption(Encryption.AES_256, password)
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            return ZipEntrySettings.DEFAULT;
        };

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(entrySettingsProvider)
                                          .splitSize(SIZE_2MB).build();

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).settings(settings).open()) {
            zipFile.add(fileBentley, fileNameBentley);
            zipFile.add(fileFerrari, fileNameFerrari);
            zipFile.add(fileWiesmann, fileNameWiesmann);
            zipFile.add(fileHonda, fileNameHonda);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(2);
        assertThatZipFile(zip, password).exists().root().hasDirectories(0).hasRegularFiles(4);
        assertThatZipFile(zip, password).regularFile(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(zip, password).regularFile(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(zip, password).regularFile(fileNameWiesmann).matches(fileWiesmannAssert);
        assertThatZipFile(zip, password).regularFile(fileNameHonda).matches(fileHondaAssert);
    }

    public void shouldCreateZipFileWhenUseZipFileAndAddFilesWithText() throws IOException {
        final String one = "one.txt";
        final String two = "two.txt";
        final String three = "three.txt";
        final String four = "four.txt";

        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
            if (one.equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
            if (two.equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if (three.equals(fileName))
                return ZipEntrySettings.builder()
                                       .encryption(Encryption.PKWARE, password)
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if (four.equals(fileName))
                return ZipEntrySettings.builder()
                                       .encryption(Encryption.AES_256, password)
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            return ZipEntrySettings.DEFAULT;
        };

        ZipFile.Entry entryOne = createRegularFileEntry(one);
        ZipFile.Entry entryTwo = createRegularFileEntry(two);
        ZipFile.Entry entryThree = createRegularFileEntry(three);
        ZipFile.Entry entryFour = createRegularFileEntry(four);

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve(fileNameZipSrc);
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(entrySettingsProvider)
                                          .splitSize(SIZE_2MB).build();

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).settings(settings).open()) {
            zipFile.add(entryOne);
            zipFile.add(entryTwo);
            zipFile.add(entryThree);
            zipFile.add(entryFour);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip, password).exists().root().hasDirectories(0).hasRegularFiles(4);
        assertThatZipFile(zip, password).regularFile(one).exists().hasContent(one);
        assertThatZipFile(zip, password).regularFile(two).exists().hasContent(two);
        assertThatZipFile(zip, password).regularFile(three).exists().hasContent(three);
        assertThatZipFile(zip, password).regularFile(four).exists().hasContent(four);
    }

    private static ZipFile.Entry createRegularFileEntry(String fileName) {
        return ZipFile.Entry.regularFile(() -> IOUtils.toInputStream(fileName, Charsets.UTF_8),
                                         fileName,
                                         System.currentTimeMillis(),
                                         0,
                                         new ExternalFileAttributes());
    }
}
