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
package ru.olegcherednik.zip4jvm.compatibility.sevenzip;

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

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
public class SevenZipToZip4jvmCompatibilityTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    public void shouldUnzipWhenLzmaSolid() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        UnzipIt.zip(sevenZipLzmaSolidZip).dstDir(dstDir).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenLzmaAndAesEncryption() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        UnzipIt.zip(sevenZipLzmaSolidAesZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenLzmaAndPkwareEncryption() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        UnzipIt.zip(sevenZipStoreSolidPkwareZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenSevenZipSplit() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        UnzipIt.zip(sevenZipStoreSplitZip).dstDir(dstDir).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    public void shouldUnzipWhenZstdSolid() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        UnzipIt.zip(sevenZipZstdSolidZip).dstDir(dstDir).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenZstdAndAesEncryption() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        UnzipIt.zip(sevenZipZstdSolidAesZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dir -> {
            dir.exists().hasDirectories(0).hasRegularFiles(2);
            dir.regularFile("one.txt").hasSize(3);
            dir.regularFile("two.txt").hasSize(6);
        });
    }

}
