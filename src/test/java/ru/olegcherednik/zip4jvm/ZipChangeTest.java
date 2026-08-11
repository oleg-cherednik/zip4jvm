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

import ru.olegcherednik.zip4jvm.engine.zip.ZipEngine;
import ru.olegcherednik.zip4jvm.exception.EntryDuplicationException;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;

/**
 * @author Oleg Cherednik
 * @since 11.04.2025
 */
@Test
public class ZipChangeTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @BeforeClass
    public void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldNotChangeSrcZipWhenAddDuplicateEntry() throws IOException, InterruptedException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("src.zip");
        ZipIt.zip(zip).add(fileBentley);
        Thread.sleep(500);

        long expectedLastModifiedTime = Files.getLastModifiedTime(zip).toMillis();
        assertThatThrownBy(() -> ZipIt.zip(zip).add(fileBentley)).isExactlyInstanceOf(EntryDuplicationException.class);
        assertThat(Files.getLastModifiedTime(zip).toMillis()).isEqualTo(expectedLastModifiedTime);
    }

    public void shouldNotChangeSrcZipWhenNoChanges() throws IOException, InterruptedException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("src.zip");
        ZipIt.zip(zip).add(fileBentley);
        Thread.sleep(500);

        long expectedLastModifiedTime = Files.getLastModifiedTime(zip).toMillis();

        try (ZipEngine zipFile = ZipFile.writer(zip, ZipSettings.of(CompressionEnum.STORE))) {
            zipFile.markSuccess();
        }

        assertThat(Files.getLastModifiedTime(zip).toMillis()).isEqualTo(expectedLastModifiedTime);
    }

    public void shouldChangeSrcZipWhenRemoveEntry() throws IOException, InterruptedException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("src.zip");
        ZipIt.zip(zip).add(fileBentley);

        Thread.sleep(500);

        long lastModifiedTime = Files.getLastModifiedTime(zip).toMillis();

        try (ZipEngine zipFile = ZipFile.writer(zip, ZipSettings.of(CompressionEnum.STORE))) {
            zipFile.removeEntryByName(fileNameBentley);
            zipFile.markSuccess();
        }

        assertThat(Files.getLastModifiedTime(zip).toMillis()).isGreaterThan(lastModifiedTime);
    }

}
