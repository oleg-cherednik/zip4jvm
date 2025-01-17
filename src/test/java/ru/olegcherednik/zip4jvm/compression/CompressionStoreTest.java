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
package ru.olegcherednik.zip4jvm.compression;

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;
import static ru.olegcherednik.zip4jvm.TestData.filesDirCars;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolidAes;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolidPkware;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplit;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.fileNamePasswordProvider;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Test
public class CompressionStoreTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(CompressionStoreTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(ROOT_DIR);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(ROOT_DIR);
    }

    public void shouldCreateSingleZipWithFilesWhenStoreCompression() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);
        ZipIt.zip(zip).settings(ZipSettings.of(Compression.STORE)).add(filesDirCars);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip).root().matches(dirCarsAssert);
    }

    public void shouldCreateSplitZipWithFilesWhenStoreCompression() throws IOException {
        ZipSettings settings = ZipSettings.builder().entrySettings(Compression.STORE).splitSize(SIZE_1MB).build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);

        ZipIt.zip(zip).settings(settings).add(filesDirCars);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(3);
        assertThatZipFile(zip).root().matches(dirCarsAssert);
    }

    public void shouldCreateSingleZipWithEntireFolderWhenStoreCompression() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);

        ZipIt.zip(zip).settings(ZipSettings.of(Compression.STORE)).add(dirCars);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip).exists().root().hasDirectories(1).hasRegularFiles(0);
        assertThatZipFile(zip).directory(dirNameCars).matches(dirCarsAssert);
    }

    public void shouldCreateSplitZipWithEntireFolderWhenStoreCompression() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(Compression.STORE)
                                          .splitSize(SIZE_1MB).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameZipSrc);

        ZipIt.zip(zip).settings(settings).add(dirCars);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(3);
        assertThatZipFile(zip).root().hasDirectories(1).hasRegularFiles(0);
        assertThatZipFile(zip).directory(dirNameCars).matches(dirCarsAssert);
    }

    public void shouldUnzipWhenStoreCompression() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);
        UnzipIt.zip(zipStoreSolid).dstDir(dstDir).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    public void shouldUnzipWhenSplitAndStoreCompression() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);
        UnzipIt.zip(zipStoreSplit).dstDir(dstDir).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    public void shouldUnzipWhenWhenStoreCompressionAndPkwareEncryption() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);

        UnzipIt.zip(zipStoreSolidPkware).dstDir(dstDir).password(password).extract(dirNameCars);
        assertThatDirectory(dstDir).matches(dirCarsAssert);
    }

    public void shouldUnzipWhenWhenStoreCompressionAndAesEncryption() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);

        UnzipSettings settings = UnzipSettings.builder().passwordProvider(fileNamePasswordProvider).build();

        UnzipIt.zip(zipStoreSolidAes).dstDir(dstDir).settings(settings).extract(dirNameCars);
        assertThatDirectory(dstDir).matches(dirCarsAssert);
    }

}
