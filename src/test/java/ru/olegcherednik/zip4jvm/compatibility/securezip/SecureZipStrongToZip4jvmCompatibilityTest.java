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
package ru.olegcherednik.zip4jvm.compatibility.securezip;

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.secureZipBzip2SolidAes256StrongZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipBzip2SplitAes256StrongZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipDeflate64SolidAes256StrongZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipDeflateSolidAes256StrongZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipLzmaSolidAes256StrongZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipStoreSolidAes128StrongZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipStoreSolidAes192StrongZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipStoreSolidAes256StrongZip;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 07.12.2022
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class SecureZipStrongToZip4jvmCompatibilityTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(
            SecureZipStrongToZip4jvmCompatibilityTest.class);

    public void shouldUnzipWhenStoreSolidAes256Strong() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);
        UnzipIt.zip(secureZipStoreSolidAes256StrongZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenStoreSolidAes192Strong() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);
        UnzipIt.zip(secureZipStoreSolidAes192StrongZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenStoreSolidAes128Strong() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);
        UnzipIt.zip(secureZipStoreSolidAes128StrongZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenDeflateSolidAes256Strong() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);
        UnzipIt.zip(secureZipDeflateSolidAes256StrongZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenBzip2SolidAes256Strong() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);
        UnzipIt.zip(secureZipBzip2SolidAes256StrongZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenDeflate642SolidAes256Strong() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);
        UnzipIt.zip(secureZipDeflate64SolidAes256StrongZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenLzma2SolidAes256Strong() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);
        UnzipIt.zip(secureZipLzmaSolidAes256StrongZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenBzip2SplitAes256Strong() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);
        UnzipIt.zip(secureZipBzip2SplitAes256StrongZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

}
