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
package ru.olegcherednik.zip4jvm.zipit;

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;

import org.testng.annotations.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 14.04.2025
 */
@Test
public class ZipItAddWithEntryNameTest extends BaseTest {

    public void shouldAddFileAndRenameToName() {
        Path zip = getZip();
        ZipIt.zip(zip).add(fileBentley, "foo.jpg");
        assertThatZipFile(zip).isSolid().root().withRegularFile("foo.jpg", fileBentleyAssert);
    }

    public void shouldAddFileAndRenameToDirAndName() {
        Path zip = getZip();
        ZipIt.zip(zip).add(fileBentley, "sub/foo.jpg");

        assertThatZipFile(zip)
                .root().hasOnlyDirectories(1)
                .directory("sub").hasOnlyRegularFiles(1)
                .regularFile("foo.jpg").matches(fileBentleyAssert);
    }

    public void shouldAddFileAndRenameToDirAndNameWithDot() {
        Path zip = getZip();
        ZipIt.zip(zip).add(fileBentley, "sub/..foo.jpg");

        assertThatZipFile(zip)
                .root().hasOnlyDirectories(1)
                .directory("sub").hasOnlyRegularFiles(1)
                .regularFile("..foo.jpg").matches(fileBentleyAssert);
    }

    public void shouldAddFileAndRenameToDirAndNameSimilarWithDirName() {
        Path zip = getZip();
        ZipIt.zip(zip).add(fileBentley, "dir_name");

        assertThatZipFile(zip)
                .root().hasOnlyRegularFiles(1)
                .regularFile("dir_name").matches(fileBentleyAssert);
    }

    public void shouldThrowIllegalArgumentExceptionWhenRenameToRelativeDir() {
        Path zip = getZip();

        assertThatThrownBy(() -> ZipIt.zip(zip).add(fileBentley, "../foo.jpg"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    public void shouldAddDirAndRenameToName() {
        Path zip = getZip();
        ZipIt.zip(zip).add(dirCars, "super_cars");

        assertThatZipFile(zip)
                .root().hasOnlyDirectories(1)
                .directory("super_cars").matches(dirCarsAssert);
    }

    public void shouldIgnoreEntryWhenExtractToAboveDstDir() {
        Path dstDir = getTestRoot();
        Path zip = Zip4jvmSuite.getResourcePath("/zip/cve_slip.zip");
        UnzipIt.zip(zip).dstDir(dstDir).extract();
        assertThatDirectory(dstDir).hasEntries(0);
    }

}
