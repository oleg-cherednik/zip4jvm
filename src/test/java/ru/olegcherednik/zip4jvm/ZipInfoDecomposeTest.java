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
package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 11.03.2021
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipInfoDecomposeTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipInfoDecomposeTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldDecomposeWhenStoreSolid() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.zipStoreSolid).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/store_solid");
    }

    public void shouldDecomposeWhenStoreSolidPkware() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.zipStoreSolidPkware).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/store_solid_pkware");
    }

    public void shouldDecomposeWhenStoreSolidAes() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.zipStoreSolidAes).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/store_solid_aes");
    }

    public void shouldDecomposeWhenStoreSplit() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.zipStoreSplit).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/store_split");
    }

    public void shouldDecomposeWhenStoreSplitPkware() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.zipStoreSplitPkware).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/store_split_pkware");
    }

    public void shouldDecomposeWhenStoreSplitAes() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.zipStoreSplitAes).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/store_split_aes");
    }

    public void shouldDecomposeWhenSingleItemZip() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(Zip4jvmSuite.getResourcePath("zip/single_item.zip")).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/single_item");
    }

    public void shouldDecomposeWhenStrongStoreAes() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.secureZipStoreSolidAes256StrongZip).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/strong_store_aes");
    }

    public void shouldDecomposeWhenStrongDeflateAes() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.secureZipDeflateSolidAes256StrongZip).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/strong_deflate_aes");
    }

    public void shouldDecomposeWhenStrongBzip2Aes() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.secureZipBzip2SolidAes256StrongZip).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/strong_bzip2_aes");
    }

    public void shouldDecomposeWhenStrongDeflate64Aes() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.secureZipDeflate64SolidAes256StrongZip).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/strong_deflate64_aes");
    }

    public void shouldDecomposeWhenStrongLzmaAes() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.secureZipLzmaSolidAes256StrongZip).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/strong_lzma_aes");
    }

    // TODO should be enabled (disabled during refactoring)
    @Test(enabled = false)
    public void shouldDecomposeWhenStrongStoreAesEcd() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.secureZipStoreSolidAes256StrongEcdZip).password(password).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/ecd/strong_store_aes_ecd");
    }

    public void shouldDecomposeWhenStrongDeflateAesEcd() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.secureZipDeflateSolidAes256StrongEcdZip).password(password).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/ecd/strong_deflate_aes_ecd");
    }

    public void shouldDecomposeWhenStrongBzip2AesEcd() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.secureZipBzip2SolidAes256StrongEcdZip).password(password).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/ecd/strong_bzip2_aes_ecd");
    }

    public void shouldDecomposeWhenStrongDeflate64AesEcd() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.secureZipDeflate64SolidAes256StrongEcdZip).password(password).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/ecd/strong_deflate64_aes_ecd");
    }

    public void shouldDecomposeWhenStrongLzmaAesEcd() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.secureZipLzmaSolidAes256StrongEcdZip).password(password).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/ecd/strong_lzma_aes_ecd");
    }

    public void shouldDecomposeWhenStrongBzip2AesSplit() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.secureZipBzip2SplitAes256StrongZip).password(password).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/strong_bzip2_aes_split");
    }

    public void shouldDecomposeWhenStrongBzip2AesSplitEcd() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir.getParent());
        ZipInfo.zip(TestData.secureZipBzip2SplitAes256StrongEcdZip).password(password).decompose(dir);
        assertThatDirectory(dir).matchesResourceDirectory("/decompose/strong/ecd/strong_bzip2_aes_split_ecd");
    }

}
