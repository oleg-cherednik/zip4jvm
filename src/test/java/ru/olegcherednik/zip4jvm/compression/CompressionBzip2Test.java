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
package ru.olegcherednik.zip4jvm.compression;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipInfo;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileNameEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;
import static ru.olegcherednik.zip4jvm.TestData.filesDirCars;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 26.04.2025
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class CompressionBzip2Test {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(CompressionBzip2Test.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(ROOT_DIR);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(ROOT_DIR);
    }

    public void shouldCreateSingleZipWithFilesWhenDeflateCompression() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.of(CompressionEnum.BZIP2);
        ZipSettings settings = ZipSettings.builder().entrySettings(entrySettings).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);

        ZipIt.zip(zip).settings(settings).add(filesDirCars);
        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip).root().matches(dirCarsAssert);
    }

    public void shouldCreateSplitZipWithFilesWhenDeflateCompression() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(CompressionEnum.BZIP2)
                                          .splitSize(SIZE_1MB).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);

        ZipIt.zip(zip).settings(settings).add(filesDirCars);
        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(3);
        assertThatZipFile(zip).root().matches(dirCarsAssert);
    }

    public void shouldCreateSingleZipWithEntireFolderWhenDeflateCompression() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);
        ZipIt.zip(zip).settings(ZipSettings.of(CompressionEnum.BZIP2)).add(dirBikes);

        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip).exists().root().hasDirectories(1).hasRegularFiles(0);
        assertThatZipFile(zip).directory(dirNameBikes).matches(dirBikesAssert);
    }

    public void shouldCreateSplitZipWithEntireFolderWhenBzip2Compression() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(CompressionEnum.BZIP2)
                                          .splitSize(SIZE_1MB).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);
        ZipIt.zip(zip).settings(settings).add(dirCars);
        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(3);
        assertThatZipFile(zip).root().hasDirectories(1).hasRegularFiles(0);
        assertThatZipFile(zip).directory(dirNameCars).matches(dirCarsAssert);
    }

    public void shouldUseCompressStoreWhenFileEmpty() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);
        ZipIt.zip(zip).settings(ZipSettings.of(CompressionEnum.BZIP2)).add(fileEmpty);
        CentralDirectory.FileHeader fileHeader = ZipInfo.zip(zip).getFileHeader(fileNameEmpty);
        assertThat(fileHeader.getCompression()).isSameAs(Compression.STORE);
    }

}
