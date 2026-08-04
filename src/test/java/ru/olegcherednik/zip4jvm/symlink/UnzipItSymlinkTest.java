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
package ru.olegcherednik.zip4jvm.symlink;

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.assertj.IDirectoryAssert;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.function.Consumer;

import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileNameFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameMcdonnelDouglas;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSigSauer;
import static ru.olegcherednik.zip4jvm.TestData.fileNameWiesmann;
import static ru.olegcherednik.zip4jvm.TestData.symlinkPosixZip;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelDirNameCars;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirEmptyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileDucatiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileEmptyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileFerrariAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileHondaAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileMcDonnellDouglasAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSaintPetersburgAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSigSauerAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileWiesmannAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 05.08.2026
 */
@Test
public class UnzipItSymlinkTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @SuppressWarnings("FieldNamingConvention")
    private static final Consumer<IDirectoryAssert<?>> dirSymlinkCarsAssert = dir -> {
        dir.exists().hasEntries(4).hasDirectories(1).hasRegularFiles(3);
        dirCarsAssert.accept(dir.directory(symlinkRelDirNameCars));
        fileBentleyAssert.accept(dir.regularFile(fileNameBentley));
        fileFerrariAssert.accept(dir.regularFile(fileNameFerrari));
        fileWiesmannAssert.accept(dir.regularFile(fileNameWiesmann));
    };

    @SuppressWarnings("FieldNamingConvention")
    private static final Consumer<IDirectoryAssert<?>> dirSymlinkDataAssert = dir -> {
        dir.exists().hasEntries(7).hasDirectories(2).hasRegularFiles(5);
        dir.directory(dirNameBikes).matches(dirBikesAssert);
        dir.directory(dirNameEmpty).matches(dirEmptyAssert);
        dir.regularFile(fileNameEmpty).matches(fileEmptyAssert);
        dir.regularFile(fileNameMcdonnelDouglas).matches(fileMcDonnellDouglasAssert);
        dir.regularFile(fileNameSaintPetersburg).matches(fileSaintPetersburgAssert);
        dir.regularFile(fileNameSigSauer).matches(fileSigSauerAssert);
    };

    @BeforeClass
    public static void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldIgnoreSymlinkWhenUnzipWithDefaultSettings() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);

        UnzipIt.zip(symlinkPosixZip).dstDir(dstDir).extract();

        assertThatDirectory(dstDir).exists().hasEntries(3).hasDirectories(2).hasRegularFiles(1);
        assertThatDirectory(dstDir).directory("cars-rel-symlink").matches(dirSymlinkCarsAssert);
        assertThatDirectory(dstDir).directory("data-abs-symlink").matches(dirSymlinkDataAssert);
        assertThatDirectory(dstDir).regularFile(fileNameDucati).matches(fileDucatiAssert);
    }

    public void shouldIgnoreSymlinkWhenUnzipWithIgnoreSymlinkTrue() {
        UnzipSettings settings = UnzipSettings.builder().ignoreSymlink(true).build();
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);

        UnzipIt.zip(symlinkPosixZip).settings(settings).dstDir(dstDir).extract();

        assertThatDirectory(dstDir).exists().hasEntries(3).hasDirectories(2).hasRegularFiles(1);
        assertThatDirectory(dstDir).directory("cars-rel-symlink").matches(dirSymlinkCarsAssert);
        assertThatDirectory(dstDir).directory("data-abs-symlink").matches(dirSymlinkDataAssert);
        assertThatDirectory(dstDir).regularFile(fileNameDucati).matches(fileDucatiAssert);
    }

    public void shouldNotIgnoreSymlinkWhenUnzipWithIgnoreSymlinkFalse() {
        UnzipSettings settings = UnzipSettings.builder().ignoreSymlink(false).build();
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);

        UnzipIt.zip(symlinkPosixZip).settings(settings).dstDir(dstDir).extract();

        assertThatDirectory(dstDir).exists().hasEntries(10).hasDirectories(2).hasRegularFiles(1).hasSymlinks(7);
        assertThatDirectory(dstDir).directory("cars-rel-symlink").matches(dirSymlinkCarsAssert);
        assertThatDirectory(dstDir).directory("data-abs-symlink").matches(dirSymlinkDataAssert);
        assertThatDirectory(dstDir).symlink("data-rel-symlink").directory().matches(dirSymlinkDataAssert);
        assertThatDirectory(dstDir).symlink("data-trn-symlink").directory().matches(dirSymlinkDataAssert);
        assertThatDirectory(dstDir).regularFile(fileNameDucati).matches(fileDucatiAssert);
        assertThatDirectory(dstDir).symlink("ducati-panigale-1199-abs-symlink.jpg")
                                   .regularFile().matches(fileDucatiAssert);
        assertThatDirectory(dstDir).symlink("ducati-panigale-1199-rel-symlink.jpg")
                                   .regularFile().matches(fileDucatiAssert);
        assertThatDirectory(dstDir).symlink("honda-cbr600rr-abs-symlink.jpg").regularFile().matches(fileHondaAssert);
        assertThatDirectory(dstDir).symlink("honda-cbr600rr-rel-symlink.jpg").regularFile().matches(fileHondaAssert);
        assertThatDirectory(dstDir).symlink("honda-cbr600rr-trn-symlink.jpg").regularFile().matches(fileHondaAssert);
    }

}
