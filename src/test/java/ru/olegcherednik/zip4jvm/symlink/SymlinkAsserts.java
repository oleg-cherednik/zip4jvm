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

import ru.olegcherednik.zip4jvm.assertj.IDirectoryAssert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
 * @since 06.08.2026
 */
@SuppressWarnings("FieldNamingConvention")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class SymlinkAsserts {

    static final Consumer<IDirectoryAssert<?>> dirSymlinkCarsAssert = dir -> {
        dir.hasOnlyDirectoriesRegularFiles(1, 3)
           .withDirectory(symlinkRelDirNameCars, dirCarsAssert)
           .withRegularFile(fileNameBentley, fileBentleyAssert)
           .withRegularFile(fileNameFerrari, fileFerrariAssert)
           .withRegularFile(fileNameWiesmann, fileWiesmannAssert);
    };

    static final Consumer<IDirectoryAssert<?>> dirSymlinkDataAssert = dir -> {
        dir.hasOnlyDirectoriesRegularFiles(2, 5)
           .withDirectory(dirNameBikes, dirBikesAssert)
           .withDirectory(dirNameEmpty, dirEmptyAssert)
           .withRegularFile(fileNameEmpty, fileEmptyAssert)
           .withRegularFile(fileNameMcdonnelDouglas, fileMcDonnellDouglasAssert)
           .withRegularFile(fileNameSaintPetersburg, fileSaintPetersburgAssert)
           .withRegularFile(fileNameSigSauer, fileSigSauerAssert);
    };

    static void checkDstDir(Path dstDir) {
        assertThatDirectory(dstDir)
                .hasOnlyDirectoriesRegularFilesSymlinks(2, 1, 7)
                .withDirectory("cars-rel-symlink", dirSymlinkCarsAssert)
                .withDirectory("data-abs-symlink", dirSymlinkDataAssert)
                .withDirectorySymlink("data-rel-symlink", dirSymlinkDataAssert)
                .withDirectorySymlink("data-trn-symlink", dirSymlinkDataAssert)
                .withRegularFile(fileNameDucati, fileDucatiAssert)
                .withRegularFileSymlink("ducati-panigale-1199-abs-symlink.jpg", fileDucatiAssert)
                .withRegularFileSymlink("ducati-panigale-1199-rel-symlink.jpg", fileDucatiAssert)
                .withRegularFileSymlink("honda-cbr600rr-abs-symlink.jpg", fileHondaAssert)
                .withRegularFileSymlink("honda-cbr600rr-rel-symlink.jpg", fileHondaAssert)
                .withRegularFileSymlink("honda-cbr600rr-trn-symlink.jpg", fileHondaAssert);
    }

}
