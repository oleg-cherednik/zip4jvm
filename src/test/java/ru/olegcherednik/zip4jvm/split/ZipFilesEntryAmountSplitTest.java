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
package ru.olegcherednik.zip4jvm.split;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipInfo;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileKawasaki;
import static ru.olegcherednik.zip4jvm.TestData.fileMcdonnelDouglas;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameWiesmann;
import static ru.olegcherednik.zip4jvm.TestData.fileSuzuki;
import static ru.olegcherednik.zip4jvm.TestData.fileWiesmann;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 15.06.2025
 */
@Test
public class ZipFilesEntryAmountSplitTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(ZipFilesEntryAmountSplitTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(ROOT_DIR);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(ROOT_DIR);
    }

    public void shouldCreateNewSplitZipWithFilesWhenEntryAmountLimit() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");
        ZipSettings settings = splitEntryAmount(2);

        ZipIt.zip(zip).settings(splitEntryAmount(2))
             .add(Arrays.asList(fileBentley, fileFerrari, fileWiesmann));

        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(2);
        assertThatZipFile(zip).root().matches(dirCarsAssert);
        assertThatZipFile(zip).regularFile(fileNameBentley).hasDiskNo(0);
        assertThatZipFile(zip).regularFile(fileNameFerrari).hasDiskNo(0);
        assertThatZipFile(zip).regularFile(fileNameWiesmann).hasDiskNo(1);
    }

    public void foo() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");

        ZipIt.zip(zip).settings(splitEntryAmount(2))
             .add(Arrays.asList(fileBentley, fileFerrari, fileWiesmann));
        ZipIt.zip(zip).settings(splitEntryAmount(3))
             .add(Arrays.asList(fileDucati, fileHonda, fileKawasaki, fileSuzuki));
//        ZipIt.zip(zip).settings(splitEntryAmount(1))
//             .add(fileMcdonnelDouglas);

        ZipInfo.zip(zip).printShortInfo();

//        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(2);
//        assertThatZipFile(zip).root().matches(dirCarsAssert);
        assertThatZipFile(zip).regularFile(fileNameBentley).hasDiskNo(0);
        assertThatZipFile(zip).regularFile(fileNameFerrari).hasDiskNo(0);
        assertThatZipFile(zip).regularFile(fileNameWiesmann).hasDiskNo(1);
    }

    private static ZipSettings splitEntryAmount(long splitEntryAmount) {
        return ZipSettings.builder()
                          .entrySettings(CompressionEnum.DEFLATE)
                          .splitEntryAmount(splitEntryAmount).build();
    }

}
