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
package ru.olegcherednik.zip4jvm.zipit;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettingsProvider;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.utils.ReflectionUtils;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.fileSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 26.09.2019
 */
@Test
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class ZipItTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    private static final Path DEF_SINGLE_ZIP = DIR_ROOT.resolve("def/single/src.zip");
    private static final Path DEF_MULTI_ZIP = DIR_ROOT.resolve("def/multi/src.zip");
    private static final Path CUSTOM_SINGLE_ZIP = DIR_ROOT.resolve("custom/single/src.zip");
    private static final Path CUSTOM_MULTI_ZIP = DIR_ROOT.resolve("custom/multi/src.zip");
    private static final Path DEF_ENTRY_ZIP = DIR_ROOT.resolve("def/entry/src.zip");

    @BeforeClass
    public static void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldCreateZipWhenAddRegularFileDefaultSettings() throws IOException {
        ZipIt.zip(DEF_SINGLE_ZIP).add(fileBentley);
        assertThatZipFile(DEF_SINGLE_ZIP).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(DEF_SINGLE_ZIP).root().hasOnlyRegularFiles(1);
        assertThatZipFile(DEF_SINGLE_ZIP).regularFile(fileNameBentley).matches(fileBentleyAssert);
    }

    public void shouldCreateZipWhenAddDirectoryDefaults() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("src.zip");

        ZipIt.zip(zip).add(dirCars);
        assertThatZipFile(zip).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).root().hasEntries(1).hasDirectories(1);
        assertThatZipFile(zip).directory(dirNameCars).matches(dirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldCreateZipWhenAddRegularFileDefaultSettings")
    public void shouldAddRegularFileWhenZipExistsDefaultSettings() throws IOException {
        ZipIt.zip(DEF_SINGLE_ZIP).add(fileSaintPetersburg);
        assertThatZipFile(DEF_SINGLE_ZIP).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(DEF_SINGLE_ZIP).root().hasOnlyRegularFiles(2);
        assertThatZipFile(DEF_SINGLE_ZIP).regularFile(fileNameBentley).exists().matches(fileBentleyAssert);
        assertThatZipFile(DEF_SINGLE_ZIP).regularFile(fileNameSaintPetersburg).exists().hasSize(1_074_836);
    }

    @Test(dependsOnMethods = "shouldAddRegularFileWhenZipExistsDefaultSettings")
    public void shouldAddDirectoryWhenZipExistsDefaultSettings() throws IOException {
        ZipIt.zip(DEF_SINGLE_ZIP).add(dirCars);
        assertThatZipFile(DEF_SINGLE_ZIP).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(DEF_SINGLE_ZIP).root().hasEntries(3).hasDirectories(1).hasRegularFiles(2);
        assertThatZipFile(DEF_SINGLE_ZIP).regularFile(fileNameBentley).exists().matches(fileBentleyAssert);
        assertThatZipFile(DEF_SINGLE_ZIP).regularFile(fileNameSaintPetersburg).exists().hasSize(1_074_836);
        assertThatZipFile(DEF_SINGLE_ZIP).directory(dirNameCars).matches(dirCarsAssert);
    }

    public void shouldCreateZipWhenAddRegularFilesAndDirectoriesAndDefaultSettings() {
        ZipIt.zip(DEF_MULTI_ZIP).add(Arrays.asList(fileHonda, dirCars));
        assertThatZipFile(DEF_MULTI_ZIP).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(DEF_MULTI_ZIP).root().hasEntries(2).hasDirectories(1).hasRegularFiles(1);
        assertThatZipFile(DEF_MULTI_ZIP).regularFile(fileNameHonda).exists().hasSize(154_591);
        assertThatZipFile(DEF_MULTI_ZIP).directory(dirNameCars).matches(dirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldCreateZipWhenAddRegularFilesAndDirectoriesAndDefaultSettings")
    public void shouldAddRegularFilesAndDirectoriesWhenZipExistsDefaultSettings() {
        ZipIt.zip(DEF_MULTI_ZIP).add(Arrays.asList(fileSaintPetersburg, dirBikes));
        assertThatZipFile(DEF_MULTI_ZIP).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(DEF_MULTI_ZIP).root().hasEntries(4).hasDirectories(2).hasRegularFiles(2);
        assertThatZipFile(DEF_MULTI_ZIP).regularFile(fileNameHonda).exists().hasSize(154_591);
        assertThatZipFile(DEF_MULTI_ZIP).regularFile(fileNameSaintPetersburg).exists().hasSize(1_074_836);
        assertThatZipFile(DEF_MULTI_ZIP).directory(dirNameCars).matches(dirCarsAssert);
        assertThatZipFile(DEF_MULTI_ZIP).directory(dirNameBikes).matches(dirBikesAssert);
    }

    public void shouldThrowExceptionWhenAddNullPathAndDefaultSettings() {
        assertThatThrownBy(() -> ZipIt.zip(DEF_SINGLE_ZIP)
                                      .add((Path) null)).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    public void shouldThrowExceptionWhenAddNullPathAndCustomSettings() {
        assertThatThrownBy(() -> ZipIt.zip(CUSTOM_SINGLE_ZIP)
                                      .settings(ZipSettings.of(CompressionEnum.STORE))
                                      .add((Path) null)).isExactlyInstanceOf(
                IllegalArgumentException.class);
    }

    public void shouldCreateZipWhenAddRegularFileAndCustomSettings() throws IOException {
        ZipIt.zip(CUSTOM_SINGLE_ZIP).settings(ZipSettings.of(CompressionEnum.STORE)).add(fileBentley);
        assertThatZipFile(CUSTOM_SINGLE_ZIP).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(CUSTOM_SINGLE_ZIP).root().hasOnlyRegularFiles(1);
        assertThatZipFile(CUSTOM_SINGLE_ZIP).regularFile(fileNameBentley).matches(fileBentleyAssert);
    }

    public void shouldCreateZipWhenAddDirectoryAndCustomSettings() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("src.zip");
        ZipIt.zip(zip).settings(ZipSettings.of(CompressionEnum.STORE)).add(dirCars);
        assertThatZipFile(zip).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).root().hasEntries(1).hasDirectories(1);
        assertThatZipFile(zip).directory(dirNameCars).matches(dirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldCreateZipWhenAddRegularFileAndCustomSettings")
    public void shouldAddRegularFileWhenZipExistsCustomSettings() throws IOException {
        ZipIt.zip(CUSTOM_SINGLE_ZIP).settings(ZipSettings.of(CompressionEnum.STORE)).add(fileSaintPetersburg);
        assertThatZipFile(CUSTOM_SINGLE_ZIP).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(CUSTOM_SINGLE_ZIP).root().hasEntries(2).hasRegularFiles(2);
        assertThatZipFile(CUSTOM_SINGLE_ZIP).regularFile(fileNameBentley).exists().matches(fileBentleyAssert);
        assertThatZipFile(CUSTOM_SINGLE_ZIP).regularFile(fileNameSaintPetersburg).exists().hasSize(1_074_836);
    }

    @Test(dependsOnMethods = "shouldAddRegularFileWhenZipExistsCustomSettings")
    public void shouldAddDirectoryWhenZipExistsCustomSettings() throws IOException {
        ZipIt.zip(CUSTOM_SINGLE_ZIP).settings(ZipSettings.of(CompressionEnum.STORE)).add(dirCars);
        assertThatZipFile(CUSTOM_SINGLE_ZIP).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(CUSTOM_SINGLE_ZIP).root().hasEntries(3).hasDirectories(1).hasRegularFiles(2);
        assertThatZipFile(CUSTOM_SINGLE_ZIP).regularFile(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(CUSTOM_SINGLE_ZIP).regularFile(fileNameSaintPetersburg).exists().hasSize(1_074_836);
        assertThatZipFile(CUSTOM_SINGLE_ZIP).directory(dirNameCars).matches(dirCarsAssert);
    }

    public void shouldCreateZipWhenAddRegularFilesAndDirectoriesAndCustomSettings() {
        ZipIt.zip(CUSTOM_MULTI_ZIP).settings(ZipSettings.of(CompressionEnum.STORE))
             .add(Arrays.asList(fileHonda, dirCars));
        assertThatZipFile(CUSTOM_MULTI_ZIP).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(CUSTOM_MULTI_ZIP).root().hasEntries(2).hasDirectories(1).hasRegularFiles(1);
        assertThatZipFile(CUSTOM_MULTI_ZIP).regularFile(fileNameHonda).exists().hasSize(154_591);
        assertThatZipFile(CUSTOM_MULTI_ZIP).directory(dirNameCars).matches(dirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldCreateZipWhenAddRegularFilesAndDirectoriesAndCustomSettings")
    public void shouldAddRegularFilesAndDirectoriesWhenZipExistsCustomSettings() {
        ZipIt.zip(CUSTOM_MULTI_ZIP).settings(ZipSettings.of(CompressionEnum.STORE))
             .add(Arrays.asList(fileSaintPetersburg, dirBikes));
        assertThatZipFile(CUSTOM_MULTI_ZIP).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(CUSTOM_MULTI_ZIP).root().hasEntries(4).hasDirectories(2).hasRegularFiles(2);
        assertThatZipFile(CUSTOM_MULTI_ZIP).regularFile(fileNameHonda).exists().hasSize(154_591);
        assertThatZipFile(CUSTOM_MULTI_ZIP).regularFile(fileNameSaintPetersburg).exists().hasSize(1_074_836);
        assertThatZipFile(CUSTOM_MULTI_ZIP).directory(dirNameCars).matches(dirCarsAssert);
        assertThatZipFile(CUSTOM_MULTI_ZIP).directory(dirNameBikes).matches(dirBikesAssert);
    }

    public void shouldCreateZipWhenAddRegularFileDefaultSettingsZipEntry() {
        ZipIt.zip(DEF_ENTRY_ZIP).execute(zipFile -> zipFile.add(fileBentley, "foo.jpg"));

        assertThatZipFile(DEF_ENTRY_ZIP).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(DEF_ENTRY_ZIP).root().hasOnlyRegularFiles(1);
        assertThatZipFile(DEF_ENTRY_ZIP).regularFile("foo.jpg").matches(fileBentleyAssert);
    }

    public void shouldUseDefaultZipSettingsWhenSetNull() throws NoSuchFieldException, IllegalAccessException {
        ZipIt zipIt = ZipIt.zip(DEF_ENTRY_ZIP);
        assertThat(getSettings(zipIt)).isSameAs(ZipSettings.DEFAULT);

        ZipSettings settings = ZipSettings.builder().comment("comment").build();
        zipIt.settings(settings);
        assertThat(getSettings(zipIt)).isSameAs(settings);

        zipIt.settings(null);
        assertThat(getSettings(zipIt)).isSameAs(ZipSettings.DEFAULT);
    }

    public void shouldUseDefaultZipEntrySettingsWhenSetNull() throws NoSuchFieldException, IllegalAccessException {
        ZipIt zipIt = ZipIt.zip(DEF_ENTRY_ZIP);
        assertThat(getSettings(zipIt).getEntrySettingsProvider()).isSameAs(ZipEntrySettingsProvider.DEFAULT);

        ZipEntrySettings entrySettings = ZipEntrySettings.of(CompressionEnum.STORE);
        zipIt.entrySettings(entrySettings);
        assertThat(getSettings(zipIt).getEntrySettings("aa")).isSameAs(entrySettings);

        zipIt.entrySettings((ZipEntrySettings) null);
        assertThat(getSettings(zipIt).getEntrySettingsProvider()).isSameAs(ZipEntrySettingsProvider.DEFAULT);
    }

    private static ZipSettings getSettings(ZipIt zipIt) throws NoSuchFieldException, IllegalAccessException {
        return ReflectionUtils.getFieldValue(zipIt, "settings");
    }

}
