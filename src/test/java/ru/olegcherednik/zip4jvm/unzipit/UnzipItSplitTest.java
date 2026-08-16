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
package ru.olegcherednik.zip4jvm.unzipit;

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.exception.SplitPartNotFoundException;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
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
import static ru.olegcherednik.zip4jvm.utils.PathUtils.SLASH;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Test
public class UnzipItSplitTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @BeforeClass
    public void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldUnzipRequiredFilesWhenSplit() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        List<String> fileNames = Arrays.asList(fileNameSaintPetersburg, dirNameCars + SLASH + fileNameBentley);
        UnzipIt.zip(zipDeflateSplit).dstDir(dstDir).extract(fileNames);

        assertThatDirectory(dstDir).exists().hasOnlyRegularFiles(2);
        assertThatFile(dstDir.resolve(fileNameSaintPetersburg)).matches(fileSaintPetersburgAssert);
        assertThatFile(dstDir.resolve(fileNameBentley)).matches(fileBentleyAssert);
    }

    public void shouldThrowFileNotFoundExceptionAndNotExtractPartialFilesWhenZipPartMissing() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.of(CompressionEnum.STORE);
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(entrySettings)
                                          .splitSize(SIZE_1MB)
                                          .build();

        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path zip = dstDir.resolve("src.zip");
        ZipIt.zip(zip).settings(settings).add(Arrays.asList(dirBikes, dirCars));
        assertThatDirectory(dstDir).exists().hasOnlyRegularFiles(4);

        Files.delete(dstDir.resolve("src.z02"));
        assertThatDirectory(dstDir).exists().hasOnlyRegularFiles(3);

        Path unzipDir = dstDir.resolve("unzip");
        Files.createDirectory(unzipDir);

        assertThatThrownBy(() -> UnzipIt.zip(zip).dstDir(unzipDir).extract()).isExactlyInstanceOf(
                SplitPartNotFoundException.class);
        assertThatDirectory(unzipDir).isEmpty();
    }

}
