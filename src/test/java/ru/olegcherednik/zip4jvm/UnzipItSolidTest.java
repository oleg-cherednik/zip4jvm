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

import ru.olegcherednik.zip4jvm.model.Charsets;
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
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileFerrariAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSaintPetersburgAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatFile;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Test
public class UnzipItSolidTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(UnzipItSolidTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(ROOT_DIR);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(ROOT_DIR);
    }

    public void shouldUnzipRequiredFiles() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(ROOT_DIR);
        List<String> fileNames = Arrays.asList(fileNameSaintPetersburg, dirNameCars + '/' + fileNameBentley);
        UnzipIt.zip(zipDeflateSolid).dstDir(dstDir).extract(fileNames);

        assertThatDirectory(dstDir).exists().hasEntries(2).hasRegularFiles(2);
        assertThatFile(dstDir.resolve(fileNameSaintPetersburg)).matches(fileSaintPetersburgAssert);
        assertThatFile(dstDir.resolve(fileNameBentley)).matches(fileBentleyAssert);
    }

    public void shouldUnzipOneFileIgnorePath() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(ROOT_DIR);
        UnzipIt.zip(zipDeflateSolid).dstDir(dstDir).extract(dirNameCars + '/' + fileNameFerrari);

        assertThatDirectory(dstDir).exists().hasOnlyRegularFiles(1);
        assertThatFile(dstDir.resolve(fileNameFerrari)).matches(fileFerrariAssert);
    }

    public void shouldUnzipFolder() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(ROOT_DIR);
        UnzipIt.zip(zipDeflateSolid).dstDir(dstDir).extract(dirNameBikes);
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldExtractZipArchiveWhenEntryNameWithCustomCharset() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(ROOT_DIR);
        Path zip = Zip4jvmSuite.getResourcePath("/zip/cjk_filename.zip");

        UnzipSettings settings = UnzipSettings.builder().charset(Charset.forName("GBK")).build();

        UnzipIt.zip(zip).dstDir(dstDir).settings(settings).extract();
        assertThatDirectory(dstDir).hasEntries(2).hasRegularFiles(2);
    }

    public void shouldExtractZipArchiveWhenZipWasCreatedUnderMac() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(ROOT_DIR);
        Path zip = Zip4jvmSuite.getResourcePath("/zip/macos_10.zip");

        UnzipIt.zip(zip).dstDir(dstDir).extract();

        assertThatDirectory(dstDir).hasDirectories(2).hasRegularFiles(0);
        assertThatDirectory(dstDir.resolve("__MACOSX")).exists();
        assertThatDirectory(dstDir.resolve("data")).matches(rootAssert);
    }

    public void shouldExtractZipArchiveWhenUtf8Charset() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(ROOT_DIR);
        Path zip = Zip4jvmSuite.getResourcePath("/zip/test2.zip");

        UnzipSettings settings = UnzipSettings.builder().charset(Charsets.UTF_8).build();

        UnzipIt.zip(zip).dstDir(dstDir).settings(settings).extract();

        assertThatDirectory(dstDir).hasEntries(1).hasDirectories(1);
        assertThatDirectory(dstDir).directory("test").hasEntries(3).hasDirectories(3);
        assertThatDirectory(dstDir).directory("test/测试文件夹1").exists();
        assertThatDirectory(dstDir).directory("test/测试文件夹2").exists();
        assertThatDirectory(dstDir).directory("test/测试文件夹3").exists();
    }

}
