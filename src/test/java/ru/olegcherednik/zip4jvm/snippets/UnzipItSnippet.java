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

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolidAes;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolidPkware;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.fileNamePasswordProvider;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.passwordStr;

/**
 * @author Oleg Cherednik
 * @since 05.10.2019
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class UnzipItSnippet {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();
    private static final Path FILENAME_ZIP = DIR_ROOT.resolve("filename.zip");
    private static final String FILENAME_CONTENT = "filename_content";

    @BeforeClass
    public static void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
        Quietly.doRuntime(() -> FileUtils.copyFile(zipDeflateSolid.toFile(), FILENAME_ZIP.toFile()));
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void extractAllEntriesIntoGivenDirectory() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(FILENAME_CONTENT);
        UnzipIt.zip(FILENAME_ZIP).dstDir(dstDir).extract();
    }

    public void extractRegularFileIntoGivenDirectory() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(FILENAME_CONTENT);
        UnzipIt.zip(FILENAME_ZIP).dstDir(dstDir).extract("cars/bentley-continental.jpg");
    }

    public void extractDirectoryIntoGivenDirectory() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(FILENAME_CONTENT);
        UnzipIt.zip(FILENAME_ZIP).dstDir(dstDir).extract("cars");
    }

    public void extractSomeEntriesIntoGivenDirectory() {
        List<String> fileNames = Arrays.asList(dirNameCars,
                                               dirNameBikes + '/' + fileNameDucati,
                                               fileNameSaintPetersburg);
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(FILENAME_CONTENT);
        UnzipIt.zip(FILENAME_ZIP).dstDir(dstDir).extract(fileNames);
    }

    public void getStreamForRegularFileEntry() throws IOException {
        Path destFile = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("bentley.jpg");
        Zip4jvmSuite.createDir(destFile.getParent());

        try (InputStream in = UnzipIt.zip(FILENAME_ZIP).stream("cars/bentley-continental.jpg");
             OutputStream out = PathUtils.newOutputStream(destFile.toFile().toPath())) {
            IOUtils.copyLarge(in, out);
        }
    }

    public void unzipWithSinglePasswordForAllEntries() throws IOException {
        Path srcZip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("filename.zip");
        FileUtils.copyFile(zipDeflateSolidPkware.toFile(), srcZip.toFile());

        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(FILENAME_CONTENT);

        char[] password = passwordStr.toCharArray();
        List<String> fileNames = Arrays.asList(dirNameCars,
                                               dirNameBikes + '/' + fileNameDucati,
                                               fileNameSaintPetersburg);
        UnzipIt.zip(srcZip).dstDir(dstDir).password(password).extract(fileNames);
    }

    public void unzipWithSeparatePasswordForEachEntry() throws IOException {
        Path srcZip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("filename.zip");
        FileUtils.copyFile(zipDeflateSolidAes.toFile(), srcZip.toFile());

        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(FILENAME_CONTENT);

        UnzipSettings settings = UnzipSettings.builder().passwordProvider(fileNamePasswordProvider).build();
        List<String> fileNames = Arrays.asList(dirNameCars,
                                               dirNameBikes + '/' + fileNameDucati,
                                               fileNameSaintPetersburg);
        UnzipIt.zip(srcZip).dstDir(dstDir).settings(settings).extract(fileNames);
    }

}
