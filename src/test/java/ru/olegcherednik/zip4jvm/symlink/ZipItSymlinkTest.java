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

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.assertj.ZipEntryDirectoryAssert;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSymlinkEnum;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirSrcSymlink;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;
import static ru.olegcherednik.zip4jvm.TestData.symlinkAbsDirNameData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkAbsFileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.symlinkAbsFileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelDirData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelDirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelDirNameData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelFileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelFileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.symlinkTrnDirNameData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkTrnFileNameHonda;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileDucatiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileHondaAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;
import static ru.olegcherednik.zip4jvm.symlink.SymlinkAsserts.dirSymlinkCarsAssert;

/**
 * @author Oleg Cherednik
 * @since 22.01.2023
 */
@Test
public class ZipItSymlinkTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @BeforeClass
    public void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldIgnoreSymlinkWhenCreateZipDefaultSettings() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        ZipIt.zip(zip).add(dirSrcSymlink);

        assertThatZipFile(zip).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).root().hasOnlyDirectories(1);

        ZipEntryDirectoryAssert dirSymlink = assertThatZipFile(zip).directory("symlink");
        dirSymlink.hasOnlyRegularFiles(1);
        dirSymlink.regularFile(fileNameDucati).matches(fileDucatiAssert);
    }

    public void shouldIgnoreSymlinkWhenIgnoreSymlink() {
        ZipSettings settings = ZipSettings.builder().zipSymlink(ZipSymlinkEnum.IGNORE_SYMLINK).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        ZipIt.zip(zip).settings(settings).add(dirSrcSymlink);

        assertThatZipFile(zip).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).root().hasOnlyDirectories(1);

        ZipEntryDirectoryAssert dirSymlink = assertThatZipFile(zip).directory("symlink");
        dirSymlink.hasOnlyRegularFiles(1);
        dirSymlink.regularFile(fileNameDucati).matches(fileDucatiAssert);
    }

    public void shouldAddRootSymlinkContentWhenZipDefaultSettings() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        ZipIt.zip(zip).add(symlinkRelDirData);

        assertThatZipFile(zip).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).root().hasOnlyDirectories(1);
        assertThatZipFile(zip).directory("data-rel-symlink").matches(rootAssert);
    }

    public void shouldCreateZipNoSymlinkWhenReplaceSymlinkWithTarget() {
        ZipSettings settings = ZipSettings.builder()
                                          .zipSymlink(ZipSymlinkEnum.REPLACE_SYMLINK_WITH_TARGET)
                                          .build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        ZipIt.zip(zip).settings(settings).add(dirSrcSymlink);


        assertThatZipFile(zip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyDirectories(1)
                .withDirectory("symlink", dir -> dir.hasOnlyDirectoriesRegularFiles(4, 6)
                                                    .withDirectory(symlinkRelDirNameCars, dirSymlinkCarsAssert)
                                                    .withDirectory(symlinkAbsDirNameData, rootAssert)
                                                    .withDirectory(symlinkRelDirNameData, rootAssert)
                                                    .withDirectory(symlinkTrnDirNameData, rootAssert)
                                                    .withRegularFile(fileNameDucati, fileDucatiAssert)
                                                    .withRegularFile(symlinkAbsFileNameDucati, fileDucatiAssert)
                                                    .withRegularFile(symlinkRelFileNameDucati, fileDucatiAssert)
                                                    .withRegularFile(symlinkAbsFileNameHonda, fileHondaAssert)
                                                    .withRegularFile(symlinkRelFileNameHonda, fileHondaAssert)
                                                    .withRegularFile(symlinkTrnFileNameHonda, fileHondaAssert));

    }

    //    /**
    //     * E.g. we have 100Mb file and 10 symlinks to this file. We want to add all 10 links at the same time. As
    //     * results, we should have only one 100Mb regular file and 9 relative symlinks to this file.
    //     */
    //    public void shouldAddAtOnceSameTargetSymlinkAsRelativeSymlinkWhenReplaceWithUniqueTarget() {
    //        Path dirRoot = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
    //        Path dirSymlinks = dirRoot.resolve("symlinks");
    //
    //        String absSymlinkName1 = "abs_1_" + fileNameDucati;
    //        String absSymlinkName2 = "abs_2_" + fileNameDucati;
    //        String relSymlinkName3 = "rel_3_" + fileNameDucati;
    //        String relSymlinkName4 = "rel_4_" + fileNameDucati;
    //
    //        Path absSymlink1 = dirSymlinks.resolve(absSymlinkName1);
    //        Path absSymlink2 = dirSymlinks.resolve(absSymlinkName2);
    //        Path relSymlink3 = dirSymlinks.resolve(relSymlinkName3);
    //        Path relSymlink4 = dirSymlinks.resolve(relSymlinkName4);
    //
    //        SymlinkData.createAbsoluteSymlink(absSymlink1, fileDucati);
    //        SymlinkData.createAbsoluteSymlink(absSymlink2, fileDucati);
    //        SymlinkData.createRelativeSymlink(relSymlink3, fileDucati);
    //        SymlinkData.createRelativeSymlink(relSymlink4, fileDucati);
    //
    //        Path zip = dirRoot.resolve("dst").resolve(fileNameZipSrc);
    //
    //        ZipSettings settings = ZipSettings.builder()
    //                                          .zipSymlink(ZipSymlinkEnum.REPLACE_SYMLINK_WITH_UNIQUE_TARGET)
    //                                          .build();
    //
    //        ZipIt.zip(zip).settings(settings).add(Arrays.asList(absSymlink1, absSymlink2, relSymlink3, relSymlink4));
    //        assertThatZipFile(zip).parent().hasOnlyRegularFiles(1);
    //        assertThatZipFile(zip).root().hasEntries(4).hasRegularFiles(1).hasSymlinks(3);
    //        assertThatZipFile(zip).regularFileMatches(absSymlinkName1, fileDucatiAssert);
    //        assertThatZipFile(zip).symlink(absSymlinkName2).regularFile().matches(fileDucatiAssert);
    //        assertThatZipFile(zip).symlink(relSymlinkName3).regularFile().matches(fileDucatiAssert);
    //        assertThatZipFile(zip).symlink(relSymlinkName4).regularFile().matches(fileDucatiAssert);
    //    }


    //    public void shouldCreateZipNoSymlinkWhenReplaceSymlinkWithUniqueTarget() {
    //        ZipSettings settings = ZipSettings.builder()
    //                                          .removeRootDir(true)
    //                                          .zipSymlink(ZipSymlinkEnum.REPLACE_SYMLINK_WITH_UNIQUE_TARGET)
    //                                          .build();
    //
    //        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
    //        Path zip = dstDir.resolve("src.zip");
    //        ZipIt.zip(zip).settings(settings).add(dirSrcSymlink);
    //
    //        //        zip = Paths.get("d:/Programming/GitHub/zip4jvm/src/test/resources/
    //        //        symlink/posix/unique-symlink-target.zip");
    //        //        dstDir = Paths.get("d:/zip4jvm/bbb/bbb");
    //        //        dstDir = Paths.get("d:/zip4jvm/bbb/aaa");
    //        //        Files.deleteIfExists(dstDir);
    //        //        ZipInfo.zip(zip).settings(ZipInfoSettings.builder().copyPayload(true)
    //        .build()).decompose(dstDir);
    //
    //        assertThatZipFile(zip).parent().hasOnlyRegularFiles(1);
    //        assertThatZipFile(zip).root().hasEntries(10).hasDirectories(2).hasRegularFiles(1).hasSymlinks(7);
    //        assertThatZipFile(zip).directory(symlinkRelDirNameCars).matches(dirSymlinkCarsAssert);
    //        assertThatZipFile(zip).directory(symlinkAbsDirNameData).matches(dir -> {
    //            dir.exists().hasEntries(8).hasDirectories(2).hasRegularFiles(5).hasSymlinks(1);
    //            dirBikesAssert.accept(dir.directory(dirNameBikes));
    //            dirEmptyAssert.accept(dir.directory(dirNameEmpty));
    //            dir.symlink(dirNameCars).exists()
    //               .hasTarget("../" + symlinkRelDirNameCars + '/' + symlinkRelDirNameCars + '/');
    //            fileMcDonnellDouglasAssert.accept(dir.regularFile(fileNameMcdonnelDouglas));
    //            fileSaintPetersburgAssert.accept(dir.regularFile(fileNameSaintPetersburg));
    //            fileEmptyAssert.accept(dir.regularFile(fileNameEmpty));
    //        });
    //        assertThatZipFile(zip).regularFileMatches(fileNameDucati, fileDucatiAssert);
    //        assertThatZipFile(zip).symlink(symlinkRelDirNameData).hasTarget(symlinkAbsDirNameData + '/');
    //        assertThatZipFile(zip).symlink(symlinkTrnDirNameData).hasTarget(symlinkAbsDirNameData + '/');
    //        assertThatZipFile(zip).symlink(symlinkAbsFileNameDucati).hasTarget(fileNameDucati);
    //        assertThatZipFile(zip).symlink(symlinkRelFileNameDucati).hasTarget(fileNameDucati);
    //        assertThatZipFile(zip).symlink(symlinkAbsFileNameHonda)
    //                              .hasTarget(symlinkAbsDirNameData + '/' + dirNameBikes + '/' + fileNameHonda);
    //        assertThatZipFile(zip).symlink(symlinkRelFileNameHonda)
    //                              .hasTarget(symlinkAbsDirNameData + '/' + dirNameBikes + '/' + fileNameHonda);
    //        assertThatZipFile(zip).symlink(symlinkTrnFileNameHonda)
    //                              .hasTarget(symlinkAbsDirNameData + '/' + dirNameBikes + '/' + fileNameHonda);
    //    }

}
