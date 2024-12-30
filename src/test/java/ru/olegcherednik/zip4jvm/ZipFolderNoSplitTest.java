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
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirEmpty;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
public class ZipFolderNoSplitTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(ZipFolderNoSplitTest.class);
    private static final Path SRC_ZIP = ROOT_DIR.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(ROOT_DIR);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(ROOT_DIR);
    }

    @Test
    public void shouldCreateNewZipWithFolder() throws IOException {
        ZipIt.zip(SRC_ZIP).settings(ZipSettings.of(Compression.DEFLATE)).add(dirCars);
        assertThatDirectory(SRC_ZIP.getParent()).exists().hasOnlyRegularFiles(1);
        assertThatZipFile(SRC_ZIP).exists().root().hasEntries(1).hasDirectories(1);
        assertThatZipFile(SRC_ZIP).directory("cars").matches(TestDataAssert.dirCarsAssert);
    }

    // @Test(dependsOnMethods = "shouldCreateNewZipWithFolder")
    // @Ignore
    // public void shouldAddFolderToExistedZip() throws IOException {
    //    Assertions.assertThat(Files.exists(zip)).isTrue();
    //    Assertions.assertThat(Files.isRegularFile(zip)).isTrue();
    //
    //    ZipSettings settings = ZipSettings.builder()
    //                                      .entrySettingsProvider(fileName ->
    //                                                                   ZipEntrySettings.builder()
    //                                                                       .compression(Compression.DEFLATE,
    //                                                                                          CompressionLevel.NORMAL)
    //                                                                                     .build())
    //                                      .build();
    // TODO commented test
    //        ZipIt.add(zip, Zip4jvmSuite.starWarsDir, settings);
    //
    //   Zip4jvmAssertions.assertThatDirectory(ZipFolderNoSplitTest.zip.getParent()).exists()
    //   .hasSubDirectories(0).hasFiles(1);
    //   Zip4jvmAssertions.assertThatZipFile(ZipFolderNoSplitTest.zip).exists().rootEntry()
    //   .hasSubDirectories(2).hasFiles(0);
    //   Zip4jvmAssertions.assertThatZipFile(ZipFolderNoSplitTest.zip).directory("cars/")
    //   .matches(TestDataAssert.zipCarsDirAssert);
    //   Zip4jvmAssertions.assertThatZipFile(ZipFolderNoSplitTest.zip).directory("Star Wars/")
    //   .matches(TestDataAssert.zipStarWarsDirAssert);
    //}

    @Test(dependsOnMethods = "shouldAddFolderToExistedZip")
    @Ignore
    public void shouldAddEmptyDirectoryToExistedZip() throws IOException {
        assertThat(Files.exists(SRC_ZIP)).isTrue();
        assertThat(Files.isRegularFile(SRC_ZIP)).isTrue();

        ZipIt.zip(SRC_ZIP).settings(ZipSettings.of(Compression.DEFLATE)).add(dirEmpty);

        assertThatDirectory(SRC_ZIP.getParent()).exists().hasOnlyRegularFiles(1);
        assertThatZipFile(SRC_ZIP).exists().root().hasEntries(3).hasDirectories(3);
        assertThatZipFile(SRC_ZIP).directory("cars").matches(TestDataAssert.dirCarsAssert);
        // TODO commented test
        // Zip4jvmAssertions.assertThatZipFile(zip).directory("Star Wars/")
        // =.matches(TestDataAssert.zipStarWarsDirAssert);
        assertThatZipFile(SRC_ZIP).directory("empty_dir").matches(TestDataAssert.dirEmptyAssert);
    }

}
