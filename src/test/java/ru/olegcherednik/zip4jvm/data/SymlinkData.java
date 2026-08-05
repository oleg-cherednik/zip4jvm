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
package ru.olegcherednik.zip4jvm.data;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.dirSrc;
import static ru.olegcherednik.zip4jvm.TestData.dirSrcData;
import static ru.olegcherednik.zip4jvm.TestData.dirSrcSymlink;
import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.getSymlinkTrnDirData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkAbsDirData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkAbsFileDucati;
import static ru.olegcherednik.zip4jvm.TestData.symlinkAbsFileHonda;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelDirCars;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelDirData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelDirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelFileDucati;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelFileHonda;
import static ru.olegcherednik.zip4jvm.TestData.symlinkTrnFileHonda;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.copyToDir;

/**
 * @author Oleg Cherednik
 * @since 22.01.2023
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({ "checkstyle:VariableDeclarationUsageDistance", "PMD.VariableDeclarationUsageDistance" })
public final class SymlinkData {

    public static void createSymlinkData() {
        Zip4jvmSuite.createDir(dirSrcSymlink);

        Path fileLocalDucati = dirSrcSymlink.resolve(fileNameDucati);
        Zip4jvmSuite.copyFile(fileDucati, fileLocalDucati);

        createRelativeSymlink(symlinkRelFileDucati, fileLocalDucati);
        createRelativeSymlink(symlinkRelFileHonda, fileHonda);
        createRelativeSymlink(symlinkRelDirData, dirSrcData);

        createAbsoluteSymlink(symlinkAbsFileDucati, fileLocalDucati);
        createAbsoluteSymlink(symlinkAbsFileHonda, fileHonda);
        createAbsoluteSymlink(symlinkAbsDirData, dirSrcData);

        createRelativeSymlink(symlinkTrnFileHonda, symlinkRelFileHonda);
        createRelativeSymlink(getSymlinkTrnDirData, symlinkRelDirData);

        createCyclicSymlink();
        createNoTargetSymlink();

        createRelativeDir();

        createBikeDir();
    }

    private static void createCyclicSymlink() {
        // two -> one -> three -> four -> one
        Path oneSymlink = dirSrcSymlink.resolve("one-symlink");
        Path twoSymlink = dirSrcSymlink.resolve("two-symlink");
        Path threeSymlink = dirSrcSymlink.getParent().resolve("three-symlink");
        Path fourSymlink = dirSrcSymlink.getParent().resolve("four-symlink");

        createRelativeSymlink(oneSymlink, threeSymlink);
        createRelativeSymlink(threeSymlink, fourSymlink);
        createAbsoluteSymlink(fourSymlink, oneSymlink);

        createRelativeSymlink(twoSymlink, oneSymlink);
    }

    private static void createRelativeDir() {
        Path dirLocalCars = dirSrc.resolve(dirNameCars);
        Path dirCarsSymlink = dirLocalCars.resolve(symlinkRelDirNameCars);

        copyToDir(dirCars, dirLocalCars);

        createRelativeSymlink(dirCarsSymlink, dirCars);
        createRelativeSymlink(symlinkRelDirCars, dirLocalCars);
    }

    private static void createNoTargetSymlink() {
        // five -> six ->
        Path fiveSymlink = dirSrcSymlink.resolve("five-symlink");
        Path sixSymlink = dirSrcSymlink.getParent().resolve("six-symlink");
        Path fantomSymlink = dirSrcSymlink.getParent().resolve("fantom-symlink");

        createRelativeSymlink(fiveSymlink, sixSymlink);
        createRelativeSymlink(sixSymlink, fantomSymlink);
    }

    private static void createBikeDir() {
        Path dirBikes1 = dirSrc.resolve("bikes");

        Zip4jvmSuite.createDir(dirBikes1);
        Zip4jvmSuite.createDir(dirBikes1.resolve("xxx"));

        Path dirSubBikes1 = dirBikes1.resolve("sub-bikes1");
        Path dirSubBikes2 = dirBikes1.resolve("sub-bikes2");

        createRelativeSymlink(dirSubBikes1, dirBikes);
        createRelativeSymlink(dirSubBikes2, dirBikes);
    }

    private static void createRelativeSymlink(Path symlink, Path target) {
        Quietly.doRuntime(() -> Files.createSymbolicLink(symlink, symlink.getParent().relativize(target)));
    }

    private static void createAbsoluteSymlink(Path symlink, Path target) {
        Quietly.doRuntime(() -> Files.createSymbolicLink(symlink, target));
    }

}
