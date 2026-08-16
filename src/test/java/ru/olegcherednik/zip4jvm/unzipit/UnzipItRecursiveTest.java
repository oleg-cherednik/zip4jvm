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
package ru.olegcherednik.zip4jvm.unzipit;

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import org.testng.annotations.Test;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 13.09.2025
 */
@Test
public class UnzipItRecursiveTest extends BaseTest {

    public void shouldUnzipRecursiveOffWhenDefaultSettings() {
        Path dstDir = getTestRoot();
        Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");
        UnzipIt.zip(zip).dstDir(dstDir).extract();

        assertThatDirectory(dstDir).exists().hasOnlyDirectoriesRegularFiles(1, 3);
        assertThatZipFile(dstDir.resolve("one_two.zip")).exists().hasSize(841);
        assertThatZipFile(dstDir.resolve("three_four.zip")).exists().hasSize(862);

        assertThatDirectory(dstDir.resolve("aa/bb")).exists().hasOnlyRegularFiles(2);
        assertThatZipFile(dstDir.resolve("aa/bb/group.zip")).exists();
    }

    //    public void shouldUnzipUpToFirstLevelWhenRecursiveLevelOne() {
    //        Path dstDir = getTestRoot();
    //        Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");
    //        UnzipIt.zip(zip)
    //               .settings(UnzipSettings.builder().recursiveLevel(1).build())
    //               .dstDir(dstDir).extract();
    //
    //        assertThatDirectory(dstDir).exists().hasOnlyDirectoriesRegularFiles(3, 1);
    //        assertThatDirectory(dstDir.resolve("one_two")).exists().hasOnlyRegularFiles(3);
    //        assertThatDirectory(dstDir.resolve("three_four")).exists().hasOnlyRegularFiles(3);
    //        assertThatDirectory(dstDir.resolve("aa/bb")).exists().hasOnlyRegularFiles(2);
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
    //        Path dstDir = getTestRoot();
    //        Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");
    //        UnzipIt.zip(zip)
    //               .settings(UnzipSettings.builder().recursiveLevel(2).build())
    //               .dstDir(dstDir).extract();
    //
    //    assertThatDirectory(dstDir).exists().hasOnlyDirectoriesRegularFiles(3, 1);
    //    assertThatDirectory(dstDir.resolve("one_two")).exists().hasOnlyDirectoriesRegularFiles(2, 1);
    //    assertThatDirectory(dstDir.resolve("one_two/one")).exists().hasOnlyRegularFiles(2);
    //    assertThatDirectory(dstDir.resolve("one_two/two")).exists().hasOnlyRegularFiles(2);
    //    assertThatDirectory(dstDir.resolve("three_four")).exists().hasOnlyDirectoriesRegularFiles(2, 1);
    //    assertThatDirectory(dstDir.resolve("three_four/three")).exists().hasOnlyRegularFiles(2);
    //    assertThatDirectory(dstDir.resolve("three_four/four")).exists().hasOnlyRegularFiles(2);
    //    assertThatDirectory(dstDir.resolve("aa/bb")).exists().hasOnlyRegularFiles(2);
    //
    //        assertThatZipFile(dstDir.resolve("aa/bb/group.zip")).exists();
    //    }

