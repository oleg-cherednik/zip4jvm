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

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @BeforeClass
    public static void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldCreateSingleZipWithFilesWhenDeflateCompression() {
        ZipEntrySettings entrySettings = ZipEntrySettings.of(CompressionEnum.BZIP2);
        ZipSettings settings = ZipSettings.builder().entrySettings(entrySettings).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);

        ZipIt.zip(zip).settings(settings).add(filesDirCars);
        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip).root().matches(dirCarsAssert);
    }

    public void shouldCreateSplitZipWithFilesWhenDeflateCompression() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(CompressionEnum.BZIP2)
                                          .splitSize(SIZE_1MB).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);

        ZipIt.zip(zip).settings(settings).add(filesDirCars);
        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(3);
        assertThatZipFile(zip).root().matches(dirCarsAssert);
    }

    public void shouldCreateSingleZipWithEntireFolderWhenDeflateCompression() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        ZipIt.zip(zip).settings(ZipSettings.of(CompressionEnum.BZIP2)).add(dirBikes);

        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip).exists().root().hasDirectories(1).hasRegularFiles(0);
        assertThatZipFile(zip).directory(dirNameBikes).matches(dirBikesAssert);
    }

    public void shouldCreateSplitZipWithEntireFolderWhenBzip2Compression() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(CompressionEnum.BZIP2)
                                          .splitSize(SIZE_1MB).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        ZipIt.zip(zip).settings(settings).add(dirCars);
        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(3);
        assertThatZipFile(zip).root().hasDirectories(1).hasRegularFiles(0);
        assertThatZipFile(zip).directory(dirNameCars).matches(dirCarsAssert);
    }

    public void shouldUseCompressStoreWhenFileEmpty() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        ZipIt.zip(zip).settings(ZipSettings.of(CompressionEnum.BZIP2)).add(fileEmpty);
        CentralDirectory.FileHeader fileHeader = ZipInfo.zip(zip).getFileHeader(fileNameEmpty);
        assertThat(fileHeader.getCompression()).isSameAs(Compression.STORE);
    }

}
