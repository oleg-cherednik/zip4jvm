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
package ru.olegcherednik.zip4jvm.model.extrafield.records;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipInfo;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatFile;

/**
 * @author Oleg Cherednik
 * @since 16.04.2025
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class InfoZipUnicodePathExtraFieldRecordTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(InfoZipUnicodePathExtraFieldRecordTest.class);

    public void shouldPrintShortInfoWhenInfoZipUnicodePathExist() throws FileNotFoundException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("actual.txt");

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(Zip4jvmSuite.getResourcePath("zip/info_zip_unicode_ef.zip")).printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/info_zip_unicode_ef.txt");
    }

    @Test(dataProvider = "crc32")
    public void shouldCheckChecksumWhenReadExtraField(String fileName, boolean checksumCorrect)
            throws FileNotFoundException {
        Path zip = Zip4jvmSuite.getResourcePath(fileName);
        CentralDirectory.FileHeader fileHeader = ZipInfo.zip(zip).getFileHeader("aaa/bbb/Oleg Cherednik.txt");
        InfoZipUnicodePathExtraFieldRecord extraFieldRecord =
                (InfoZipUnicodePathExtraFieldRecord) fileHeader.getExtraField()
                                                               .getRecord(InfoZipUnicodePathExtraFieldRecord.SIGNATURE);
        InfoZipUnicodePathExtraFieldRecord.VersionOnePayload payload = extraFieldRecord.getPayload();

        assertThat(payload.isChecksumCorrect()).isEqualTo(checksumCorrect);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    @DataProvider(name = "crc32")
    public static Object[][] crc32() {
        return new Object[][] {
                { "zip/info_zip_unicode_ef.zip", true },
                { "zip/info_zip_unicode_ef_checksum_not_valid.zip", false } };
    }

    public void shouldCreateRecordWhenAllDataValid() {
        InfoZipUnicodePathExtraFieldRecord record = createRecord();

        assertThat(record).isNotNull();
        assertThat(record).isNotSameAs(InfoZipUnicodePathExtraFieldRecord.NULL);
        assertThat(record.getDataSize()).isEqualTo(46);
        assertThat(record.getPayload() instanceof InfoZipUnicodePathExtraFieldRecord.VersionOnePayload).isTrue();

        InfoZipUnicodePathExtraFieldRecord.VersionOnePayload actualPayload = record.getPayload();
        assertThat(actualPayload.getCrc32()).isEqualTo(0xAEE33AF8L);
        assertThat(actualPayload.getName()).isEqualTo("aaa/bbb/Олег Чередник.txt");
        assertThat(actualPayload.isChecksumCorrect()).isTrue();
    }

    public void shouldRetrieveNullStringWhenToStringForNullObject() {
        InfoZipUnicodePathExtraFieldRecord record = createRecord();

        assertThat(record.toString()).isNotEqualTo("<null>");
        assertThat(InfoZipUnicodePathExtraFieldRecord.NULL.toString()).isEqualTo("<null>");
    }

    public void shouldRetrieve0WhenGetBlockSizeForNullObject() {
        InfoZipUnicodePathExtraFieldRecord record = createRecord();

        assertThat(record).isNotSameAs(InfoZipUnicodePathExtraFieldRecord.NULL);
        assertThat(record.getBlockSize()).isEqualTo(46 + InfoZipUnicodePathExtraFieldRecord.SIZE_FIELD);
        assertThat(InfoZipUnicodePathExtraFieldRecord.NULL.getBlockSize()).isEqualTo(0);
    }

    private static InfoZipUnicodePathExtraFieldRecord createRecord() {
        InfoZipUnicodePathExtraFieldRecord.VersionOnePayload payload =
                InfoZipUnicodePathExtraFieldRecord.VersionOnePayload.builder()
                                                                    .crc32(0xAEE33AF8L)
                                                                    .name("aaa/bbb/Олег Чередник.txt")
                                                                    .checksumCorrect(true)
                                                                    .build();
        return InfoZipUnicodePathExtraFieldRecord.builder()
                                                 .dataSize(46)
                                                 .payload(payload)
                                                 .build();
    }

}
