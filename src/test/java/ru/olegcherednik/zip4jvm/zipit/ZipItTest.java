/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettingsProvider;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.utils.ReflectionUtils;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.fileNameWiesmann;
import static ru.olegcherednik.zip4jvm.TestData.fileSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.fileWiesmann;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileFerrariAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileHondaAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSaintPetersburgAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileWiesmannAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 26.09.2019
 */
@Test
public class ZipItTest extends BaseTest {

    private final Path defSingleZip = resolve("def/single/src.zip");
    private final Path defMultiZip = resolve("def/multi/src.zip");
    private final Path customSingleZip = resolve("custom/single/src.zip");
    private final Path customMultiZip = resolve("custom/multi/src.zip");
    private final Path defEntryZip = resolve("def/entry/src.zip");

    public void shouldCreateZipWhenAddRegularFileDefaultSettings() {
        ZipIt.zip(defSingleZip).add(fileBentley);
        assertThatZipFile(defSingleZip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyRegularFiles(1)
                .withRegularFile(fileNameBentley, fileBentleyAssert);
    }

    public void shouldCreateZipWhenAddDirectoryDefaults() {
        Path zip = getZip();

        ZipIt.zip(zip).add(dirCars);

        assertThatZipFile(zip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyDirectories(1)
                .withDirectory(dirNameCars, dirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldCreateZipWhenAddRegularFileDefaultSettings")
    public void shouldAddRegularFileWhenZipExistsDefaultSettings() {
        ZipIt.zip(defSingleZip).add(fileSaintPetersburg);

        assertThatZipFile(defSingleZip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyRegularFiles(2)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFile(fileNameSaintPetersburg, fileSaintPetersburgAssert);
    }

    @Test(dependsOnMethods = "shouldAddRegularFileWhenZipExistsDefaultSettings")
    public void shouldAddDirectoryWhenZipExistsDefaultSettings() {
        ZipIt.zip(defSingleZip).add(dirCars);

        assertThatZipFile(defSingleZip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyDirectoriesRegularFiles(1, 2)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFile(fileNameSaintPetersburg, fileSaintPetersburgAssert)
                .withDirectory(dirNameCars, dirCarsAssert);
    }

    public void shouldCreateZipWhenAddRegularFilesAndDirectoriesAndDefaultSettings() {
        ZipIt.zip(defMultiZip).add(Arrays.asList(fileHonda, dirCars));

        assertThatZipFile(defMultiZip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyDirectoriesRegularFiles(1, 1)
                .withRegularFile(fileNameHonda, fileHondaAssert)
                .withDirectory(dirNameCars, dirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldCreateZipWhenAddRegularFilesAndDirectoriesAndDefaultSettings")
    public void shouldAddRegularFilesAndDirectoriesWhenZipExistsDefaultSettings() {
        ZipIt.zip(defMultiZip).add(Arrays.asList(fileSaintPetersburg, dirBikes));

        assertThatZipFile(defMultiZip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyDirectoriesRegularFiles(2, 2)
                .withRegularFile(fileNameHonda, fileHondaAssert)
                .withRegularFile(fileNameSaintPetersburg, fileSaintPetersburgAssert)
                .withDirectory(dirNameCars, dirCarsAssert)
                .withDirectory(dirNameBikes, dirBikesAssert);
    }

    public void shouldCreateZipWhenAddRegularFileAndCustomSettings() {
        ZipIt.zip(customSingleZip).settings(ZipSettings.of(CompressionEnum.STORE)).add(fileBentley);

        assertThatZipFile(customSingleZip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyRegularFiles(1)
                .withRegularFile(fileNameBentley, fileBentleyAssert);
    }

    public void shouldCreateZipWhenAddDirectoryAndCustomSettings() {
        Path zip = getTestRoot().resolve("src.zip");
        ZipIt.zip(zip).settings(ZipSettings.of(CompressionEnum.STORE)).add(dirCars);

        assertThatZipFile(zip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyDirectories(1)
                .withDirectory(dirNameCars, dirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldCreateZipWhenAddRegularFileAndCustomSettings")
    public void shouldAddRegularFileWhenZipExistsCustomSettings() {
        ZipIt.zip(customSingleZip).settings(ZipSettings.of(CompressionEnum.STORE)).add(fileSaintPetersburg);

        assertThatZipFile(customSingleZip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyRegularFiles(2)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFile(fileNameSaintPetersburg, fileSaintPetersburgAssert);
    }

    @Test(dependsOnMethods = "shouldAddRegularFileWhenZipExistsCustomSettings")
    public void shouldAddDirectoryWhenZipExistsCustomSettings() {
        ZipIt.zip(customSingleZip).settings(ZipSettings.of(CompressionEnum.STORE)).add(dirCars);

        assertThatZipFile(customSingleZip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyDirectoriesRegularFiles(1, 2)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFile(fileNameSaintPetersburg, fileSaintPetersburgAssert)
                .withDirectory(dirNameCars, dirCarsAssert);
    }

    public void shouldCreateZipWhenAddRegularFilesAndDirectoriesAndCustomSettings() {
        ZipIt.zip(customMultiZip).settings(ZipSettings.of(CompressionEnum.STORE))
             .add(Arrays.asList(fileHonda, dirCars));

        assertThatZipFile(customMultiZip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyDirectoriesRegularFiles(1, 1)
                .withRegularFile(fileNameHonda, fileHondaAssert)
                .withDirectory(dirNameCars, dirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldCreateZipWhenAddRegularFilesAndDirectoriesAndCustomSettings")
    public void shouldAddRegularFilesAndDirectoriesWhenZipExistsCustomSettings() {
        ZipIt.zip(customMultiZip).settings(ZipSettings.of(CompressionEnum.STORE))
             .add(Arrays.asList(fileSaintPetersburg, dirBikes));

        assertThatZipFile(customMultiZip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyDirectoriesRegularFiles(2, 2)
                .withRegularFile(fileNameHonda, file -> file.hasSize(154_591))
                .withRegularFile(fileNameSaintPetersburg, file -> file.hasSize(1_074_836))
                .withDirectory(dirNameCars, dirCarsAssert)
                .withDirectory(dirNameBikes, dirBikesAssert);
    }

    public void shouldCreateZipWhenAddRegularFileDefaultSettingsZipEntry() {
        ZipIt.zip(defEntryZip).execute(zipFile -> zipFile.add(fileBentley, "foo.jpg"));

        assertThatZipFile(defEntryZip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyRegularFiles(1)
                .withRegularFile("foo.jpg", fileBentleyAssert);
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
        assertThat(getSettings(zipIt).getEntrySettingsProvider()).isSameAs(ZipEntrySettingsProvider.DEFAULT);

        ZipEntrySettings entrySettings = ZipEntrySettings.of(CompressionEnum.STORE);
        zipIt.entrySettings(entrySettings);
        assertThat(getSettings(zipIt).getEntrySettings("aa")).isSameAs(entrySettings);

        zipIt.entrySettings((ZipEntrySettings) null);
        assertThat(getSettings(zipIt).getEntrySettingsProvider()).isSameAs(ZipEntrySettingsProvider.DEFAULT);
    }

    public void shouldAcceptVarargsWhenInvokeAdd() {
        Path zip = getTestRoot().resolve("src.zip");

        ZipIt.zip(zip).add(fileBentley);
        assertThatZipFile(zip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyRegularFiles(1)
                .withRegularFile(fileNameBentley, fileBentleyAssert);

        ZipIt.zip(zip).add(fileFerrari, fileWiesmann);
        assertThatZipFile(zip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyRegularFiles(3)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFile(fileNameFerrari, fileFerrariAssert)
                .withRegularFile(fileNameWiesmann, fileWiesmannAssert);
    }

    private static ZipSettings getSettings(ZipIt zipIt) throws NoSuchFieldException, IllegalAccessException {
        return ReflectionUtils.getFieldValue(zipIt, "settings");
    }

}
