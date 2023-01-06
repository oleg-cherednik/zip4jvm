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

import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatFile;

/**
 * @author Oleg Cherednik
 * @since 11.10.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipInfoPrintShortInfoTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipInfoPrintShortInfoTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldRetrieveInfoWhenStoreSolid() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("actual.txt");
        Files.createDirectories(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSolid).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_solid.txt");
    }

    public void shouldRetrieveInfoWhenStoreSolidPkware() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("actual.txt");
        Files.createDirectories(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSolidPkware).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_solid_pkware.txt");
    }

    public void shouldRetrieveInfoWhenStoreSolidAes() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("actual.txt");
        Files.createDirectories(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSolidAes).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_solid_aes.txt");
    }

    public void shouldRetrieveInfoWhenStoreSplit() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("actual.txt");
        Files.createDirectories(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSplit).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_split.txt");
    }

    public void shouldRetrieveInfoWhenStoreSplitPkware() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("actual.txt");
        Files.createDirectories(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSplitPkware).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_split_pkware.txt");
    }

    public void shouldRetrieveInfoWhenStoreSplitAes() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("actual.txt");
        Files.createDirectories(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSplitAes).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_split_aes.txt");
    }

    public void shouldPrintShortInfoWhenSingleItemZip() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("actual.txt");

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(Zip4jvmSuite.getResourcePath("zip/single_item.zip")).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/single_item.txt");
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
    public void printShortInfo() throws IOException {
        ZipInfoSettings settings = ZipInfoSettings.builder().copyPayload(true).build();
        zipInfo().settings(settings).printShortInfo(System.out);
    }

}
