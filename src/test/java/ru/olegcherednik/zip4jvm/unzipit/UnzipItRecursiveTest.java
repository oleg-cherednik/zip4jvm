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
package ru.olegcherednik.zip4jvm.unzipit;

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 13.09.2025
 */
@Test
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class UnzipItRecursiveTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @BeforeClass
    public static void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldUnzipRecursiveOffWhenDefaultSettings() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");
        UnzipIt.zip(zip).dstDir(dstDir).extract();

        assertThatDirectory(dstDir).exists().hasEntries(4).hasRegularFiles(3).hasDirectories(1);
        assertThatZipFile(dstDir.resolve("one_two.zip")).exists().hasSize(841);
        assertThatZipFile(dstDir.resolve("three_four.zip")).exists().hasSize(862);

        assertThatDirectory(dstDir.resolve("aa/bb")).exists().hasEntries(2).hasRegularFiles(2);
        assertThatZipFile(dstDir.resolve("aa/bb/group.zip")).exists();
    }

    //    public void shouldUnzipUpToFirstLevelWhenRecursiveLevelOne() {
    //        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
    //        Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");
    //        UnzipIt.zip(zip)
    //               .settings(UnzipSettings.builder().recursiveLevel(1).build())
    //               .dstDir(dstDir).extract();
    //
    //        assertThatDirectory(dstDir).exists().hasEntries(4).hasRegularFiles(1).hasDirectories(3);
    //        assertThatDirectory(dstDir.resolve("one_two")).exists().hasEntries(3).hasRegularFiles(3);
    //        assertThatDirectory(dstDir.resolve("three_four")).exists().hasEntries(3).hasRegularFiles(3);
    //        assertThatDirectory(dstDir.resolve("aa/bb")).exists().hasEntries(2).hasRegularFiles(2);
    //
    //        assertThatZipFile(dstDir.resolve("one_two/one.zip")).exists().hasSize(362);
    //        assertThatZipFile(dstDir.resolve("one_two/two.zip")).exists().hasSize(362);
    //        assertThatZipFile(dstDir.resolve("three_four/three.zip")).exists().hasSize(374);
    //        assertThatZipFile(dstDir.resolve("three_four/four.zip")).exists().hasSize(368);
    //        assertThatZipFile(dstDir.resolve("aa/bb/group.zip")).exists();
    //    }

    //@SuppressWarnings("checkstyle:linelength")
    // TODO should be fixed - failed on CI
    //    public void shouldUnzipUpToSecondLevelWhenRecursiveLevelTwo() {
    //        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
    //        Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");
    //        UnzipIt.zip(zip)
    //               .settings(UnzipSettings.builder().recursiveLevel(2).build())
    //               .dstDir(dstDir).extract();
    //
    //    assertThatDirectory(dstDir).exists().hasEntries(4).hasRegularFiles(1).hasDirectories(3);
    //    assertThatDirectory(dstDir.resolve("one_two")).exists().hasEntries(3).hasRegularFiles(1).hasDirectories(2);
    //    assertThatDirectory(dstDir.resolve("one_two/one")).exists().hasEntries(2).hasRegularFiles(2);
    //    assertThatDirectory(dstDir.resolve("one_two/two")).exists().hasEntries(2).hasRegularFiles(2);
    //    assertThatDirectory(dstDir.resolve("three_four")).exists().hasEntries(3).hasRegularFiles(1).hasDirectories(2);
    //    assertThatDirectory(dstDir.resolve("three_four/three")).exists().hasEntries(2).hasRegularFiles(2);
    //    assertThatDirectory(dstDir.resolve("three_four/four")).exists().hasEntries(2).hasRegularFiles(2);
    //    assertThatDirectory(dstDir.resolve("aa/bb")).exists().hasEntries(2).hasRegularFiles(2);
    //
    //        assertThatZipFile(dstDir.resolve("aa/bb/group.zip")).exists();
    //    }

    // TODO should be fixed - failed on CI
    //    public void shouldUnzipUpToThirdLevelWhenRecursiveLevelThree() {
    //        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
    //        Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");
    //        UnzipIt.zip(zip)
    //               .settings(UnzipSettings.builder().recursiveLevel(3).build())
    //               .dstDir(dstDir).extract();
    //
    //    assertThatDirectory(dstDir).exists().hasEntries(4).hasRegularFiles(1).hasDirectories(3);
    //    assertThatDirectory(dstDir.resolve("one_two")).exists().hasEntries(3).hasRegularFiles(1).hasDirectories(2);
    //    assertThatDirectory(dstDir.resolve("one_two/one")).exists().hasEntries(2).hasRegularFiles(2);
    //    assertThatDirectory(dstDir.resolve("one_two/two")).exists().hasEntries(2).hasRegularFiles(2);
    //    assertThatDirectory(dstDir.resolve("three_four")).exists().hasEntries(3).hasRegularFiles(1).hasDirectories(2);
    //    assertThatDirectory(dstDir.resolve("three_four/three")).exists().hasEntries(2).hasRegularFiles(2);
    //    assertThatDirectory(dstDir.resolve("three_four/four")).exists().hasEntries(2).hasRegularFiles(2);
    //    assertThatDirectory(dstDir.resolve("aa/bb")).exists().hasEntries(2).hasRegularFiles(1).hasDirectories(1);
    //    assertThatDirectory(dstDir.resolve("aa/bb/group")).exists().hasEntries(3).hasRegularFiles(3);
    //
    //    assertThatZipFile(dstDir.resolve("aa/bb/group/five_six.zip")).exists().hasSize(282);
    //    assertThatZipFile(dstDir.resolve("aa/bb/group/seven_eight.zip")).exists().hasSize(360);
    //    }

    // TODO should be fixed - failed on CI
    //    public void shouldUnzipUpToFourthLevelWhenRecursiveLevelFour() {
    //        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
    //        Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");
    //        UnzipIt.zip(zip)
    //               .settings(UnzipSettings.builder().recursiveLevel(4).build())
    //               .dstDir(dstDir).extract();
    //
    //   assertThatDirectory(dstDir).exists().hasEntries(4).hasRegularFiles(1).hasDirectories(3);
    //   assertThatDirectory(dstDir.resolve("one_two")).exists().hasEntries(3).hasRegularFiles(1).hasDirectories(2);
    //   assertThatDirectory(dstDir.resolve("one_two/one")).exists().hasEntries(2).hasRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("one_two/two")).exists().hasEntries(2).hasRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("three_four")).exists().hasEntries(3).hasRegularFiles(1).hasDirectories(2);
    //   assertThatDirectory(dstDir.resolve("three_four/three")).exists().hasEntries(2).hasRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("three_four/four")).exists().hasEntries(2).hasRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("aa/bb")).exists().hasEntries(2).hasRegularFiles(1).hasDirectories(1);
    //   assertThatDirectory(dstDir.resolve("aa/bb/group")).exists().hasEntries(3).hasRegularFiles(1).hasDirectories(2);
    //   assertThatDirectory(dstDir.resolve("aa/bb/group/five_six")).exists().hasEntries(2).hasRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("aa/bb/group/seven_eight")).exists().hasEntries(2).hasRegularFiles(2);
    //    }

    // TODO should be fixed - failed on CI
    //    public void shouldUnzipAllWhenMaxLevel() {
    //        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
    //        Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");
    //        UnzipIt.zip(zip)
    //               .settings(UnzipSettings.builder().recursiveLevelMax().build())
    //               .dstDir(dstDir).extract();
    //
    //   assertThatDirectory(dstDir).exists().hasEntries(4).hasRegularFiles(1).hasDirectories(3);
    //   assertThatDirectory(dstDir.resolve("one_two")).exists().hasEntries(3).hasRegularFiles(1).hasDirectories(2);
    //   assertThatDirectory(dstDir.resolve("one_two/one")).exists().hasEntries(2).hasRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("one_two/two")).exists().hasEntries(2).hasRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("three_four")).exists().hasEntries(3).hasRegularFiles(1).hasDirectories(2);
    //   assertThatDirectory(dstDir.resolve("three_four/three")).exists().hasEntries(2).hasRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("three_four/four")).exists().hasEntries(2).hasRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("aa/bb")).exists().hasEntries(2).hasRegularFiles(1).hasDirectories(1);
    //   assertThatDirectory(dstDir.resolve("aa/bb/group")).exists().hasEntries(3).hasRegularFiles(1).hasDirectories(2);
    //   assertThatDirectory(dstDir.resolve("aa/bb/group/five_six")).exists().hasEntries(2).hasRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("aa/bb/group/seven_eight")).exists().hasEntries(2).hasRegularFiles(2);
    //    }

}
