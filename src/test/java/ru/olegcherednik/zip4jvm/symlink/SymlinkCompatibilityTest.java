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

import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameEmpty;
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
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelDirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelDirNameData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelFileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelFileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.symlinkTrnDirNameData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkTrnFileNameHonda;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirEmptyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileDucatiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileEmptyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileFerrariAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileMcDonnellDouglasAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSaintPetersburgAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileWiesmannAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 18.03.2023
 */
// TODO commented because of the problem in GitHub Actions
//@Test
@SuppressWarnings("FieldNamingConvention")
public class SymlinkCompatibilityTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(SymlinkCompatibilityTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(ROOT_DIR);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(ROOT_DIR);
    }

    public void shouldUnzipPosixZipWithSymlink() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);
        Path zip = Paths.get("src/test/resources/symlink/posix/unique-symlink-target.zip").toAbsolutePath();

        UnzipIt.zip(zip).dstDir(dstDir).extract();

        assertThatDirectory(dstDir).exists().hasEntries(10).hasDirectories(2).hasRegularFiles(1).hasSymlinks(7);
        assertThatDirectory(dstDir).directory(symlinkRelDirNameCars).matches(dirSymlinkCarsAssert);
        assertThatDirectory(dstDir).directory(symlinkAbsDirNameData).matches(dir -> {
            dir.exists().hasEntries(8).hasDirectories(2).hasRegularFiles(5).hasSymlinks(1);
            dirBikesAssert.accept(dir.directory(dirNameBikes));
            dirEmptyAssert.accept(dir.directory(dirNameEmpty));
            dir.symlink(dirNameCars).exists()
               .hasTarget("../" + symlinkRelDirNameCars + '/' + symlinkRelDirNameCars + '/');
            fileMcDonnellDouglasAssert.accept(dir.regularFile(fileNameMcdonnelDouglas));
            fileSaintPetersburgAssert.accept(dir.regularFile(fileNameSaintPetersburg));
            fileEmptyAssert.accept(dir.regularFile(fileNameEmpty));
        });
        assertThatDirectory(dstDir).regularFile(fileNameDucati).matches(fileDucatiAssert);
        assertThatDirectory(dstDir).symlink(symlinkRelDirNameData).hasTarget(symlinkAbsDirNameData + '/');
        assertThatDirectory(dstDir).symlink(symlinkTrnDirNameData).hasTarget(symlinkAbsDirNameData + '/');
        assertThatDirectory(dstDir).symlink(symlinkAbsFileNameDucati).hasTarget(fileNameDucati);
        assertThatDirectory(dstDir).symlink(symlinkRelFileNameDucati).hasTarget(fileNameDucati);
        assertThatDirectory(dstDir).symlink(symlinkAbsFileNameHonda)
                                    .hasTarget(symlinkAbsDirNameData + '/' + dirNameBikes + '/' + fileNameHonda);
        assertThatDirectory(dstDir).symlink(symlinkRelFileNameHonda)
                                    .hasTarget(symlinkAbsDirNameData + '/' + dirNameBikes + '/' + fileNameHonda);
        assertThatDirectory(dstDir).symlink(symlinkTrnFileNameHonda)
                                    .hasTarget(symlinkAbsDirNameData + '/' + dirNameBikes + '/' + fileNameHonda);
    }

    public void shouldUnzipWinZipWithSymlink() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);
        Path zip = Paths.get("src/test/resources/symlink/win/unique-symlink-target.zip").toAbsolutePath();

        UnzipIt.zip(zip).dstDir(dstDir).extract();

        assertThatDirectory(dstDir).exists().hasEntries(10).hasDirectories(2).hasRegularFiles(1).hasSymlinks(7);
        assertThatDirectory(dstDir).directory(symlinkRelDirNameCars).matches(dirSymlinkCarsAssert);
        assertThatDirectory(dstDir).directory(symlinkAbsDirNameData).matches(dir -> {
            dir.exists().hasEntries(8).hasDirectories(2).hasRegularFiles(5).hasSymlinks(1);
            dirBikesAssert.accept(dir.directory(dirNameBikes));
            dirEmptyAssert.accept(dir.directory(dirNameEmpty));
            dir.symlink(dirNameCars).exists()
               .hasTarget("../" + symlinkRelDirNameCars + '/' + symlinkRelDirNameCars + '/');
            fileMcDonnellDouglasAssert.accept(dir.regularFile(fileNameMcdonnelDouglas));
            fileSaintPetersburgAssert.accept(dir.regularFile(fileNameSaintPetersburg));
            fileEmptyAssert.accept(dir.regularFile(fileNameEmpty));
        });
        assertThatDirectory(dstDir).regularFile(fileNameDucati).matches(fileDucatiAssert);
        assertThatDirectory(dstDir).symlink(symlinkRelDirNameData).hasTarget(symlinkAbsDirNameData + '/');
        assertThatDirectory(dstDir).symlink(symlinkTrnDirNameData).hasTarget(symlinkAbsDirNameData + '/');
        assertThatDirectory(dstDir).symlink(symlinkAbsFileNameDucati).hasTarget(fileNameDucati);
        assertThatDirectory(dstDir).symlink(symlinkRelFileNameDucati).hasTarget(fileNameDucati);
        assertThatDirectory(dstDir).symlink(symlinkAbsFileNameHonda)
                                    .hasTarget(symlinkAbsDirNameData + '/' + dirNameBikes + '/' + fileNameHonda);
        assertThatDirectory(dstDir).symlink(symlinkRelFileNameHonda)
                                    .hasTarget(symlinkAbsDirNameData + '/' + dirNameBikes + '/' + fileNameHonda);
        assertThatDirectory(dstDir).symlink(symlinkTrnFileNameHonda)
                                    .hasTarget(symlinkAbsDirNameData + '/' + dirNameBikes + '/' + fileNameHonda);
    }

    @SuppressWarnings("FieldNamingConvention")
    private static final Consumer<IDirectoryAssert<?>> dirSymlinkCarsAssert = dir -> {
        dir.exists().hasEntries(4).hasDirectories(1).hasRegularFiles(3);
        dirCarsAssert.accept(dir.directory(symlinkRelDirNameCars));
        fileBentleyAssert.accept(dir.regularFile(fileNameBentley));
        fileFerrariAssert.accept(dir.regularFile(fileNameFerrari));
        fileWiesmannAssert.accept(dir.regularFile(fileNameWiesmann));
    };
}
