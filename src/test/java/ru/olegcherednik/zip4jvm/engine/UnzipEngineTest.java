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
package ru.olegcherednik.zip4jvm.engine;

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.charset.Charsets;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.fileNamePasswordProvider;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
@Test
public class UnzipEngineTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @BeforeClass
    public void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldUnzipZipFileIntoDestinationFolderWhenDeflateSolid() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);

        UnzipIt.zip(zipDeflateSolid).dstDir(dstDir).extract(dirNameCars);
        assertThatDirectory(dstDir).matches(dirCarsAssert);
    }

    public void shouldUnzipZipFileIntoDestinationFolderWhenDeflateSolidPkware() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        UnzipSettings settings = UnzipSettings.builder().password(password).build();

        UnzipIt.zip(zipDeflateSolid).settings(settings).dstDir(dstDir).extract(dirNameCars);
        assertThatDirectory(dstDir).matches(dirCarsAssert);
    }

    public void shouldUnzipZipFileIntoDestinationFolderWhenDeflateSolidAes() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        UnzipSettings settings = UnzipSettings.builder().passwordProvider(fileNamePasswordProvider).build();

        UnzipIt.zip(zipDeflateSolid).settings(settings).dstDir(dstDir).extract(dirNameCars);
        assertThatDirectory(dstDir).matches(dirCarsAssert);
    }

    public void shouldCorrectlySetLastTimeStampWhenUnzip() throws IOException, ParseException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path file = dstDir.resolve("foo.txt");
        final String str = "2014.10.29T18:10:44";
        FileUtils.writeStringToFile(file.toFile(), "oleg.cherednik", Charsets.UTF_8);

        Files.setLastModifiedTime(file, FileTime.fromMillis(convert(str)));

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("src.zip");
        ZipIt.zip(zip).add(file);

        Path unzipDir = dstDir.resolve("unzip");
        UnzipIt.zip(zip).dstDir(unzipDir).extract();

        Path fileFooUnzip = unzipDir.resolve("foo.txt");
        assertThat(convert(Files.getLastModifiedTime(fileFooUnzip).toMillis())).isEqualTo(str);
    }

    public void shouldUnzipZipFileIntoDestinationFolderRemovingPrefixWhenExtractWithPrefix() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path zip = Zip4jvmSuite.getResourcePath("/zip/macos_10.zip");

        UnzipIt.zip(zip).dstDir(dstDir).extract("data");
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    private static long convert(String str) throws ParseException {
        return new SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss", Locale.ENGLISH).parse(str).getTime();
    }

    private static String convert(long time) {
        return DateTimeFormatter.ofPattern("yyyy.MM.dd'T'HH:mm:ss", Locale.ENGLISH)
                                .format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
    }

}
