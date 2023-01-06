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
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatFile;

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

    private static ZipInfo zipInfo() {
        Path path = Paths.get("d:/zip4jvm/tmp/aes.zip");
//        Files.deleteIfExists(path);

//        ZipInfo res = ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/lzma/lzma_16mb.zip"));
//        ZipInfo res = ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/lzma/lzma_1mb_32.zip"));
//        ZipInfo res = ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/lzma/enc/lzma-ultra.zip"));
//        res = res.settings(ZipInfoSettings.builder().readEntries(false).build());
//        ZipInfo res = ZipInfo.zip(sevenZipLzmaSolidZip);
//        ZipInfo res = ZipInfo.zip(Paths.get("d:/zip4jvm/3des/3des_store_168.zip"));
//        ZipInfo res = ZipInfo.zip(Paths.get("d:/zip4jvm/bzip2/bzip2.zip"));
//        ZipInfo res = ZipInfo.zip(Paths.get("d:/zip4jvm/bzip2/min.zip"));
        ZipInfo res = ZipInfo.zip(Paths.get("d:/zip4jvm/ZIpCrypto/src.zip"));

//        ZipInfo res = ZipInfo.zip(Paths.get("d:/zip4jvm/securezip/aes/aes128.zip"));
//        ZipInfo res = ZipInfo.zip(Paths.get("d:/zip4jvm/securezip/aes/aes192.zip"));
//        ZipInfo res = ZipInfo.zip(Paths.get("d:/zip4jvm/securezip/aes/aes256.zip"));
//        ZipInfo res = ZipInfo.zip(Paths.get("D:\\zip4jvm\\foo\\compression\\1581465466689\\CompressionLzmaTest\\shouldCreateSingleZipWithFilesWhenLzmaCompressionAndAesEncryption/src.zip"));
//        ZipInfo res = ZipInfo.zip(
//                Paths.get("D:\\zip4jvm\\foo\\encryption\\1581466463189\\EncryptionAesTest\\shouldCreateNewZipWithFolderAndAes256Encryption/src.zip"));

        return res;
    }

    @Test(enabled = false)
    public void decompose() throws IOException {
        ZipInfoSettings settings = ZipInfoSettings.builder().copyPayload(true).build();
        zipInfo().settings(settings).decompose(Zip4jvmSuite.subDirNameAsMethodName(rootDir));
    }

}
