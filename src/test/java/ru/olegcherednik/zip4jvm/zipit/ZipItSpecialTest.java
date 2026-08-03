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
package ru.olegcherednik.zip4jvm.zipit;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.charset.Charsets;
import ru.olegcherednik.zip4jvm.model.settings.EncryptionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettingsProvider;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import org.testng.annotations.Test;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileDucatiAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 20.10.2024
 */
@Test
public class ZipItSpecialTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    public void shouldAddRegularFileWhenSameNameAndDifferentDstPath() {
        final char[] one = "1".toCharArray();
        final char[] two = "2".toCharArray();

        final String oneEntryName = "one/" + fileNameBentley;
        final String twoEntryName = "two/" + fileNameBentley;
        final String threeEntryName = "three/" + fileNameBentley;

        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(ZipEntrySettingsProvider.of(entryName -> {
                                              if (entryName.equals(oneEntryName))
                                                  return ZipEntrySettings.of(EncryptionEnum.AES_256, one);
                                              if (entryName.equals(twoEntryName))
                                                  return ZipEntrySettings.of(EncryptionEnum.AES_256, two);
                                              return null;
                                          })).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);

        ZipIt.zip(zip).settings(settings).execute(zipFile -> {
            zipFile.add(fileBentley, oneEntryName);
            zipFile.add(fileBentley, twoEntryName);
            zipFile.add(fileBentley, threeEntryName);
        });

        assertThatZipFile(zip).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).root().hasOnlyDirectories(3);
        assertThatZipFile(zip, one).regularFile(oneEntryName).matches(fileBentleyAssert);
        assertThatZipFile(zip, two).regularFile(twoEntryName).matches(fileBentleyAssert);
        assertThatZipFile(zip).regularFile(threeEntryName).matches(fileBentleyAssert);
    }

    public void shouldAddDirectoryWhenSameNameAndDifferentDestPath() {
        final char[] one = "1".toCharArray();
        final char[] two = "2".toCharArray();

        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(ZipEntrySettingsProvider.of(entryName -> {
                                              if (entryName.startsWith("one/"))
                                                  return ZipEntrySettings.of(EncryptionEnum.AES_256, one);
                                              if (entryName.startsWith("two/"))
                                                  return ZipEntrySettings.of(EncryptionEnum.AES_256, two);
                                              return null;
                                          }))
                                          .build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);

        ZipIt.zip(zip).settings(settings).execute(zipFile -> {
            zipFile.add(dirCars, "one");
            zipFile.add(dirCars, "two");
            zipFile.add(dirCars, "three");
            zipFile.add(dirCars);
        });

        assertThatZipFile(zip).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).root().hasOnlyDirectories(4);
        assertThatZipFile(zip, one).directory(dirNameCars).matches(dirCarsAssert);
        assertThatZipFile(zip, one).directory("one").matches(dirCarsAssert);
        assertThatZipFile(zip, two).directory("two").matches(dirCarsAssert);
        assertThatZipFile(zip).directory("three").matches(dirCarsAssert);
    }

    public void shouldAddContentWhenInputStream() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        String fileName = fileDucati.getFileName().toString();

        ZipIt.zip(zip).execute(zipFile -> zipFile.add(PathUtils.newInputStreamSupplier(fileDucati), fileName));
        assertThatZipFile(zip).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).root().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).regularFile(fileName).matches(fileDucatiAssert);
    }

    public void shouldAddContentWhenByteArray() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        String content = "byte array content";
        String fileName = "byte_array.txt";

        ZipIt.zip(zip).execute(zipFile -> zipFile.add(content.getBytes(Charsets.UTF_8), fileName));
        assertThatZipFile(zip).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).root().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).regularFile(fileName).hasContent(content);
    }

    public void shouldAddContentWhenString() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);
        String content = "string content";
        String fileName = "string.txt";

        ZipIt.zip(zip).execute(zipFile -> zipFile.add(content, fileName));
        assertThatZipFile(zip).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).root().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).regularFile(fileName).hasContent(content);
    }

    public void shouldAddContentWhenMultiple() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(fileNameZipSrc);

        String byteArrayContent = "byte array content";
        String byteArrayFileName = "byte_array.txt";

        String strContent = "string content";
        String strFileName = "string.txt";

        ZipIt.zip(zip).execute(zipFile -> {
            zipFile.add(PathUtils.newInputStreamSupplier(fileDucati), fileNameDucati);
            zipFile.add(byteArrayContent.getBytes(Charsets.UTF_8), byteArrayFileName);
            zipFile.add(strContent, strFileName);
        });

        assertThatZipFile(zip).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).root().hasOnlyRegularFiles(3);
        assertThatZipFile(zip).regularFile(fileNameDucati).matches(fileDucatiAssert);
        assertThatZipFile(zip).regularFile(byteArrayFileName).hasContent(byteArrayContent);
        assertThatZipFile(zip).regularFile(strFileName).hasContent(strContent);
    }

}
