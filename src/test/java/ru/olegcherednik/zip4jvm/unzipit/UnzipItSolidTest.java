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
import ru.olegcherednik.zip4jvm.model.charset.Charsets;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSigSauer;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileFerrariAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSaintPetersburgAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.assertj.IDirectoryAssert.SLASH_CHAR;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatFile;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Test
public class UnzipItSolidTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @BeforeClass
    public void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldUnzipRequiredFiles() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        List<String> fileNames = Arrays.asList(fileNameSaintPetersburg, dirNameCars + SLASH_CHAR + fileNameBentley);
        UnzipIt.zip(zipDeflateSolid).dstDir(dstDir).extract(fileNames);

        assertThatDirectory(dstDir).exists().hasOnlyRegularFiles(2);
        assertThatFile(dstDir.resolve(fileNameSaintPetersburg)).matches(fileSaintPetersburgAssert);
        assertThatFile(dstDir.resolve(fileNameBentley)).matches(fileBentleyAssert);
    }

    public void shouldUnzipOneFileIgnorePath() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        UnzipIt.zip(zipDeflateSolid).dstDir(dstDir).extract(dirNameCars + '/' + fileNameFerrari);

        assertThatDirectory(dstDir).exists().hasOnlyRegularFiles(1);
        assertThatFile(dstDir.resolve(fileNameFerrari)).matches(fileFerrariAssert);
    }

    public void shouldUnzipFolder() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        UnzipIt.zip(zipDeflateSolid).dstDir(dstDir).extract(dirNameBikes);
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldExtractZipArchiveWhenEntryNameWithCustomCharset() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path zip = Zip4jvmSuite.getResourcePath("/zip/cjk_filename.zip");

        UnzipSettings settings = UnzipSettings.builder().charset(Charset.forName("GBK")).build();

        UnzipIt.zip(zip).dstDir(dstDir).settings(settings).extract();
        assertThatDirectory(dstDir).hasOnlyRegularFiles(2);
    }

    public void shouldExtractZipArchiveWhenZipWasCreatedUnderMac() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path zip = Zip4jvmSuite.getResourcePath("/zip/macos_10.zip");

        UnzipIt.zip(zip).dstDir(dstDir).extract();

        assertThatDirectory(dstDir).hasOnlyDirectories(2);
        assertThatDirectory(dstDir.resolve("__MACOSX")).exists();
        assertThatDirectory(dstDir.resolve("data")).matches(rootAssert);
    }

    public void shouldExtractZipArchiveWhenUtf8Charset() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path zip = Zip4jvmSuite.getResourcePath("/zip/test2.zip");

        UnzipSettings settings = UnzipSettings.builder().charset(Charsets.UTF_8).build();

        UnzipIt.zip(zip).dstDir(dstDir).settings(settings).extract();

        assertThatDirectory(dstDir).hasOnlyDirectories(1);
        assertThatDirectory(dstDir).directory("test").hasOnlyDirectories(3);
        assertThatDirectory(dstDir).directory("test/测试文件夹1").exists();
        assertThatDirectory(dstDir).directory("test/测试文件夹2").exists();
        assertThatDirectory(dstDir).directory("test/测试文件夹3").exists();
    }

    public void shouldExtractZipArchiveToCurrentDirWhenDstDirNotSet() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("src.zip");
        Files.copy(zipDeflateSolid, zip);
        UnzipIt.zip(zip).extract(fileNameSigSauer);
        assertThatDirectory(zip.getParent()).hasOnlyRegularFiles(2);
        assertThatFile(zip.getParent().resolve(fileNameSigSauer)).exists();
    }

}
