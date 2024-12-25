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
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatFile;

/**
 * @author Oleg Cherednik
 * @since 11.10.2019
 */
@Test
public class ZipInfoPrintShortInfoTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(ZipInfoPrintShortInfoTest.class);
    private static final String ACTUAL_TXT = "actual.txt";

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(ROOT_DIR);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(ROOT_DIR);
    }

    public void shouldRetrieveInfoWhenStoreSolid() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(ACTUAL_TXT);
        Files.createDirectories(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSolid).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_solid.txt");
    }

    public void shouldRetrieveInfoWhenStoreSolidPkware() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(ACTUAL_TXT);
        Files.createDirectories(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSolidPkware).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_solid_pkware.txt");
    }

    public void shouldRetrieveInfoWhenStoreSolidAes() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(ACTUAL_TXT);
        Files.createDirectories(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSolidAes).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_solid_aes.txt");
    }

    public void shouldRetrieveInfoWhenStoreSplit() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(ACTUAL_TXT);
        Files.createDirectories(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSplit).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_split.txt");
    }

    public void shouldRetrieveInfoWhenStoreSplitPkware() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(ACTUAL_TXT);
        Files.createDirectories(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSplitPkware).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_split_pkware.txt");
    }

    public void shouldRetrieveInfoWhenStoreSplitAes() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(ACTUAL_TXT);
        Files.createDirectories(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSplitAes).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_split_aes.txt");
    }

    public void shouldPrintShortInfoWhenSingleItemZip() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(ACTUAL_TXT);

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(Zip4jvmSuite.getResourcePath("zip/single_item.zip")).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/single_item.txt");
    }

}
