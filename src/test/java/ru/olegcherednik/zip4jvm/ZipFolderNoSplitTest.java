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

import ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirEmpty;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class ZipFolderNoSplitTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipFolderNoSplitTest.class);
    private static final Path zip = rootDir.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    @Test
    public void shouldCreateNewZipWithFolder() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                         .build();
        ZipSettings settings = ZipSettings.builder().entrySettings(entrySettings).build();
        ZipIt.zip(zip).settings(settings).add(dirCars);

        Zip4jvmAssertions.assertThatDirectory(zip.getParent()).exists().hasOnlyRegularFiles(1);
        Zip4jvmAssertions.assertThatZipFile(zip).exists().root().hasEntries(1).hasDirectories(1);
        Zip4jvmAssertions.assertThatZipFile(zip).directory("cars/").matches(TestDataAssert.dirCarsAssert);
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
        Assertions.assertThat(Files.exists(zip)).isTrue();
        Assertions.assertThat(Files.isRegularFile(zip)).isTrue();

        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                         .build();

        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(entrySettings)
                                          .build();
        ZipIt.zip(zip).settings(settings).add(dirEmpty);

        Zip4jvmAssertions.assertThatDirectory(zip.getParent()).exists().hasOnlyRegularFiles(1);
        Zip4jvmAssertions.assertThatZipFile(zip).exists().root().hasEntries(3).hasDirectories(3);
        Zip4jvmAssertions.assertThatZipFile(zip).directory("cars/").matches(TestDataAssert.dirCarsAssert);
        // TODO commented test
        // Zip4jvmAssertions.assertThatZipFile(zip).directory("Star Wars/")
        // =.matches(TestDataAssert.zipStarWarsDirAssert);
        Zip4jvmAssertions.assertThatZipFile(zip).directory("empty_dir/").matches(TestDataAssert.dirEmptyAssert);
    }

}
