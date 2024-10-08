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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileWiesmann;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipFilesNoSplitTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipFilesNoSplitTest.class);
    private static final Path zip = rootDir.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldCreateNewZipWithFiles() throws IOException {
        ZipSettings settings = ZipSettings
                .builder()
                .entrySettingsProvider(fileName ->
                                               ZipEntrySettings.builder()
                                                               .compression(Compression.DEFLATE,
                                                                            CompressionLevel.NORMAL)
                                                               .build())
                .build();

        List<Path> files = Arrays.asList(fileBentley, fileFerrari, fileWiesmann);
        ZipIt.zip(zip).settings(settings).add(files);

        Zip4jvmAssertions.assertThatDirectory(zip.getParent()).exists().hasOnlyRegularFiles(1);
        Zip4jvmAssertions.assertThatZipFile(zip).root().matches(TestDataAssert.dirCarsAssert);
    }

    // TODO Test to add files to existed no split zip
}
