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

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;

import org.testng.annotations.Test;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.symlinkCve20074550Zip;
import static ru.olegcherednik.zip4jvm.TestData.symlinkPosixZip;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileDucatiAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.symlink.SymlinkAsserts.checkDstDir;
import static ru.olegcherednik.zip4jvm.symlink.SymlinkAsserts.dirSymlinkCarsAssert;
import static ru.olegcherednik.zip4jvm.symlink.SymlinkAsserts.dirSymlinkDataAssert;

/**
 * @author Oleg Cherednik
 * @since 05.08.2026
 */
@Test
public class UnzipItSymlinkTest extends BaseTest {

    public void shouldIgnoreSymlinkWhenUnzipWithDefaultSettings() {
        Path dstDir = getTestRoot();

        UnzipIt.zip(symlinkPosixZip).dstDir(dstDir).extract();

        assertThatDirectory(dstDir)
                .hasOnlyDirectoriesRegularFiles(2, 1)
                .withDirectory("cars-rel-symlink", dirSymlinkCarsAssert)
                .withDirectory("data-abs-symlink", dirSymlinkDataAssert)
                .withRegularFile(fileNameDucati, fileDucatiAssert);
    }

    public void shouldIgnoreSymlinkWhenUnzipWithIgnoreSymlinkTrue() {
        UnzipSettings settings = UnzipSettings.builder().ignoreSymlink(true).build();
        Path dstDir = getTestRoot();

        UnzipIt.zip(symlinkPosixZip).settings(settings).dstDir(dstDir).extract();

        assertThatDirectory(dstDir)
                .hasOnlyDirectoriesRegularFiles(2, 1)
                .withDirectory("cars-rel-symlink", dirSymlinkCarsAssert)
                .withDirectory("data-abs-symlink", dirSymlinkDataAssert)
                .withRegularFile(fileNameDucati, fileDucatiAssert);
    }

    public void shouldNotIgnoreSymlinkWhenUnzipWithIgnoreSymlinkFalse() {
        UnzipSettings settings = UnzipSettings.builder().ignoreSymlink(false).build();
        Path dstDir = getTestRoot();

        UnzipIt.zip(symlinkPosixZip).settings(settings).dstDir(dstDir).extract();
        checkDstDir(dstDir);
    }

    public void shouldIgnoreSymlinkWhenUnzipCve20074559AndIgnoreSymlinkTrue() {
        UnzipSettings settings = UnzipSettings.builder().ignoreSymlink(true).build();
        Path dstDir = getTestRoot();

        UnzipIt.zip(symlinkCve20074550Zip).settings(settings).dstDir(dstDir).extract();

        assertThatDirectory(dstDir).exists().hasOnlyDirectoriesRegularFiles(1, 1);
        assertThatDirectory(dstDir).directory("escape").hasOnlyRegularFiles(1)
                                   .regularFile("pwned_via_symlink.txt")
                                   .hasContent("CVE-2007-4559: written OUTSIDE the extraction directory");
        assertThatDirectory(dstDir).regularFile("safe.txt").hasContent("harmless content");
    }

    public void shouldNotIgnoreUnderDstDirSymlinkWhenUnzipCve20074559AndIgnoreSymlinkFalse() {
        UnzipSettings settings = UnzipSettings.builder().ignoreSymlink(false).build();
        Path dstDir = getTestRoot();

        UnzipIt.zip(symlinkCve20074550Zip).settings(settings).dstDir(dstDir).extract();

        assertThatDirectory(dstDir).exists().hasOnlyDirectoriesRegularFiles(1, 1);
        assertThatDirectory(dstDir).directory("escape").hasOnlyRegularFiles(1)
                                   .regularFile("pwned_via_symlink.txt")
                                   .hasContent("CVE-2007-4559: written OUTSIDE the extraction directory");
        assertThatDirectory(dstDir).regularFile("safe.txt").hasContent("harmless content");
    }

}
