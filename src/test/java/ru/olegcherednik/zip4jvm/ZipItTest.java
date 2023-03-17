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
import ru.olegcherednik.zip4jvm.utils.ReflectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.fileSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameCars;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 26.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipItTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipItTest.class);
    private static final Path defSingleZip = rootDir.resolve("def/single/src.zip");
    private static final Path defMultiZip = rootDir.resolve("def/multi/src.zip");
    private static final Path customSingleZip = rootDir.resolve("custom/single/src.zip");
    private static final Path customMultiZip = rootDir.resolve("custom/multi/src.zip");
    private static final Path defEntryZip = rootDir.resolve("def/entry/src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldCreateZipWhenAddRegularFileDefaultSettings() throws IOException {
        ZipIt.zip(defSingleZip).add(fileBentley);
        assertThatDirectory(defSingleZip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(defSingleZip).root().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(defSingleZip).file(fileNameBentley).exists().hasSize(1_395_362);
    }

    public void shouldCreateZipWhenAddDirectoryDefaultSettings() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        ZipIt.zip(zip).add(dirCars);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip).root().hasDirectories(1).hasRegularFiles(0);
        assertThatZipFile(zip).directory(zipDirNameCars).matches(dirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldCreateZipWhenAddRegularFileDefaultSettings")
    public void shouldAddRegularFileWhenZipExistsDefaultSettings() throws IOException {
        ZipIt.zip(defSingleZip).add(fileSaintPetersburg);
        assertThatDirectory(defSingleZip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(defSingleZip).root().hasDirectories(0).hasRegularFiles(2);
        assertThatZipFile(defSingleZip).file(fileNameBentley).exists().hasSize(1_395_362);
        assertThatZipFile(defSingleZip).file(fileNameSaintPetersburg).exists().hasSize(1_074_836);
    }

    @Test(dependsOnMethods = "shouldAddRegularFileWhenZipExistsDefaultSettings")
    public void shouldAddDirectoryWhenZipExistsDefaultSettings() throws IOException {
        ZipIt.zip(defSingleZip).add(dirCars);
        assertThatDirectory(defSingleZip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(defSingleZip).root().hasDirectories(1).hasRegularFiles(2);
        assertThatZipFile(defSingleZip).file(fileNameBentley).exists().hasSize(1_395_362);
        assertThatZipFile(defSingleZip).file(fileNameSaintPetersburg).exists().hasSize(1_074_836);
        assertThatZipFile(defSingleZip).directory(zipDirNameCars).matches(dirCarsAssert);
    }

    public void shouldCreateZipWhenAddRegularFilesAndDirectoriesAndDefaultSettings() throws IOException {
        ZipIt.zip(defMultiZip).add(Arrays.asList(fileHonda, dirCars));
        assertThatDirectory(defMultiZip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(defMultiZip).root().hasDirectories(1).hasRegularFiles(1);
        assertThatZipFile(defMultiZip).file(fileNameHonda).exists().hasSize(154_591);
        assertThatZipFile(defMultiZip).directory(zipDirNameCars).matches(dirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldCreateZipWhenAddRegularFilesAndDirectoriesAndDefaultSettings")
    public void shouldAddRegularFilesAndDirectoriesWhenZipExistsDefaultSettings() throws IOException {
        ZipIt.zip(defMultiZip).add(Arrays.asList(fileSaintPetersburg, dirBikes));
        assertThatDirectory(defMultiZip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(defMultiZip).root().hasDirectories(2).hasRegularFiles(2);
        assertThatZipFile(defMultiZip).file(fileNameHonda).exists().hasSize(154_591);
        assertThatZipFile(defMultiZip).file(fileNameSaintPetersburg).exists().hasSize(1_074_836);
        assertThatZipFile(defMultiZip).directory(zipDirNameCars).matches(dirCarsAssert);
        assertThatZipFile(defMultiZip).directory(zipDirNameBikes).matches(dirBikesAssert);
    }

    public void shouldThrowExceptionWhenAddNullPathAndDefaultSettings() {
        assertThatThrownBy(() -> ZipIt.zip(defSingleZip).add((Path)null)).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    public void shouldThrowExceptionWhenAddNullPathAndCustomSettings() {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();
        assertThatThrownBy(() -> ZipIt.zip(customSingleZip).settings(settings).add((Path)null)).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    public void shouldCreateZipWhenAddRegularFileAndCustomSettings() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();

        ZipIt.zip(customSingleZip).settings(settings).add(fileBentley);
        assertThatDirectory(customSingleZip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(customSingleZip).root().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(customSingleZip).file(fileNameBentley).exists().hasSize(1_395_362);
    }

    public void shouldCreateZipWhenAddDirectoryAndCustomSettings() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        ZipIt.zip(zip).settings(settings).add(dirCars);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip).root().hasDirectories(1).hasRegularFiles(0);
        assertThatZipFile(zip).directory(zipDirNameCars).matches(dirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldCreateZipWhenAddRegularFileAndCustomSettings")
    public void shouldAddRegularFileWhenZipExistsCustomSettings() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();

        ZipIt.zip(customSingleZip).settings(settings).add(fileSaintPetersburg);
        assertThatDirectory(customSingleZip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(customSingleZip).root().hasDirectories(0).hasRegularFiles(2);
        assertThatZipFile(customSingleZip).file(fileNameBentley).exists().hasSize(1_395_362);
        assertThatZipFile(customSingleZip).file(fileNameSaintPetersburg).exists().hasSize(1_074_836);
    }

    @Test(dependsOnMethods = "shouldAddRegularFileWhenZipExistsCustomSettings")
    public void shouldAddDirectoryWhenZipExistsCustomSettings() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();

        ZipIt.zip(customSingleZip).settings(settings).add(dirCars);
        assertThatDirectory(customSingleZip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(customSingleZip).root().hasDirectories(1).hasRegularFiles(2);
        assertThatZipFile(customSingleZip).file(fileNameBentley).exists().hasSize(1_395_362);
        assertThatZipFile(customSingleZip).file(fileNameSaintPetersburg).exists().hasSize(1_074_836);
        assertThatZipFile(customSingleZip).directory(zipDirNameCars).matches(dirCarsAssert);
    }

    public void shouldCreateZipWhenAddRegularFilesAndDirectoriesAndCustomSettings() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();

        ZipIt.zip(customMultiZip).settings(settings).add(Arrays.asList(fileHonda, dirCars));
        assertThatDirectory(customMultiZip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(customMultiZip).root().hasDirectories(1).hasRegularFiles(1);
        assertThatZipFile(customMultiZip).file(fileNameHonda).exists().hasSize(154_591);
        assertThatZipFile(customMultiZip).directory(zipDirNameCars).matches(dirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldCreateZipWhenAddRegularFilesAndDirectoriesAndCustomSettings")
    public void shouldAddRegularFilesAndDirectoriesWhenZipExistsCustomSettings() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();

        ZipIt.zip(customMultiZip).settings(settings).add(Arrays.asList(fileSaintPetersburg, dirBikes));
        assertThatDirectory(customMultiZip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(customMultiZip).root().hasDirectories(2).hasRegularFiles(2);
        assertThatZipFile(customMultiZip).file(fileNameHonda).exists().hasSize(154_591);
        assertThatZipFile(customMultiZip).file(fileNameSaintPetersburg).exists().hasSize(1_074_836);
        assertThatZipFile(customMultiZip).directory(zipDirNameCars).matches(dirCarsAssert);
        assertThatZipFile(customMultiZip).directory(zipDirNameBikes).matches(dirBikesAssert);
    }

    public void shouldCreateZipWhenAddRegularFileDefaultSettingsZipEntry() throws IOException {
        try (ZipFile.Writer zipFile = ZipIt.zip(defEntryZip).open()) {
            zipFile.add(fileBentley, "foo.jpg");
        }

        assertThatDirectory(defEntryZip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(defEntryZip).root().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(defEntryZip).file("foo.jpg").exists().hasSize(1_395_362);
    }

    public void shouldUseDefaultZipSettingsWhenSetNull() throws NoSuchFieldException, IllegalAccessException {
        ZipIt zipIt = ZipIt.zip(defEntryZip);
        assertThat(getSettings(zipIt)).isSameAs(ZipSettings.DEFAULT);

        ZipSettings settings = ZipSettings.builder().comment("comment").build();
        zipIt.settings(settings);
        assertThat(getSettings(zipIt)).isSameAs(settings);

        zipIt.settings(null);
        assertThat(getSettings(zipIt)).isSameAs(ZipSettings.DEFAULT);
    }

    public void shouldUseDefaultZipEntrySettingsWhenSetNull() throws NoSuchFieldException, IllegalAccessException {
        ZipIt zipIt = ZipIt.zip(defEntryZip);
        assertThat(getSettings(zipIt).getEntrySettingsProvider()).isSameAs(ZipEntrySettings.DEFAULT_PROVIDER);

        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        zipIt.entrySettings(entrySettings);
        assertThat(getSettings(zipIt).getEntrySettingsProvider().apply("aa")).isSameAs(entrySettings);

        zipIt.entrySettings((ZipEntrySettings)null);
        assertThat(getSettings(zipIt).getEntrySettingsProvider()).isSameAs(ZipEntrySettings.DEFAULT_PROVIDER);
    }

    private static ZipSettings getSettings(ZipIt zipIt) throws NoSuchFieldException, IllegalAccessException {
        return ReflectionUtils.getFieldValue(zipIt, "settings");
    }

}
