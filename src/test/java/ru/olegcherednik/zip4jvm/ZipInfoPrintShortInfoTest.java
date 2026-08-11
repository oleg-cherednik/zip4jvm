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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatFile;

/**
 * @author Oleg Cherednik
 * @since 11.10.2019
 */
@Test
public class ZipInfoPrintShortInfoTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();
    private static final String ACTUAL_TXT = "actual.txt";

    @BeforeClass
    public void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldRetrieveInfoWhenStoreSolid() throws FileNotFoundException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(ACTUAL_TXT);
        Zip4jvmSuite.createDir(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSolid).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_solid.txt");
    }

    public void shouldRetrieveInfoWhenStoreSolidPkware() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(ACTUAL_TXT);
        Zip4jvmSuite.createDir(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSolidPkware).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_solid_pkware.txt");
    }

    public void shouldRetrieveInfoWhenStoreSolidAes() throws FileNotFoundException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(ACTUAL_TXT);
        Zip4jvmSuite.createDir(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSolidAes).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_solid_aes.txt");
    }

    public void shouldRetrieveInfoWhenStoreSplit() throws FileNotFoundException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(ACTUAL_TXT);
        Zip4jvmSuite.createDir(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSplit).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_split.txt");
    }

    public void shouldRetrieveInfoWhenStoreSplitPkware() throws FileNotFoundException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(ACTUAL_TXT);
        Zip4jvmSuite.createDir(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSplitPkware).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_split_pkware.txt");
    }

    public void shouldRetrieveInfoWhenStoreSplitAes() throws FileNotFoundException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(ACTUAL_TXT);
        Zip4jvmSuite.createDir(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSplitAes).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/store_split_aes.txt");
    }

    public void shouldPrintShortInfoWhenSingleItemZip() throws FileNotFoundException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(ACTUAL_TXT);

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(Zip4jvmSuite.getResourcePath("zip/single_item.zip")).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/single_item.txt");
    }

}
