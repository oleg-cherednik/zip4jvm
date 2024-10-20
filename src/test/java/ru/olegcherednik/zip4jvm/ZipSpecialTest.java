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

import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettingsProvider;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 20.10.2024
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipSpecialTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipSpecialTest.class);

    public void shouldAddRegularFileWhenSameNameAndDifferentDestPath() throws IOException {
        final char[] one = "1".toCharArray();
        final char[] two = "2".toCharArray();

        final String oneEntryName = "one/" + fileNameBentley;
        final String twoEntryName = "two/" + fileNameBentley;
        final String threeEntryName = "three/" + fileNameBentley;

        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(ZipEntrySettingsProvider.of(entryName -> {
                                              if (entryName.equals(oneEntryName))
                                                  return ZipEntrySettings.of(Encryption.AES_256, one);
                                              if (entryName.equals(twoEntryName))
                                                  return ZipEntrySettings.of(Encryption.AES_256, two);
                                              return null;
                                          })).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        try (ZipFile.Writer zipFile = ZipFile.writer(zip, settings)) {
            zipFile.addWithMove(fileBentley, "one");
            zipFile.addWithMove(fileBentley, "two");
            zipFile.addWithMove(fileBentley, "three");
        }

        assertThatDirectory(zip.getParent()).exists().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).root().hasEntries(3).hasDirectories(3).hasRegularFiles(0);
        assertThatZipFile(zip, one).regularFile(oneEntryName).matches(fileBentleyAssert);
        assertThatZipFile(zip, two).regularFile(twoEntryName).matches(fileBentleyAssert);
        assertThatZipFile(zip).regularFile(threeEntryName).matches(fileBentleyAssert);
    }

    public void shouldAddDirectoryWhenSameNameAndDifferentDestPath() throws IOException {
        final char[] one = "1".toCharArray();
        final char[] two = "2".toCharArray();

        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(ZipEntrySettingsProvider.of(entryName -> {
                                              if (entryName.startsWith("one/"))
                                                  return ZipEntrySettings.of(Encryption.AES_256, one);
                                              if (entryName.startsWith("two/"))
                                                  return ZipEntrySettings.of(Encryption.AES_256, two);
                                              return null;
                                          }))
                                          .removeRootDir(true)
                                          .build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        try (ZipFile.Writer zipFile = ZipFile.writer(zip, settings)) {
            zipFile.addWithMove(dirCars, "one");
            zipFile.addWithMove(dirCars, "two");
            zipFile.addWithMove(dirCars, "three");
        }

        assertThatDirectory(zip.getParent()).exists().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).root().hasEntries(3).hasDirectories(3).hasRegularFiles(0);
        assertThatZipFile(zip, one).directory("one").matches(dirCarsAssert);
        assertThatZipFile(zip, two).directory("two").matches(dirCarsAssert);
        assertThatZipFile(zip).directory("three").matches(dirCarsAssert);
    }

}
