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
import java.util.stream.Stream;

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
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatFile;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class UnzipItSolidTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirName(UnzipItSolidTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldUnzipRequiredFiles() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
        List<String> fileNames = Arrays.asList(fileNameSaintPetersburg, dirNameCars + '/' + fileNameBentley);
        UnzipIt.zip(zipDeflateSolid).destDir(destDir).extract(fileNames);

        assertThatDirectory(destDir).exists().hasEntries(2).hasRegularFiles(2);
        assertThatFile(destDir.resolve(fileNameSaintPetersburg)).matches(fileSaintPetersburgAssert);
        assertThatFile(destDir.resolve(fileNameBentley)).matches(fileBentleyAssert);
    }

    public void shouldUnzipOneFile() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
        UnzipIt.zip(zipDeflateSolid).destDir(destDir).extract(dirNameCars + '/' + fileNameFerrari);

        assertThatDirectory(destDir).exists().hasOnlyRegularFiles(1);
        assertThatFile(destDir.resolve(fileNameFerrari)).matches(fileFerrariAssert);
    }

    public void shouldUnzipFolder() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
        UnzipIt.zip(zipDeflateSolid).destDir(destDir).extract(dirNameBikes);

        assertThatDirectory(destDir).exists().hasEntries(1).hasDirectories(1);
        assertThatDirectory(destDir.resolve(dirNameBikes)).matches(dirBikesAssert);
    }

    public void shouldExtractZipArchiveWhenEntryNameWithCustomCharset() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
        Path zip = Zip4jvmSuite.getResourcePath("/zip/cjk_filename.zip");

        UnzipSettings settings = UnzipSettings.builder().charset(Charset.forName("GBK")).build();

        UnzipIt.zip(zip).destDir(destDir).settings(settings).extract();

        assertThatDirectory(destDir).hasEntries(2).hasRegularFiles(2);
    }

    public void shouldExtractZipArchiveWhenZipWasCreatedUnderMac() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
        Path zip = Zip4jvmSuite.getResourcePath("/zip/macos_10.zip");

        UnzipIt.zip(zip).destDir(destDir).extract();

        //    TODO commented tests
        //        assertThatDirectory(destDir).hasDirectories(0).hasFiles(2);
        //        assertThatDirectory(destDir).file("fff - 副本.txt").exists();
    }

    public void shouldExtractZipArchiveWhenUtf8Charset() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
        Path zip = Zip4jvmSuite.getResourcePath("/zip/test2.zip");

        UnzipSettings settings = UnzipSettings.builder().charset(Charsets.UTF_8).build();

        UnzipIt.zip(zip).destDir(destDir).settings(settings).extract();

        assertThatDirectory(destDir).hasEntries(1).hasDirectories(1);
        assertThatDirectory(destDir).directory("test").hasEntries(3).hasDirectories(3);
        assertThatDirectory(destDir).directory("test/测试文件夹1").exists();
        assertThatDirectory(destDir).directory("test/测试文件夹2").exists();
        assertThatDirectory(destDir).directory("test/测试文件夹3").exists();
    }

    @Test(enabled = false)
    public void foo() throws IOException {
        /*
          The issue was that for some unknown reason there's a spanned archive
          marker (0x08074b50, little endian) at the start of these ZIP files,
          right before the first local file header (0x04034b50), which results
          in iterating over the file using ZipInputStream.getNextEntry() failing
          as the first call immediately returns null.
         */
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
        Path zip = Zip4jvmSuite.getResourcePath("/zip/spanned.zip");

        // TODO we could have a problem when read a zip like a stream (not reading CentralDirectory)

        // Stream<ZipFile.Entry> stream = UnzipIt.zip(zip).open().stream();

        int a = 0;
        a++;
        //        ZipInfo.zip(zip).decompose(destDir);
    }

}
