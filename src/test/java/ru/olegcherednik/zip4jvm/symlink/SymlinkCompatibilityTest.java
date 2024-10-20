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
package ru.olegcherednik.zip4jvm.symlink;

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.assertj.IDirectoryAssert;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileNameFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileNameMcdonnelDouglas;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.fileNameWiesmann;
import static ru.olegcherednik.zip4jvm.TestData.symlinkAbsDirNameData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkAbsFileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.symlinkAbsFileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelDirNameData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelFileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelFileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.symlinkTrnDirNameData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkTrnFileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameEmpty;
import static ru.olegcherednik.zip4jvm.TestData.zipSymlinkAbsDirNameData;
import static ru.olegcherednik.zip4jvm.TestData.zipSymlinkRelDirNameCars;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirEmptyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileDucatiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileEmptyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileFerrariAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileMcdonnelDouglasAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSaintPetersburgAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileWiesmannAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 18.03.2023
 */
// TODO commented because of the problem in GitHub Actions
//@Test
public class SymlinkCompatibilityTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(SymlinkCompatibilityTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldUnzipPosixZipWithSymlink() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Path zip = Paths.get("src/test/resources/symlink/posix/unique-symlink-target.zip").toAbsolutePath();

        UnzipIt.zip(zip).destDir(destDir).extract();

        assertThatDirectory(destDir).exists().hasEntries(10).hasDirectories(2).hasRegularFiles(1).hasSymlinks(7);
        assertThatDirectory(destDir).directory(zipSymlinkRelDirNameCars).matches(dirSymlinkCarsAssert);
        assertThatDirectory(destDir).directory(zipSymlinkAbsDirNameData).matches(dir -> {
            dir.exists().hasEntries(8).hasDirectories(2).hasRegularFiles(5).hasSymlinks(1);
            dirBikesAssert.accept(dir.directory(zipDirNameBikes));
            dirEmptyAssert.accept(dir.directory(zipDirNameEmpty));
            dir.symlink(dirNameCars).exists().hasTarget("../" + zipSymlinkRelDirNameCars + zipSymlinkRelDirNameCars);
            fileMcdonnelDouglasAssert.accept(dir.regularFile(fileNameMcdonnelDouglas));
            fileSaintPetersburgAssert.accept(dir.regularFile(fileNameSaintPetersburg));
            fileEmptyAssert.accept(dir.regularFile(fileNameEmpty));
        });
        assertThatDirectory(destDir).regularFile(fileNameDucati).matches(fileDucatiAssert);
        assertThatDirectory(destDir).symlink(symlinkRelDirNameData).hasTarget(symlinkAbsDirNameData + '/');
        assertThatDirectory(destDir).symlink(symlinkTrnDirNameData).hasTarget(symlinkAbsDirNameData + '/');
        assertThatDirectory(destDir).symlink(symlinkAbsFileNameDucati).hasTarget(fileNameDucati);
        assertThatDirectory(destDir).symlink(symlinkRelFileNameDucati).hasTarget(fileNameDucati);
        assertThatDirectory(destDir).symlink(symlinkAbsFileNameHonda).hasTarget(
                zipSymlinkAbsDirNameData + zipDirNameBikes + fileNameHonda);
        assertThatDirectory(destDir).symlink(symlinkRelFileNameHonda).hasTarget(
                zipSymlinkAbsDirNameData + zipDirNameBikes + fileNameHonda);
        assertThatDirectory(destDir).symlink(symlinkTrnFileNameHonda).hasTarget(
                zipSymlinkAbsDirNameData + zipDirNameBikes + fileNameHonda);
    }

    public void shouldUnzipWinZipWithSymlink() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Path zip = Paths.get("src/test/resources/symlink/win/unique-symlink-target.zip").toAbsolutePath();

        UnzipIt.zip(zip).destDir(destDir).extract();

        assertThatDirectory(destDir).exists().hasEntries(10).hasDirectories(2).hasRegularFiles(1).hasSymlinks(7);
        assertThatDirectory(destDir).directory(zipSymlinkRelDirNameCars).matches(dirSymlinkCarsAssert);
        assertThatDirectory(destDir).directory(zipSymlinkAbsDirNameData).matches(dir -> {
            dir.exists().hasEntries(8).hasDirectories(2).hasRegularFiles(5).hasSymlinks(1);
            dirBikesAssert.accept(dir.directory(zipDirNameBikes));
            dirEmptyAssert.accept(dir.directory(zipDirNameEmpty));
            dir.symlink(dirNameCars).exists().hasTarget("../" + zipSymlinkRelDirNameCars + zipSymlinkRelDirNameCars);
            fileMcdonnelDouglasAssert.accept(dir.regularFile(fileNameMcdonnelDouglas));
            fileSaintPetersburgAssert.accept(dir.regularFile(fileNameSaintPetersburg));
            fileEmptyAssert.accept(dir.regularFile(fileNameEmpty));
        });
        assertThatDirectory(destDir).regularFile(fileNameDucati).matches(fileDucatiAssert);
        assertThatDirectory(destDir).symlink(symlinkRelDirNameData).hasTarget(symlinkAbsDirNameData + '/');
        assertThatDirectory(destDir).symlink(symlinkTrnDirNameData).hasTarget(symlinkAbsDirNameData + '/');
        assertThatDirectory(destDir).symlink(symlinkAbsFileNameDucati).hasTarget(fileNameDucati);
        assertThatDirectory(destDir).symlink(symlinkRelFileNameDucati).hasTarget(fileNameDucati);
        assertThatDirectory(destDir).symlink(symlinkAbsFileNameHonda).hasTarget(
                zipSymlinkAbsDirNameData + zipDirNameBikes + fileNameHonda);
        assertThatDirectory(destDir).symlink(symlinkRelFileNameHonda).hasTarget(
                zipSymlinkAbsDirNameData + zipDirNameBikes + fileNameHonda);
        assertThatDirectory(destDir).symlink(symlinkTrnFileNameHonda).hasTarget(
                zipSymlinkAbsDirNameData + zipDirNameBikes + fileNameHonda);
    }

    @SuppressWarnings("FieldNamingConvention")
    private static final Consumer<IDirectoryAssert<?>> dirSymlinkCarsAssert = dir -> {
        dir.exists().hasEntries(4).hasDirectories(1).hasRegularFiles(3);
        dirCarsAssert.accept(dir.directory(zipSymlinkRelDirNameCars));
        fileBentleyAssert.accept(dir.regularFile(fileNameBentley));
        fileFerrariAssert.accept(dir.regularFile(fileNameFerrari));
        fileWiesmannAssert.accept(dir.regularFile(fileNameWiesmann));
    };
}
