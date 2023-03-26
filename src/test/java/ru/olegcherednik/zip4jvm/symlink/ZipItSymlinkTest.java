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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipInfo;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.assertj.IDirectoryAssert;
import ru.olegcherednik.zip4jvm.model.ZipSymlink;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.dirSrcSymlink;
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
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelDirData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelDirNameData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelFileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelFileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.symlinkTrnDirNameData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkTrnFileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameEmpty;
import static ru.olegcherednik.zip4jvm.TestData.zipSymlinkAbsDirNameData;
import static ru.olegcherednik.zip4jvm.TestData.zipSymlinkRelDirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.zipSymlinkRelDirNameData;
import static ru.olegcherednik.zip4jvm.TestData.zipSymlinkTrnDirNameData;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirEmptyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileDucatiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileEmptyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileFerrariAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileHondaAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileMcdonnelDouglasAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSaintPetersburgAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileWiesmannAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 22.01.2023
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipItSymlinkTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipItSymlinkTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldIgnoreSymlinkWhenCreateZipDefaultSettings() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Path zip = destDir.resolve("src.zip");
        ZipIt.zip(zip).settings(ZipSettings.builder().removeRootDir(true).build()).add(dirSrcSymlink);

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip).root().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip).regularFile(fileNameDucati).matches(fileDucatiAssert);
    }

    public void shouldIgnoreSymlinkWhenIgnoreSymlink() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .removeRootDir(true)
                                          .zipSymlink(ZipSymlink.IGNORE_SYMLINK)
                                          .build();

        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Path zip = destDir.resolve("src.zip");
        ZipIt.zip(zip).settings(settings).add(dirSrcSymlink);

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip).root().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip).regularFile(fileNameDucati).matches(fileDucatiAssert);
    }

    public void shouldAddRootSymlinkContentWhenZipDefaultSettings() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Path zip = destDir.resolve("src.zip");
        ZipIt.zip(zip).settings(ZipSettings.builder().removeRootDir(true).build()).add(symlinkRelDirData);
        assertThatZipFile(zip).root().matches(rootAssert);
    }

    public void shouldCreateZipNoSymlinkWhenReplaceSymlinkWithTarget() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .removeRootDir(true)
                                          .zipSymlink(ZipSymlink.REPLACE_SYMLINK_WITH_TARGET)
                                          .build();

        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Path zip = destDir.resolve("src.zip");
        ZipIt.zip(zip).settings(settings).add(dirSrcSymlink);

        assertThatDirectory(zip.getParent()).exists().hasEntries(1).hasRegularFiles(1);
        assertThatZipFile(zip).root().hasEntries(10).hasDirectories(4).hasRegularFiles(6);
        assertThatZipFile(zip).directory(zipSymlinkRelDirNameData).matches(rootAssert);
        assertThatZipFile(zip).directory(zipSymlinkAbsDirNameData).matches(rootAssert);
        assertThatZipFile(zip).directory(zipSymlinkTrnDirNameData).matches(rootAssert);
        assertThatZipFile(zip).directory(zipSymlinkRelDirNameCars).matches(dirSymlinkCarsAssert);
        assertThatZipFile(zip).regularFile(fileNameDucati).matches(fileDucatiAssert);
        assertThatZipFile(zip).regularFile(symlinkRelFileNameDucati).matches(fileDucatiAssert);
        assertThatZipFile(zip).regularFile(symlinkRelFileNameHonda).matches(fileHondaAssert);
        assertThatZipFile(zip).regularFile(symlinkAbsFileNameDucati).matches(fileDucatiAssert);
        assertThatZipFile(zip).regularFile(symlinkAbsFileNameHonda).matches(fileHondaAssert);
        assertThatZipFile(zip).regularFile(symlinkTrnFileNameHonda).matches(fileHondaAssert);
    }

    public void shouldCreateZipNoSymlinkWhenReplaceSymlinkWithUniqueTarget() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .removeRootDir(true)
                                          .zipSymlink(ZipSymlink.REPLACE_SYMLINK_WITH_UNIQUE_TARGET)
                                          .build();

        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Path zip = destDir.resolve("src.zip");
        ZipIt.zip(zip).settings(settings).add(dirSrcSymlink);

//        zip = Paths.get("d:/Programming/GitHub/zip4jvm/src/test/resources/symlink/posix/unique-symlink-target.zip");
//        destDir = Paths.get("d:/zip4jvm/bbb/bbb");
//        destDir = Paths.get("d:/zip4jvm/bbb/aaa");
//        Files.deleteIfExists(destDir);
//        ZipInfo.zip(zip).settings(ZipInfoSettings.builder().copyPayload(true).build()).decompose(destDir);

        assertThatDirectory(zip.getParent()).exists().hasEntries(1).hasRegularFiles(1);
        assertThatZipFile(zip).root().hasEntries(10).hasDirectories(2).hasRegularFiles(1).hasSymlinks(7);
        assertThatZipFile(zip).directory(zipSymlinkRelDirNameCars).matches(dirSymlinkCarsAssert);
        assertThatZipFile(zip).directory(zipSymlinkAbsDirNameData).matches(dir -> {
            dir.exists().hasEntries(8).hasDirectories(2).hasRegularFiles(5).hasSymlinks(1);
            dirBikesAssert.accept(dir.directory(zipDirNameBikes));
            dirEmptyAssert.accept(dir.directory(zipDirNameEmpty));
            dir.symlink(dirNameCars).exists().hasTarget("../" + zipSymlinkRelDirNameCars + zipSymlinkRelDirNameCars);
            fileMcdonnelDouglasAssert.accept(dir.regularFile(fileNameMcdonnelDouglas));
            fileSaintPetersburgAssert.accept(dir.regularFile(fileNameSaintPetersburg));
            fileEmptyAssert.accept(dir.regularFile(fileNameEmpty));
        });
        assertThatZipFile(zip).regularFile(fileNameDucati).matches(fileDucatiAssert);
        assertThatZipFile(zip).symlink(symlinkRelDirNameData).hasTarget(symlinkAbsDirNameData + '/');
        assertThatZipFile(zip).symlink(symlinkTrnDirNameData).hasTarget(symlinkAbsDirNameData + '/');
        assertThatZipFile(zip).symlink(symlinkAbsFileNameDucati).hasTarget(fileNameDucati);
        assertThatZipFile(zip).symlink(symlinkRelFileNameDucati).hasTarget(fileNameDucati);
        assertThatZipFile(zip).symlink(symlinkAbsFileNameHonda).hasTarget(zipSymlinkAbsDirNameData + zipDirNameBikes + fileNameHonda);
        assertThatZipFile(zip).symlink(symlinkRelFileNameHonda).hasTarget(zipSymlinkAbsDirNameData + zipDirNameBikes + fileNameHonda);
        assertThatZipFile(zip).symlink(symlinkTrnFileNameHonda).hasTarget(zipSymlinkAbsDirNameData + zipDirNameBikes + fileNameHonda);
    }

    private static final Consumer<IDirectoryAssert<?>> dirSymlinkCarsAssert = dir -> {
        dir.exists().hasEntries(4).hasDirectories(1).hasRegularFiles(3);
        dirCarsAssert.accept(dir.directory(zipSymlinkRelDirNameCars));
        fileBentleyAssert.accept(dir.regularFile(fileNameBentley));
        fileFerrariAssert.accept(dir.regularFile(fileNameFerrari));
        fileWiesmannAssert.accept(dir.regularFile(fileNameWiesmann));
    };

}
