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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.contentDirSrc;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class ZipFolderSplitTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipFolderSplitTest.class);
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
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).splitSize(SIZE_1MB).build();

        ZipIt.zip(zip).settings(settings).add(contentDirSrc);
        assertThatDirectory(zip.getParent()).exists().hasEntries(6).hasRegularFiles(6);
        assertThat(Files.exists(zip)).isTrue();
        assertThat(Files.isRegularFile(zip)).isTrue();
        // TODO ZipFile does not read split archive
//        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);
    }
//    TODO commented tests
//    @Test(dependsOnMethods = "shouldCreateNewZipWithFolder")
//    public void shouldThrowExceptionWhenModifySplitZip() {
//        ZipFileWriterSettings settings = ZipFileWriterSettings.builder()
//                                                  .entrySettings(
//                                                          ZipEntrySettings.builder()
//                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
//                                                  .splitSize(2014 * 1024).build();
//
//        assertThatThrownBy(() -> ZipIt.add(zip, Zip4jSuite.starWarsDir, settings)).isExactlyInstanceOf(Zip4jvmException.class);
//    }
}
