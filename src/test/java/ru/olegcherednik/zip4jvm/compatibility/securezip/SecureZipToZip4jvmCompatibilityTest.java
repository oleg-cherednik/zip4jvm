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

import static ru.olegcherednik.zip4jvm.TestData.secureZipBzip2SolidPkwareZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipBzip2SolidZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipEnhancedDeflateSolidZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipLzmaSolidZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipStoreSolidAesZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipStoreSplitZip;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
@Test
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention" })
public class SecureZipToZip4jvmCompatibilityTest {

    private static final Path rootDir =
            Zip4jvmSuite.generateSubDirNameWithTime(SecureZipToZip4jvmCompatibilityTest.class);

    public void shouldUnzipWhenLzmaSolid() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.zip(secureZipLzmaSolidZip).dstDir(dstDir).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenBzip2Solid() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.zip(secureZipBzip2SolidZip).dstDir(dstDir).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenBzip2AndPkwareEncryption() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.zip(secureZipBzip2SolidPkwareZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenStoreAndAesEncryption() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.zip(secureZipStoreSolidAesZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenSplit() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.zip(secureZipStoreSplitZip).dstDir(dstDir).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    public void shouldUnzipWhenEnhancedDeflateSolid() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.zip(secureZipEnhancedDeflateSolidZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

}
