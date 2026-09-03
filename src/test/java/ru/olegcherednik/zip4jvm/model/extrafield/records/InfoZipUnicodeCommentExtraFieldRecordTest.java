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
package ru.olegcherednik.zip4jvm.model.extrafield.records;

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipInfo;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatFile;

/**
 * @author Oleg Cherednik
 * @since 20.04.2025
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class InfoZipUnicodeCommentExtraFieldRecordTest extends BaseTest {

    public void shouldPrintShortInfoWhenInfoZipUnicodePathExist() throws IOException {
        Path file = getTestRoot().resolve("actual.txt");

        try (PrintStream out = new PrintStream(file.toFile(), StandardCharsets.UTF_8)) {
            ZipInfo.zip(Zip4jvmSuite.getResourcePath("zip/extrafield/info_zip_unicode_comment.zip"))
                   .printShortInfo(out);
        }

        assertThatFile(file).matchesTextLines("/info/extrafield/info_zip_unicode_comment.txt");
    }

    @Test(dataProvider = "crc32")
    public void shouldCheckChecksumWhenReadExtraField(String fileName, boolean checksumCorrect)
            throws FileNotFoundException {
        Path zip = Zip4jvmSuite.getResourcePath(fileName);
        CentralDirectory.FileHeader fileHeader = ZipInfo.zip(zip).getFileHeader("Oleg Cherednik.txt");
        InfoZipUnicodePathExtraFieldRecord extraFieldRecord =
                (InfoZipUnicodePathExtraFieldRecord) fileHeader.getExtraField()
                                                               .getRecord(InfoZipUnicodePathExtraFieldRecord.SIGNATURE);
        InfoZipUnicodePathExtraFieldRecord.VersionOnePayload payload =
                (InfoZipUnicodePathExtraFieldRecord.VersionOnePayload) extraFieldRecord.getPayload();

        assertThat(payload.isChecksumCorrect()).isEqualTo(checksumCorrect);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    @DataProvider(name = "crc32")
    public static Object[][] crc32() {
        return new Object[][] {
                { "zip/extrafield/info_zip_unicode_comment.zip", true },
                { "zip/extrafield/info_zip_unicode_comment_checksum.zip", true }
        };
    }

    public void shouldCreateRecordWhenAllDataValid() {
        InfoZipUnicodePathExtraFieldRecord record = createRecord();

        assertThat(record).isNotNull();
        assertThat(record).isNotSameAs(InfoZipUnicodePathExtraFieldRecord.NULL);
        assertThat(record.getDataSize()).isEqualTo(46);
        assertThat(record.getPayload() instanceof InfoZipUnicodePathExtraFieldRecord.VersionOnePayload).isTrue();

        InfoZipUnicodePathExtraFieldRecord.VersionOnePayload actualPayload =
                (InfoZipUnicodePathExtraFieldRecord.VersionOnePayload) record.getPayload();
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
