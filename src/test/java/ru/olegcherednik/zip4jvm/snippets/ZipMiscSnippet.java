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
package ru.olegcherednik.zip4jvm.snippets;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipMisc;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileNameFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSplit;

/**
 * @author Oleg Cherednik
 * @since 07.10.2019
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class ZipMiscSnippet {

    private static final String FILE_NAME = "filename.zip";
    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();
    private static final Path FILENAME_ZIP = DIR_ROOT.resolve(FILE_NAME);

    @BeforeClass
    public static void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
        Quietly.doRuntime(() -> FileUtils.copyFile(zipDeflateSolid.toFile(), FILENAME_ZIP.toFile()));
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void modifyZipArchiveComment() throws IOException {
        Path srcZip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(FILE_NAME);
        FileUtils.copyFile(zipDeflateSolid.toFile(), srcZip.toFile());

        ZipMisc zipFile = ZipMisc.zip(srcZip);

        assertThat(zipFile.getComment()).isNull();

        zipFile.setComment("new comment");
        assertThat(zipFile.getComment()).isEqualTo("new comment");

        zipFile.setComment("  ");
        assertThat(zipFile.getComment()).isEqualTo("  ");

        zipFile.setComment("");
        assertThat(zipFile.getComment()).isNull();

        zipFile.setComment(null);
        assertThat(zipFile.getComment()).isNull();
    }

    public void removeEntryByName() throws IOException {
        Path srcZip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(FILE_NAME);
        FileUtils.copyFile(zipDeflateSolid.toFile(), srcZip.toFile());

        ZipMisc zipFile = ZipMisc.zip(srcZip);
        zipFile.removeEntryByName(dirNameCars + '/' + fileNameFerrari);
    }

    public void removeSomeEntriesByName() throws IOException {
        Path srcZip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(FILE_NAME);
        FileUtils.copyFile(zipDeflateSolid.toFile(), srcZip.toFile());

        Collection<String> entryNames = Arrays.asList(dirNameCars + '/' + fileNameFerrari,
                                                      dirNameBikes + '/' + fileNameHonda);

        ZipMisc zipFile = ZipMisc.zip(srcZip);
        zipFile.removeEntryByName(entryNames);
    }

    public void removeEntryByNamePrefix() throws IOException {
        Path srcZip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(FILE_NAME);
        FileUtils.copyFile(zipDeflateSolid.toFile(), srcZip.toFile());

        ZipMisc zipFile = ZipMisc.zip(srcZip);
        zipFile.removeEntryByNamePrefix(dirNameCars);
    }

    public void checkWhetherZipArchiveSplitOrNot() {
        assertThat(ZipMisc.zip(FILENAME_ZIP).isSplit()).isFalse();
    }

    public void mergeSplitArchiveIntoSolidOne() {
        Path srcZip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(FILE_NAME);
        ZipMisc zipFile = ZipMisc.zip(zipDeflateSplit);
        zipFile.merge(srcZip);
    }

}
