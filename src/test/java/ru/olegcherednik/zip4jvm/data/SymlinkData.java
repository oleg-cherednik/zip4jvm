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
package ru.olegcherednik.zip4jvm.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
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
public final class SymlinkData {

    public static void main(String... args) throws IOException {
        createSymlinkData();
    }

    public static void createSymlinkData() throws IOException {
        Files.createDirectories(dirSrcSymlink);

        Path fileLocalDucati = dirSrcSymlink.resolve(fileNameDucati);
        Files.copy(fileDucati, fileLocalDucati);

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

    private static void createCyclicSymlink() throws IOException {
        // two -> one -> three -> four -> one
        Path oneSymlink = dirSrcSymlink.resolve("one-symlink");
        Path twoSymlink = dirSrcSymlink.resolve("two-symlink");
        Path threeSymlink = dirSrc.resolve("three-symlink");
        Path fourSymlink = dirSrc.resolve("four-symlink");

        createRelativeSymlink(oneSymlink, threeSymlink);
        createRelativeSymlink(threeSymlink, fourSymlink);
        createAbsoluteSymlink(fourSymlink, oneSymlink);

        createRelativeSymlink(twoSymlink, oneSymlink);
    }

    private static void createRelativeDir() throws IOException {
        Path dirLocalCars = dirSrc.resolve(dirNameCars);
        Path dirCarsSymlink = dirLocalCars.resolve(symlinkRelDirNameCars);

        copyToDir(dirCars, dirLocalCars);

        createRelativeSymlink(dirCarsSymlink, dirCars);
        createRelativeSymlink(symlinkRelDirCars, dirLocalCars);
    }

    private static void createRelativeSymlink(Path symlink, Path target) throws IOException {
        Files.createSymbolicLink(symlink, symlink.getParent().relativize(target));
    }

    private static void createAbsoluteSymlink(Path symlink, Path target) throws IOException {
        Files.createSymbolicLink(symlink, target);
    }

    private static void createNoTargetSymlink() throws IOException {
        // five -> six ->
        Path fiveSymlink = dirSrcSymlink.resolve("five-symlink");
        Path sixSymlink = dirSrc.resolve("six-symlink");
        Path fantomSymlink = dirSrc.resolve("fantom-symlink");

        createRelativeSymlink(fiveSymlink, sixSymlink);
        createRelativeSymlink(sixSymlink, fantomSymlink);
    }

    private static void createBikeDir() throws IOException {
        Path dirBikes1 = dirSrc.resolve("bikes");
        Files.createDirectories(dirBikes1);
        Files.createDirectories(dirBikes1.resolve("xxx"));

        Path dirSubBikes1 = dirBikes1.resolve("sub-bikes1");
        Path dirSubBikes2 = dirBikes1.resolve("sub-bikes2");

        createRelativeSymlink(dirSubBikes1, dirBikes);
        createRelativeSymlink(dirSubBikes2, dirBikes);
    }

}
