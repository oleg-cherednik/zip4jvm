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
package ru.olegcherednik.zip4jvm.compatibility.sevenzip;

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.UnzipIt;

import org.testng.annotations.Test;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.sevenZipLzmaSolidAesZip;
import static ru.olegcherednik.zip4jvm.TestData.sevenZipLzmaSolidZip;
import static ru.olegcherednik.zip4jvm.TestData.sevenZipStoreSolidPkwareZip;
import static ru.olegcherednik.zip4jvm.TestData.sevenZipStoreSplitZip;
import static ru.olegcherednik.zip4jvm.TestData.sevenZipZstdSolidAesZip;
import static ru.olegcherednik.zip4jvm.TestData.sevenZipZstdSolidZip;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 25.01.2020
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class SevenZipToZip4jvmCompatibilityTest extends BaseTest {

    public void shouldUnzipWhenLzmaSolid() {
        Path dstDir = getTestRoot();
        UnzipIt.zip(sevenZipLzmaSolidZip).dstDir(dstDir).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenLzmaAndAesEncryption() {
        Path dstDir = getTestRoot();
        UnzipIt.zip(sevenZipLzmaSolidAesZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenLzmaAndPkwareEncryption() {
        Path dstDir = getTestRoot();
        UnzipIt.zip(sevenZipStoreSolidPkwareZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenSevenZipSplit() {
        Path dstDir = getTestRoot();
        UnzipIt.zip(sevenZipStoreSplitZip).dstDir(dstDir).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    public void shouldUnzipWhenZstdSolid() {
        Path dstDir = getTestRoot();
        UnzipIt.zip(sevenZipZstdSolidZip).dstDir(dstDir).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenZstdAndAesEncryption() {
        Path dstDir = getTestRoot();
        UnzipIt.zip(sevenZipZstdSolidAesZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dir -> {
            dir.exists().hasOnlyRegularFiles(2);
            dir.regularFile("one.txt").hasSize(3);
            dir.regularFile("two.txt").hasSize(6);
        });
    }

}
