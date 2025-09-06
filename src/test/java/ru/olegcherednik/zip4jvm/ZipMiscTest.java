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

import ru.olegcherednik.zip4jvm.exception.EntryDuplicationException;
import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.exception.PathNotExistsException;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.dirSrcData;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;
import static ru.olegcherednik.zip4jvm.TestData.fileOlegCherednik;
import static ru.olegcherednik.zip4jvm.TestData.fileWiesmann;
import static ru.olegcherednik.zip4jvm.TestData.filesDirCars;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolidPkware;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSplit;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolidPkware;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplit;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplitAes;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Test
public class ZipMiscTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();
    private static final Path ZIP_MERGE = DIR_ROOT.resolve("merge/src.zip");

    @BeforeClass
    public static void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldRetrieveAllEntryNamesForExistedZip() {
        assertThat(ZipMisc.zip(zipDeflateSolid).getEntries()).hasSize(13);
    }

    public void shouldRetrieveAllEntryNamesForExistedEncryptedZip() {
        Path zip = Zip4jvmSuite.copy(DIR_ROOT, zipDeflateSolidPkware);
        assertThat(ZipMisc.zip(zip).getEntries()).hasSize(13);
    }

    public void shouldThrowExceptionWhenAddedFileNotExists() throws IOException {
        ZipSettings settings = ZipSettings.of(CompressionEnum.STORE);

        Path notExisted = dirCars.resolve(UUID.randomUUID().toString());
        List<Path> files = Arrays.asList(fileBentley, fileFerrari, fileWiesmann, notExisted);

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);

        assertThatThrownBy(() -> ZipIt.zip(zip).settings(settings).add(files)).isExactlyInstanceOf(
                PathNotExistsException.class);
    }

    public void shouldMergeSplitZip() throws IOException {
        ZipMisc.zip(zipDeflateSplit).merge(ZIP_MERGE);
        assertThatZipFile(ZIP_MERGE).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(ZIP_MERGE).exists().root().matches(rootAssert);
    }

    @Test(dependsOnMethods = "shouldMergeSplitZip")
    public void shouldThrowExceptionWhenMergeWithDuplicatedEntries() throws IOException {
        assertThatThrownBy(() -> ZipMisc.zip(zipDeflateSplit).merge(ZIP_MERGE)).isExactlyInstanceOf(
                EntryDuplicationException.class);
    }

    public void shouldRetrieveTrueWhenSplitZipWithMultipleDisks() {
        assertThat(ZipMisc.zip(zipStoreSplit).isSplit()).isTrue();
    }

    public void shouldRetrieveTrueWhenSplitZipWithOneDisk() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(CompressionEnum.STORE)
                                          .splitSize(SIZE_1MB)
                                          .build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        ZipIt.zip(zip).settings(settings).add(Collections.singleton(fileOlegCherednik));

        assertThat(ZipMisc.zip(zipStoreSplit).isSplit()).isTrue();
    }

    public void shouldRemoveGivenFilesFromExistedZip() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        Files.createDirectories(zip.getParent());
        Files.copy(zipStoreSolid, zip);
        assertThatZipFile(zip).exists().root().matches(rootAssert);

        List<String> entryNames = filesDirCars.stream()
                                              .map(file -> dirSrcData.relativize(file).toString())
                                              .collect(Collectors.toList());

        ZipMisc zipFile = ZipMisc.zip(zip);

        zipFile.removeEntryByNamePrefix(entryNames);
        assertThat(zipFile.getEntries()).hasSize(10);
    }

    public void shouldRemoveFolderFromExistedZip() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        Files.createDirectories(zip.getParent());
        Files.copy(zipStoreSolid, zip);
        assertThatZipFile(zip).exists().root().matches(rootAssert);

        ZipMisc zipFile = ZipMisc.zip(zip);

        zipFile.removeEntryByNamePrefix(dirSrcData.relativize(dirCars).toString());
        assertThat(zipFile.getEntries()).hasSize(10);
    }

    public void shouldThrowExceptionWhenRemovedEntryWithExactNameDoesNotExists() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        Files.createDirectories(zip.getParent());
        Files.copy(zipStoreSolid, zip);

        ZipMisc zipFile = ZipMisc.zip(zip);
        assertThat(zipFile.getEntries()).hasSize(13);

        assertThatThrownBy(() -> ZipMisc.zip(zip).removeEntryByName(dirNameCars)).isExactlyInstanceOf(
                EntryNotFoundException.class);
        assertThat(zipFile.getEntries()).hasSize(13);
    }

    public void shouldIterateOverAllEntriesWhenStoreSolidPkware() {
        List<String> entryNames = ZipMisc.zip(zipStoreSolidPkware).getEntries()
                                         .map(ZipFile.Entry::getName)
                                         .collect(Collectors.toList());

        assertThat(entryNames).hasSize(13);
    }

    public void shouldRetrieveStreamWithAllEntriesWhenStoreSplitAes() {
        List<String> entryNames = ZipMisc.zip(zipStoreSplitAes).getEntries()
                                         .map(ZipFile.Entry::getName)
                                         .collect(Collectors.toList());

        assertThat(entryNames).hasSize(13);
    }
}