    // TODO should be fixed - failed on CI
    //    public void shouldUnzipUpToThirdLevelWhenRecursiveLevelThree() {
    //        Path dstDir = getTestRoot();
    //        Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");
    //        UnzipIt.zip(zip)
    //               .settings(UnzipSettings.builder().recursiveLevel(3).build())
    //               .dstDir(dstDir).extract();
    //
    //    assertThatDirectory(dstDir).exists().hasOnlyDirectoriesRegularFiles(3, 1);
    //    assertThatDirectory(dstDir.resolve("one_two")).exists().hasOnlyDirectoriesRegularFiles(2, 1);
    //    assertThatDirectory(dstDir.resolve("one_two/one")).exists().hasOnlyRegularFiles(2);
    //    assertThatDirectory(dstDir.resolve("one_two/two")).exists().hasOnlyRegularFiles(2);
    //    assertThatDirectory(dstDir.resolve("three_four")).exists().hasOnlyDirectoriesRegularFiles(2, 1);
    //    assertThatDirectory(dstDir.resolve("three_four/three")).exists().hasOnlyRegularFiles(2);
    //    assertThatDirectory(dstDir.resolve("three_four/four")).exists().hasOnlyRegularFiles(2);
    //    assertThatDirectory(dstDir.resolve("aa/bb")).exists().hasOnlyDirectoriesRegularFiles(1, 1);
    //    assertThatDirectory(dstDir.resolve("aa/bb/group")).exists().hasOnlyRegularFiles(3);
    //
    //    assertThatZipFile(dstDir.resolve("aa/bb/group/five_six.zip")).exists().hasSize(282);
    //    assertThatZipFile(dstDir.resolve("aa/bb/group/seven_eight.zip")).exists().hasSize(360);
    //    }

    // TODO should be fixed - failed on CI
    //    public void shouldUnzipUpToFourthLevelWhenRecursiveLevelFour() {
    //        Path dstDir = getTestRoot();
    //        Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");
    //        UnzipIt.zip(zip)
    //               .settings(UnzipSettings.builder().recursiveLevel(4).build())
    //               .dstDir(dstDir).extract();
    //
    //   assertThatDirectory(dstDir).exists().hasOnlyDirectoriesRegularFiles(3, 1);
    //   assertThatDirectory(dstDir.resolve("one_two")).exists().hasOnlyDirectoriesRegularFiles(2, 1);
    //   assertThatDirectory(dstDir.resolve("one_two/one")).exists().hasOnlyRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("one_two/two")).exists().hasOnlyRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("three_four")).exists().hasOnlyDirectoriesRegularFiles(2, 1);
    //   assertThatDirectory(dstDir.resolve("three_four/three")).exists().hasOnlyRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("three_four/four")).exists().hasOnlyRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("aa/bb")).exists().hasOnlyDirectoriesRegularFiles(1, 1);
    //   assertThatDirectory(dstDir.resolve("aa/bb/group")).exists().hasOnlyDirectoriesRegularFiles(2, 1);
    //   assertThatDirectory(dstDir.resolve("aa/bb/group/five_six")).exists().hasOnlyRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("aa/bb/group/seven_eight")).exists().hasOnlyRegularFiles(2);
    //    }

    // TODO should be fixed - failed on CI
    //    public void shouldUnzipAllWhenMaxLevel() {
    //        Path dstDir = getTestRoot();
    //        Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");
    //        UnzipIt.zip(zip)
    //               .settings(UnzipSettings.builder().recursiveLevelMax().build())
    //               .dstDir(dstDir).extract();
    //
    //   assertThatDirectory(dstDir).exists().hasOnlyDirectoriesRegularFiles(3, 1);
    //   assertThatDirectory(dstDir.resolve("one_two")).exists().hasOnlyDirectoriesRegularFiles(2, 1);
    //   assertThatDirectory(dstDir.resolve("one_two/one")).exists().hasOnlyRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("one_two/two")).exists().hasOnlyRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("three_four")).exists().hasOnlyDirectoriesRegularFiles(2, 1);
    //   assertThatDirectory(dstDir.resolve("three_four/three")).exists().hasOnlyRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("three_four/four")).exists().hasOnlyRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("aa/bb")).exists().hasOnlyDirectoriesRegularFiles(1, 1);
    //   assertThatDirectory(dstDir.resolve("aa/bb/group")).exists().hasOnlyDirectoriesRegularFiles(2, 1);
    //   assertThatDirectory(dstDir.resolve("aa/bb/group/five_six")).exists().hasOnlyRegularFiles(2);
    //   assertThatDirectory(dstDir.resolve("aa/bb/group/seven_eight")).exists().hasOnlyRegularFiles(2);
    //    }

}
