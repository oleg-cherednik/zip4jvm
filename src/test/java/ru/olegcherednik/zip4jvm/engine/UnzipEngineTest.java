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
package ru.olegcherednik.zip4jvm.engine;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.fileNamePasswordProvider;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class UnzipEngineTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(UnzipEngineTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldUnzipZipFileIntoDestinationFolderWhenDeflateSolid() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        UnzipIt.zip(zipDeflateSolid).destDir(destDir).extract(dirNameCars);

        assertThatDirectory(destDir).exists().hasDirectories(1).hasFiles(0);
        assertThatDirectory(destDir.resolve(dirNameCars)).matches(dirCarsAssert);
    }

    public void shouldUnzipZipFileIntoDestinationFolderWhenDeflateSolidPkware() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipSettings settings = UnzipSettings.builder().password(password).build();

        UnzipIt.zip(zipDeflateSolid).settings(settings).destDir(destDir).extract(dirNameCars);

        assertThatDirectory(destDir).exists().hasDirectories(1).hasFiles(0);
        assertThatDirectory(destDir.resolve(dirNameCars)).matches(dirCarsAssert);
    }

    public void shouldUnzipZipFileIntoDestinationFolderWhenDeflateSolidAes() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipSettings settings = UnzipSettings.builder().passwordProvider(fileNamePasswordProvider).build();

        UnzipIt.zip(zipDeflateSolid).settings(settings).destDir(destDir).extract(dirNameCars);

        assertThatDirectory(destDir).exists().hasDirectories(1).hasFiles(0);
        assertThatDirectory(destDir.resolve(dirNameCars)).matches(dirCarsAssert);
    }

    public void shouldCorrectlySetLastTimeStampWhenUnzip() throws IOException, ParseException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Path file = destDir.resolve("foo.txt");
        final String str = "2014.10.29T18:10:44";
        FileUtils.writeStringToFile(file.toFile(), "oleg.cherednik", Charsets.UTF_8);

        Files.setLastModifiedTime(file, FileTime.fromMillis(convert(str)));

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.zip(zip).add(file);

        Path unzipDir = destDir.resolve("unzip");
        UnzipIt.zip(zip).destDir(unzipDir).extract();

        Path fileFooUnzip = unzipDir.resolve("foo.txt");
        assertThat(convert(Files.getLastModifiedTime(fileFooUnzip).toMillis())).isEqualTo(str);
    }

    private static long convert(String str) throws ParseException {
        return new SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss").parse(str).getTime();
    }

    private static String convert(long time) {
        return new SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss").format(new Date(time));
    }

}
