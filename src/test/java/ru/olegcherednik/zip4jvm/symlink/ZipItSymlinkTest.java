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
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.model.symlink.ZipSymlink;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirSrcSymlink;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.symlinkAbsFileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.symlinkAbsFileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelFileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelFileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.symlinkTrnFileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.zipSymlinkAbsDirNameData;
import static ru.olegcherednik.zip4jvm.TestData.zipSymlinkRelDirNameData;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileDucatiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileHondaAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

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

    public void shouldIgnoreSymlinkWhenCreateZipDefaultSettings() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Path zip = destDir.resolve("src.zip");
        ZipIt.zip(zip).settings(ZipSettings.builder().removeRootDir(true).build()).add(dirSrcSymlink);

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zip).root().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zip).file(fileNameDucati).matches(fileDucatiAssert);
    }

    public void shouldCreateZipNoSymlinkWhenIncludeLinkedFile() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .removeRootDir(true)
                                          .zipSymlink(ZipSymlink.INCLUDE_LINKED_FILE)
                                          .build();

        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Path zip = destDir.resolve("src.zip");
        ZipIt.zip(zip).settings(settings).add(dirSrcSymlink);

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zip).root().hasDirectories(2).hasFiles(6);
        assertThatZipFile(zip).directory(zipSymlinkRelDirNameData).matches(rootAssert);
        assertThatZipFile(zip).directory(zipSymlinkAbsDirNameData).matches(rootAssert);
        assertThatZipFile(zip).file(fileNameDucati).matches(fileDucatiAssert);
        assertThatZipFile(zip).file(symlinkRelFileNameDucati).isNotSymlink().matches(fileDucatiAssert);
        assertThatZipFile(zip).file(symlinkRelFileNameHonda).isNotSymlink().matches(fileHondaAssert);
        assertThatZipFile(zip).file(symlinkAbsFileNameDucati).isNotSymlink().matches(fileDucatiAssert);
        assertThatZipFile(zip).file(symlinkAbsFileNameHonda).isNotSymlink().matches(fileHondaAssert);
        assertThatZipFile(zip).file(symlinkTrnFileNameHonda).isNotSymlink().matches(fileHondaAssert);
    }

}
