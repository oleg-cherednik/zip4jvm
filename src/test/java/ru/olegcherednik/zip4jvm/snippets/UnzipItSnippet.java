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
package ru.olegcherednik.zip4jvm.snippets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

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
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention" })
public class UnzipItSnippet {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(UnzipItSnippet.class);
    private static final Path zip = rootDir.resolve("filename.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
        FileUtils.copyFile(zipDeflateSolid.toFile(), zip.toFile());
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void extractAllEntriesIntoGivenDirectory() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename_content");
        UnzipIt.zip(zip).destDir(destDir).extract();
    }

    public void extractRegularFileIntoGivenDirectory() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename_content");
        UnzipIt.zip(zip).destDir(destDir).extract("cars/bentley-continental.jpg");
    }

    public void extractDirectoryIntoGivenDirectory() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename_content");
        UnzipIt.zip(zip).destDir(destDir).extract("cars");
    }

    public void extractSomeEntriesIntoGivenDirectory() throws IOException {
        List<String> fileNames = Arrays.asList(dirNameCars, dirNameBikes + '/' + fileNameDucati, fileNameSaintPetersburg);
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename_content");
        UnzipIt.zip(zip).destDir(destDir).extract(fileNames);
    }

    public void getStreamForRegularFileEntry() throws IOException {
        Path destFile = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("bentley.jpg");
        Files.createDirectories(destFile.getParent());

        try (InputStream in = UnzipIt.zip(zip).stream("cars/bentley-continental.jpg"); OutputStream out = new FileOutputStream(destFile.toFile())) {
            IOUtils.copyLarge(in, out);
        }
    }

    public void unzipWithSinglePasswordForAllEntries() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");
        FileUtils.copyFile(zipDeflateSolidPkware.toFile(), zip.toFile());

        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename_content");

        char[] password = passwordStr.toCharArray();
        List<String> fileNames = Arrays.asList(dirNameCars, dirNameBikes + '/' + fileNameDucati, fileNameSaintPetersburg);
        UnzipIt.zip(zip).destDir(destDir).password(password).extract(fileNames);
    }

    public void unzipWithSeparatePasswordForEachEntry() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");
        FileUtils.copyFile(zipDeflateSolidAes.toFile(), zip.toFile());

        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename_content");

        UnzipSettings settings = UnzipSettings.builder().passwordProvider(fileNamePasswordProvider).build();
        List<String> fileNames = Arrays.asList(dirNameCars, dirNameBikes + '/' + fileNameDucati, fileNameSaintPetersburg);
        UnzipIt.zip(zip).destDir(destDir).settings(settings).extract(fileNames);
    }

}
