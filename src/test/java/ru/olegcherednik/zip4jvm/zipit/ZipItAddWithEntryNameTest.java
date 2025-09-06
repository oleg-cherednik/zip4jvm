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
package ru.olegcherednik.zip4jvm.zipit;

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
public class ZipItAddWithEntryNameTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();
    private static final String SRC_ZIP = "src.zip";

    public void shouldAddFileAndRenameToName() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(SRC_ZIP);
        ZipIt.zip(zip).add(fileBentley, "foo.jpg");
        assertThatZipFile(zip).root().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).regularFile("foo.jpg").exists().matches(fileBentleyAssert);
    }

    public void shouldAddFileAndRenameToDirAndName() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(SRC_ZIP);
        ZipIt.zip(zip).add(fileBentley, "sub/foo.jpg");
        assertThatZipFile(zip).root().hasOnlyDirectories(1);
        assertThatZipFile(zip).directory("sub").exists().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).regularFile("sub/foo.jpg").exists().matches(fileBentleyAssert);
    }

    public void shouldAddFileAndRenameToDirAndNameWithDot() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(SRC_ZIP);
        ZipIt.zip(zip).add(fileBentley, "sub/..foo.jpg");
        assertThatZipFile(zip).root().hasOnlyDirectories(1);
        assertThatZipFile(zip).directory("sub").exists().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).regularFile("sub/..foo.jpg").exists().matches(fileBentleyAssert);
    }

    public void shouldAddFileAndRenameToDirAndNameSimilarWithDirName() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(SRC_ZIP);
        ZipIt.zip(zip).add(fileBentley, "dir_name");
        assertThatZipFile(zip).root().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).regularFile("dir_name").exists().matches(fileBentleyAssert);
    }

    public void shouldThrowIllegalArgumentExceptionWhenRenameToRelativeDir() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(SRC_ZIP);

        assertThatThrownBy(() -> ZipIt.zip(zip).add(fileBentley, "../foo.jpg"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    public void shouldAddDirAndRenameToName() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(SRC_ZIP);
        ZipIt.zip(zip).add(dirCars, "super_cars");
        assertThatZipFile(zip).root().hasOnlyDirectories(1);
        assertThatZipFile(zip).directory("super_cars").exists().matches(dirCarsAssert);
    }

    public void shouldIgnoreEntryWhenExtractToAboveDstDir() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path zip = Zip4jvmSuite.getResourcePath("/zip/cve_slip.zip");
        UnzipIt.zip(zip).dstDir(dstDir).extract();
        assertThatDirectory(dstDir).hasEntries(0);
    }

}
