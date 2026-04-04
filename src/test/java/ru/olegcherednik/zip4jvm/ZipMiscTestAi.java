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

import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.exception.PathNotExistsException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplit;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplitAes;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 04.04.2026
 */
@Test
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class ZipMiscTestAi {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @BeforeClass
    public static void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    // -- zip() factory -------------------------------------------------------

    public void shouldThrowExceptionWhenZipIsNull() {
        assertThatThrownBy(() -> ZipMisc.zip(null))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    public void shouldThrowExceptionWhenZipDoesNotExist() {
        Path notExisted = DIR_ROOT.resolve(UUID.randomUUID() + ".zip");
        assertThatThrownBy(() -> ZipMisc.zip(notExisted))
                .isExactlyInstanceOf(PathNotExistsException.class);
    }

    public void shouldThrowExceptionWhenZipIsDirectory() {
        assertThatThrownBy(() -> ZipMisc.zip(DIR_ROOT))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    // -- getComment() / setComment() -----------------------------------------

    public void shouldReturnNullWhenNoComment() {
        assertThat(ZipMisc.zip(zipStoreSolid).getComment()).isNull();
    }

    public void shouldSetAndGetComment() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        Zip4jvmSuite.copyFile(zipStoreSolid, zip);

        ZipMisc zipMisc = ZipMisc.zip(zip);
        zipMisc.setComment("hello");
        assertThat(zipMisc.getComment()).isEqualTo("hello");
    }

    public void shouldRemoveCommentWhenSetToNull() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        Zip4jvmSuite.copyFile(zipStoreSolid, zip);

        ZipMisc zipMisc = ZipMisc.zip(zip);
        zipMisc.setComment("temporary");
        zipMisc.setComment(null);
        assertThat(zipMisc.getComment()).isNull();
    }

    public void shouldRemoveCommentWhenSetToEmpty() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        Zip4jvmSuite.copyFile(zipStoreSolid, zip);

        ZipMisc zipMisc = ZipMisc.zip(zip);
        zipMisc.setComment("temporary");
        zipMisc.setComment("");
        assertThat(zipMisc.getComment()).isNull();
    }

    // -- getEntries() --------------------------------------------------------

    public void shouldReturnAllEntriesForSolidZip() {
        assertThat(ZipMisc.zip(zipStoreSolid).getEntries()).hasSize(13);
    }

    public void shouldReturnAllEntriesForSplitZip() {
        assertThat(ZipMisc.zip(zipStoreSplit).getEntries()).hasSize(13);
    }

    public void shouldReturnEntryNamesNotNull() {
        List<String> names = ZipMisc.zip(zipDeflateSolid).getEntries()
                                    .map(ZipFile.Entry::getName)
                                    .collect(Collectors.toList());

        assertThat(names).isNotEmpty().doesNotContainNull();
    }

    // -- isSplit() -----------------------------------------------------------

    public void shouldReturnFalseForSolidZip() {
        assertThat(ZipMisc.zip(zipStoreSolid).isSplit()).isFalse();
    }

    public void shouldReturnTrueForSplitZip() {
        assertThat(ZipMisc.zip(zipStoreSplit).isSplit()).isTrue();
    }

    public void shouldReturnTrueForSplitAesZip() {
        assertThat(ZipMisc.zip(zipStoreSplitAes).isSplit()).isTrue();
    }

    // -- removeEntryByName(String) -------------------------------------------

    public void shouldRemoveSingleEntryByExactName() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        Zip4jvmSuite.createDir(zip.getParent());
        Zip4jvmSuite.copyFile(zipStoreSolid, zip);

        String entryName = dirNameCars + "/" + fileNameBentley;
        ZipMisc zipMisc = ZipMisc.zip(zip);
        zipMisc.removeEntryByName(entryName);
        assertThat(zipMisc.getEntries()).hasSize(12);
    }

    public void shouldThrowExceptionWhenRemoveEntryByNameNotFound() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        Zip4jvmSuite.createDir(zip.getParent());
        Zip4jvmSuite.copyFile(zipStoreSolid, zip);

        assertThatThrownBy(() -> ZipMisc.zip(zip).removeEntryByName("nonexistent.txt"))
                .isExactlyInstanceOf(EntryNotFoundException.class);
    }

    public void shouldThrowExceptionWhenRemoveEntryByNameIsBlank() {
        assertThatThrownBy(() -> ZipMisc.zip(zipStoreSolid).removeEntryByName("  "))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    // -- removeEntryByName(Collection<String>) -------------------------------

    public void shouldRemoveMultipleEntriesByExactName() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        Zip4jvmSuite.createDir(zip.getParent());
        Zip4jvmSuite.copyFile(zipStoreSolid, zip);

        List<String> entryNames = Arrays.asList(
                dirNameCars + "/" + fileNameBentley,
                dirNameCars + "/" + fileNameFerrari);

        ZipMisc zipMisc = ZipMisc.zip(zip);
        zipMisc.removeEntryByName(entryNames);
        assertThat(zipMisc.getEntries()).hasSize(11);
    }

    public void shouldThrowExceptionWhenRemoveEntryByNameCollectionIsEmpty() {
        assertThatThrownBy(() -> ZipMisc.zip(zipStoreSolid).removeEntryByName(Collections.emptyList()))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    public void shouldThrowExceptionWhenOneOfRemovedEntriesNotFound() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        Zip4jvmSuite.createDir(zip.getParent());
        Zip4jvmSuite.copyFile(zipStoreSolid, zip);

        List<String> entryNames = Arrays.asList(
                dirNameCars + "/" + fileNameBentley,
                "nonexistent.txt");

        assertThatThrownBy(() -> ZipMisc.zip(zip).removeEntryByName(entryNames))
                .isExactlyInstanceOf(EntryNotFoundException.class);
    }

    // -- removeEntryByNamePrefix(String) -------------------------------------

    public void shouldRemoveEntriesByPrefix() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        Zip4jvmSuite.createDir(zip.getParent());
        Zip4jvmSuite.copyFile(zipStoreSolid, zip);

        ZipMisc zipMisc = ZipMisc.zip(zip);
        zipMisc.removeEntryByNamePrefix(dirNameCars);
        assertThat(zipMisc.getEntries()).hasSize(10);
    }

    public void shouldThrowExceptionWhenRemoveByPrefixIsBlank() {
        assertThatThrownBy(() -> ZipMisc.zip(zipStoreSolid).removeEntryByNamePrefix("  "))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    // -- removeEntryByNamePrefix(Collection<String>) -------------------------

    public void shouldRemoveEntriesByMultiplePrefixes() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        Zip4jvmSuite.createDir(zip.getParent());
        Zip4jvmSuite.copyFile(zipStoreSolid, zip);

        ZipMisc zipMisc = ZipMisc.zip(zip);
        zipMisc.removeEntryByNamePrefix(Arrays.asList(dirNameBikes, dirNameCars));
        assertThat(zipMisc.getEntries()).hasSize(6);
    }

    public void shouldThrowExceptionWhenRemoveByPrefixCollectionIsEmpty() {
        assertThatThrownBy(() -> ZipMisc.zip(zipStoreSolid).removeEntryByNamePrefix(Collections.emptyList()))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    // -- merge() -------------------------------------------------------------

    public void shouldMergeSplitZipIntoSingleFile() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path dst = dstDir.resolve(fileNameZipSrc);

        ZipMisc.zip(zipStoreSplit).merge(dst);
        assertThatZipFile(dst).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(dst).exists().root().matches(rootAssert);
    }

    public void shouldThrowExceptionWhenMergeCalledOnSolidZip() {
        Path dst = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        assertThatThrownBy(() -> ZipMisc.zip(zipStoreSolid).merge(dst))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Zip archive is not split");
    }

    public void shouldThrowExceptionWhenMergeDstIsNull() {
        assertThatThrownBy(() -> ZipMisc.zip(zipStoreSplit).merge(null))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

}
