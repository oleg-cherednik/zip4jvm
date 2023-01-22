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
package ru.olegcherednik.zip4jvm.symlink;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirSrcSymlink;

/**
 * @author Oleg Cherednik
 * @since 22.01.2023
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipItSymlinkTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipItSymlinkTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldCreateZipWhenCreateZipWithDefaultSettings() throws IOException {
        Path zip = rootDir.resolve("src.zip");
        ZipIt.zip(zip).add(dirSrcSymlink);
//        assertThatDirectory(defSingleZip.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(defSingleZip).root().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(defSingleZip).file(fileNameBentley).exists().hasSize(1_395_362);
    }

//    public void shouldUnzipOneFile() throws IOException {
//        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
//        UnzipIt.zip(zipDeflateSolid).destDir(destDir).extract(dirNameCars + '/' + fileNameFerrari);
//
//        assertThatDirectory(destDir).exists().hasDirectories(0).hasFiles(1);
//        assertThatFile(destDir.resolve(fileNameFerrari)).matches(fileFerrariAssert);
//    }
//
//    public void shouldUnzipFolder() throws IOException {
//        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
//        UnzipIt.zip(zipDeflateSolid).destDir(destDir).extract(dirNameBikes);
//
//        assertThatDirectory(destDir).exists().hasDirectories(1).hasFiles(0);
//        assertThatDirectory(destDir.resolve(dirNameBikes)).matches(dirBikesAssert);
//    }
//
//    public void shouldExtractZipArchiveWhenEntryNameWithCustomCharset() throws IOException {
//        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
//        Path zip = Zip4jvmSuite.getResourcePath("/zip/cjk_filename.zip");
//
//        UnzipSettings settings = UnzipSettings.builder().charset(Charset.forName("GBK")).build();
//
//        UnzipIt.zip(zip).destDir(destDir).settings(settings).extract();
//
//        assertThatDirectory(destDir).hasDirectories(0).hasFiles(2);
//    }
//
//    public void shouldExtractZipArchiveWhenZipWasCreatedUnderMac() throws IOException {
//        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
//        Path zip = Zip4jvmSuite.getResourcePath("/zip/macos_10.zip");
//
//        UnzipIt.zip(zip).destDir(destDir).extract();
//
//        int a = 0;
//        a++;
////    TODO commented tests
////        assertThatDirectory(destDir).hasDirectories(0).hasFiles(2);
////        assertThatDirectory(destDir).file("fff - 副本.txt").exists();
//    }
//
//    public void shouldExtractZipArchiveWhenUtf8Charset() throws IOException {
//        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
//        Path zip = Zip4jvmSuite.getResourcePath("/zip/test2.zip");
//
//        UnzipSettings settings = UnzipSettings.builder().charset(StandardCharsets.UTF_8).build();
//
//        UnzipIt.zip(zip).destDir(destDir).settings(settings).extract();
//
//        assertThatDirectory(destDir).hasDirectories(1).hasFiles(0);
//        assertThatDirectory(destDir).directory("test").hasDirectories(3).hasFiles(0);
//        assertThatDirectory(destDir).directory("test/测试文件夹1").exists();
//        assertThatDirectory(destDir).directory("test/测试文件夹2").exists();
//        assertThatDirectory(destDir).directory("test/测试文件夹3").exists();
//    }

}
