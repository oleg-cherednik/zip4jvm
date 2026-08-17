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
package ru.olegcherednik.zip4jvm.compatibility.securezip;

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.UnzipIt;

import org.testng.annotations.Test;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.secureZipBzip2SolidAes256StrongEcdZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipBzip2SplitAes256StrongEcdZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipDeflate64SolidAes256StrongEcdZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipDeflateSolidAes256StrongEcdZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipLzmaSolidAes256StrongEcdZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipStoreSolid3Des168StrongEcdZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipStoreSolidAes256StrongEcdZip;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 10.12.2022
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class SecureZipEncryptedCentralDirectoryToZip4jvmCompatibilityTest extends BaseTest {

    public void shouldUnzipWhenStoreSolidAes256StrongEncryptedCentralDirectory() {
        Path dstDir = getTestRoot();
        UnzipIt.zip(secureZipStoreSolidAes256StrongEcdZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenDeflateAes256StrongEncryptedCentralDirectory() {
        Path dstDir = getTestRoot();
        UnzipIt.zip(secureZipDeflateSolidAes256StrongEcdZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenDeflate64Aes256StrongEncryptedCentralDirectory() {
        Path dstDir = getTestRoot();
        UnzipIt.zip(secureZipDeflate64SolidAes256StrongEcdZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenLzmaAes256StrongEncryptedCentralDirectory() {
        Path dstDir = getTestRoot();
        UnzipIt.zip(secureZipLzmaSolidAes256StrongEcdZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenBzip2Aes256StrongEncryptedCentralDirectory() {
        Path dstDir = getTestRoot();
        UnzipIt.zip(secureZipBzip2SolidAes256StrongEcdZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenBzip2SplitAes256StrongEncryptedCentralDirectory() {
        Path dstDir = getTestRoot();
        UnzipIt.zip(secureZipBzip2SplitAes256StrongEcdZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    public void shouldUnzipWhenStoreSolid3des168StrongEncryptedCentralDirectory() {
        Path dstDir = getTestRoot();
        UnzipIt.zip(secureZipStoreSolid3Des168StrongEcdZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).hasOnlyRegularFiles(1)
                                   .withRegularFile(fileNameBentley, fileBentleyAssert);
    }

}
