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
package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 11.03.2021
 */
@Test
public class ZipInfoDecomposeTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @BeforeClass
    public static void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldDecomposeWhenStoreSolid() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.zipStoreSolid).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/store_solid");
    }

    public void shouldDecomposeWhenStoreSolidPkware() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.zipStoreSolidPkware).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/store_solid_pkware");
    }

    public void shouldDecomposeWhenStoreSolidAes() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.zipStoreSolidAes).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/store_solid_aes");
    }

    public void shouldDecomposeWhenStoreSplit() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.zipStoreSplit).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/store_split");
    }

    public void shouldDecomposeWhenStoreSplitPkware() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.zipStoreSplitPkware).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/store_split_pkware");
    }

    public void shouldDecomposeWhenStoreSplitAes() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.zipStoreSplitAes).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/store_split_aes");
    }

    public void shouldDecomposeWhenSingleItemZip() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(Zip4jvmSuite.getResourcePath("zip/single_item.zip")).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/single_item");
    }

    public void shouldDecomposeWhenStrongStoreAes() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.secureZipStoreSolidAes256StrongZip).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/strong_store_aes");
    }

    public void shouldDecomposeWhenStrongDeflateAes() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.secureZipDeflateSolidAes256StrongZip).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/strong_deflate_aes");
    }

    public void shouldDecomposeWhenStrongBzip2Aes() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.secureZipBzip2SolidAes256StrongZip).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/strong_bzip2_aes");
    }

    public void shouldDecomposeWhenStrongDeflate64Aes() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.secureZipDeflate64SolidAes256StrongZip).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/strong_deflate64_aes");
    }

    public void shouldDecomposeWhenStrongLzmaAes() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.secureZipLzmaSolidAes256StrongZip).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/strong_lzma_aes");
    }

    // TODO should be enabled (disabled during refactoring)
    @Test(enabled = false)
    public void shouldDecomposeWhenStrongStoreAesEcd() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.secureZipStoreSolidAes256StrongEcdZip).password(password).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/ecd/strong_store_aes_ecd");
    }

    @Test(enabled = false)
    public void shouldDecomposeWhenStrongDeflateAesEcd() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.secureZipDeflateSolidAes256StrongEcdZip).password(password).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/ecd/strong_deflate_aes_ecd");
    }

    @Test(enabled = false)
    public void shouldDecomposeWhenStrongBzip2AesEcd() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.secureZipBzip2SolidAes256StrongEcdZip).password(password).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/ecd/strong_bzip2_aes_ecd");
    }

    @Test(enabled = false)
    public void shouldDecomposeWhenStrongDeflate64AesEcd() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.secureZipDeflate64SolidAes256StrongEcdZip).password(password).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/ecd/strong_deflate64_aes_ecd");
    }

    @Test(enabled = false)
    public void shouldDecomposeWhenStrongLzmaAesEcd() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.secureZipLzmaSolidAes256StrongEcdZip).password(password).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/ecd/strong_lzma_aes_ecd");
    }

    public void shouldDecomposeWhenStrongBzip2AesSplit() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.secureZipBzip2SplitAes256StrongZip).password(password).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/strong_bzip2_aes_split");
    }

    @Test(enabled = false)
    public void shouldDecomposeWhenStrongBzip2AesSplitEcd() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        ZipInfo.zip(TestData.secureZipBzip2SplitAes256StrongEcdZip).password(password).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/ecd/strong_bzip2_aes_split_ecd");
    }

}
