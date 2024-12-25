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
package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettingsProvider;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static ru.olegcherednik.zip4jvm.TestData.dirEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
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
import static ru.olegcherednik.zip4jvm.TestData.filesDirBikes;
import static ru.olegcherednik.zip4jvm.TestData.filesDirCars;
import static ru.olegcherednik.zip4jvm.TestData.filesDirSrc;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileDucatiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileFerrariAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileHondaAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileKawasakiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSuzukiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileWiesmannAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 01.09.2019
 */
@Test
public class ZipFileTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(ZipFileTest.class);
    private static final Path SRC_ZIP = ROOT_DIR.resolve("createZipArchiveAndAddFiles/src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(ROOT_DIR);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(ROOT_DIR);
    }

    public void shouldCreateZipFileWhenUseZipFileAndAddFiles() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.of(Compression.STORE);

        try (ZipFile.Writer zipFile = ZipIt.zip(SRC_ZIP).entrySettings(entrySettings).open()) {
            zipFile.add(fileBentley);
            zipFile.add(fileFerrari);
            zipFile.add(fileWiesmann);
        }

        assertThatDirectory(SRC_ZIP.getParent()).exists().hasOnlyRegularFiles(1);
        assertThatZipFile(SRC_ZIP).exists().root().hasOnlyRegularFiles(3);
        assertThatZipFile(SRC_ZIP).regularFile(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(SRC_ZIP).regularFile(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(SRC_ZIP).regularFile(fileNameWiesmann).matches(fileWiesmannAssert);
    }

    @Test(dependsOnMethods = "shouldCreateZipFileWhenUseZipFileAndAddFiles")
    public void shouldAddFilesToExistedZipWhenUseZipFile() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.of(Compression.STORE);

        try (ZipFile.Writer zipFile = ZipIt.zip(SRC_ZIP).entrySettings(entrySettings).open()) {
            zipFile.add(fileDucati);
            zipFile.add(fileHonda);
            zipFile.add(fileKawasaki);
            zipFile.add(fileSuzuki);
        }

        assertThatDirectory(SRC_ZIP.getParent()).exists().hasOnlyRegularFiles(1);
        assertThatZipFile(SRC_ZIP).exists().root().hasEntries(7).hasRegularFiles(7);
        assertThatZipFile(SRC_ZIP).regularFile(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(SRC_ZIP).regularFile(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(SRC_ZIP).regularFile(fileNameWiesmann).matches(fileWiesmannAssert);
        assertThatZipFile(SRC_ZIP).regularFile(fileNameDucati).matches(fileDucatiAssert);
        assertThatZipFile(SRC_ZIP).regularFile(fileNameHonda).matches(fileHondaAssert);
        assertThatZipFile(SRC_ZIP).regularFile(fileNameKawasaki).matches(fileKawasakiAssert);
        assertThatZipFile(SRC_ZIP).regularFile(fileNameSuzuki).matches(fileSuzukiAssert);
    }

    public void shouldCreateZipFileWithEntryCommentWhenUseZipFile() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);

        Function<String, ZipEntrySettings> func = fileName -> {
            if (fileNameBentley.equals(fileName))
                return ZipEntrySettings.builder()
                                       .compression(Compression.STORE)
                                       .comment("bentley-continental").build();
            if (fileNameFerrari.equals(fileName))
                return ZipEntrySettings.builder()
                                       .compression(Compression.DEFLATE)
                                       .comment("ferrari-458-italia").build();
            if (fileNameWiesmann.equals(fileName))
                return ZipEntrySettings.builder()
                                       .compression(Compression.STORE)
                                       .comment("wiesmann-gt-mf5").build();
            return ZipEntrySettings.DEFAULT;
        };

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).entrySettings(ZipEntrySettingsProvider.of(func)).open()) {
            zipFile.add(fileBentley);
            zipFile.add(fileFerrari);
            zipFile.add(fileWiesmann);
        }

        assertThatDirectory(zip.getParent()).exists().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).exists().root().hasOnlyRegularFiles(3);
        assertThatZipFile(zip).regularFile(fileNameBentley)
                              .matches(fileBentleyAssert).hasComment("bentley-continental");
        assertThatZipFile(zip).regularFile(fileNameFerrari)
                              .matches(fileFerrariAssert).hasComment("ferrari-458-italia");
        assertThatZipFile(zip).regularFile(fileNameWiesmann)
                              .matches(fileWiesmannAssert).hasComment("wiesmann-gt-mf5");
    }

    // TODO add unzip tests for such ZipFile

    public void shouldCreateZipFileWithEntryDifferentEncryptionAndPasswordWhenUseZipFile() throws IOException {
        Function<String, ZipEntrySettings> func = fileName -> {
            if (fileNameBentley.equals(fileName))
                return ZipEntrySettings.of(Compression.STORE);
            if (fileNameFerrari.equals(fileName))
                return ZipEntrySettings.of(Compression.STORE, Encryption.PKWARE, "1".toCharArray());
            if (fileNameWiesmann.equals(fileName))
                return ZipEntrySettings.of(Compression.STORE, Encryption.AES_256, "2".toCharArray());
            return ZipEntrySettings.DEFAULT.toBuilder().password(Zip4jvmSuite.password).build();
        };

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).entrySettings(ZipEntrySettingsProvider.of(func)).open()) {
            zipFile.add(fileBentley);
            zipFile.add(fileFerrari);
            zipFile.add(fileWiesmann);
        }

        assertThatDirectory(zip.getParent()).exists().hasOnlyRegularFiles(1);
        // TODO commented test
        // assertThatZipFile(file).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
        // assertThatZipFile(file).file(fileNameBentley).exists()
        //                        .isImage().hasSize(1_395_362).hasComment("bentley-continental");
        // assertThatZipFile(file).file(fileNameFerrari).exists()
        //                       .isImage().hasSize(320_894).hasComment("ferrari-458-italia");
        // assertThatZipFile(file).file(fileNameWiesmann).exists()
        //                       .isImage().hasSize(729_633).hasComment("wiesmann-gt-mf5");
    }

    public void shouldCreateZipFileWithContentWhenUseZipFile() throws IOException {
        Function<String, ZipEntrySettings> func = entryName -> {
            if (entryName.startsWith("Star Wars/"))
                return ZipEntrySettings.of(Compression.DEFLATE);
            if (!entryName.contains("/"))
                return ZipEntrySettings.of(Compression.DEFLATE, Encryption.PKWARE, Zip4jvmSuite.password);
            return ZipEntrySettings.of(Compression.STORE);
        };

        ZipSettings settings = ZipSettings.builder()
                                          .comment("Global Comment")
                                          .entrySettingsProvider(ZipEntrySettingsProvider.of(func)).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).settings(settings).open()) {
            for (Path path : filesDirBikes)
                zipFile.add(path);
            for (Path path : filesDirCars)
                zipFile.add(path);
            for (Path path : filesDirSrc)
                zipFile.add(path);
        }

        // TODO commented test
        // assertThatDirectory(file.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        // assertThatZipFile(file).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
        // assertThatZipFile(file).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
        // assertThatZipFile(file).file(fileNameFerrari).exists().isImage().hasSize(320_894);
        // assertThatZipFile(file).file(fileNameWiesmann).exists().isImage().hasSize(729_633);
    }

    public void shouldCreateZipFileWithEmptyDirectoryWhenAddEmptyDirectory() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(ZipEntrySettings.builder().build())
                                          .build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).settings(settings).open()) {
            zipFile.add(dirEmpty);
        }

        assertThatDirectory(zip.getParent()).exists().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).exists().root().hasEntries(1).hasDirectories(1);
        // TODO commented test
        // assertThatZipFile(file).file(fileNameBentley).exists().isImage().hasSize(1_395_362);
        // assertThatZipFile(file).file(fileNameFerrari).exists().isImage().hasSize(320_894);
        // assertThatZipFile(file).file(fileNameWiesmann).exists().isImage().hasSize(729_633);
    }
}
