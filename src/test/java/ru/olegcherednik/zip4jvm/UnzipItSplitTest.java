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

import ru.olegcherednik.zip4jvm.exception.SplitPartNotFoundException;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSplit;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSaintPetersburgAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatFile;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Test
public class UnzipItSplitTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(UnzipItSplitTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(ROOT_DIR);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(ROOT_DIR);
    }

    public void shouldUnzipRequiredFilesWhenSplit() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(ROOT_DIR);
        List<String> fileNames = Arrays.asList(fileNameSaintPetersburg, dirNameCars + '/' + fileNameBentley);
        UnzipIt.zip(zipDeflateSplit).dstDir(dstDir).extract(fileNames);

        assertThatDirectory(dstDir).exists().hasEntries(2).hasRegularFiles(2);
        assertThatFile(dstDir.resolve(fileNameSaintPetersburg)).matches(fileSaintPetersburgAssert);
        assertThatFile(dstDir.resolve(fileNameBentley)).matches(fileBentleyAssert);
    }

    public void shouldThrowFileNotFoundExceptionAndNotExtractPartialFilesWhenZipPartMissing() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.of(Compression.STORE);
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(entrySettings)
                                          .splitSize(SIZE_1MB)
                                          .build();

        Path dstDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(ROOT_DIR);
        Path zip = dstDir.resolve("src.zip");
        ZipIt.zip(zip).settings(settings).add(Arrays.asList(dirBikes, dirCars));
        assertThatDirectory(dstDir).exists().hasEntries(4).hasRegularFiles(4);

        Files.delete(dstDir.resolve("src.z02"));
        assertThatDirectory(dstDir).exists().hasEntries(3).hasRegularFiles(3);

        Path unzipDir = dstDir.resolve("unzip");
        Files.createDirectory(unzipDir);

        assertThatThrownBy(() -> UnzipIt.zip(zip).dstDir(unzipDir).extract()).isExactlyInstanceOf(
                SplitPartNotFoundException.class);
        assertThatDirectory(unzipDir).isEmpty();
    }

}
