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

import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.symlinkPosixZip;
import static ru.olegcherednik.zip4jvm.TestData.symlinkWinZip;
import static ru.olegcherednik.zip4jvm.symlink.SymlinkAsserts.checkDstDir;

/**
 * @author Oleg Cherednik
 * @since 18.03.2023
 */
@Test
public class SymlinkCompatibilityTest extends BaseTest {

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    @BeforeMethod(onlyForGroups = "windows-only")
    public void checkOperatingSystem() {
        // Skip if the OS is not Windows
        if (!isWindows())
            throw new SkipException("Skipping test because it is not running on Windows.");
    }

    public void shouldUnzipPosixZipWithSymlink() {
        UnzipSettings settings = UnzipSettings.builder().ignoreSymlink(false).build();
        Path dstDir = getTestRoot();

        UnzipIt.zip(symlinkPosixZip).settings(settings).dstDir(dstDir).extract();
        checkDstDir(dstDir);
    }

    @Test(groups = "windows-only")
    public void shouldUnzipWinZipWithSymlink() {
        UnzipSettings settings = UnzipSettings.builder().ignoreSymlink(false).build();
        Path dstDir = getTestRoot();

        UnzipIt.zip(symlinkWinZip).settings(settings).dstDir(dstDir).extract();
        checkDstDir(dstDir);
    }

}
