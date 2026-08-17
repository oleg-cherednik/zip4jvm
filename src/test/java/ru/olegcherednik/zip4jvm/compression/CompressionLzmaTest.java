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
package ru.olegcherednik.zip4jvm.compression;

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.ZipInfo;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
import ru.olegcherednik.zip4jvm.model.settings.CompressionLevelEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.testng.annotations.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.fileEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileNameEmpty;
import static ru.olegcherednik.zip4jvm.TestData.filesDirBikes;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 09.02.2020
 */
@Test
public class CompressionLzmaTest extends BaseTest {

    public void shouldCreateSingleZipWithFilesWhenLzmaCompressionNormalLevelEosMarker() {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(CompressionEnum.LZMA, CompressionLevelEnum.NORMAL)
                                                         .lzmaEosMarker(true).build();

        Path zip = getZip();

        ZipIt.zip(zip).settings(ZipSettings.of(entrySettings)).add(filesDirBikes);

        assertThatZipFile(zip)
                .isSolid()
                .root().matches(dirBikesAssert);
    }

    public void shouldCreateSingleZipWithFilesWhenLzmaCompressionNormalLevelEosNoMarker() {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(CompressionEnum.LZMA, CompressionLevelEnum.NORMAL)
                                                         .lzmaEosMarker(false).build();

        Path zip = getZip();

        ZipIt.zip(zip).settings(ZipSettings.of(entrySettings)).add(filesDirBikes);

        assertThatZipFile(zip)
                .isSolid()
                .root().matches(dirBikesAssert);
    }

    public void shouldCreateSingleZipWithFilesWhenLzmaCompressionSuperFastLevelEosMarker() {
        ZipEntrySettings entrySettings =
                ZipEntrySettings.builder()
                                .compression(CompressionEnum.LZMA, CompressionLevelEnum.SUPER_FAST)
                                .lzmaEosMarker(true).build();

        Path zip = getZip();

        ZipIt.zip(zip).settings(ZipSettings.of(entrySettings)).add(filesDirBikes);

        assertThatZipFile(zip)
                .isSolid()
                .root().matches(dirBikesAssert);
    }

    public void shouldCreateSingleZipWithFilesWhenLzmaCompressionSuperFastLevelNoEosMarker() {
        ZipEntrySettings entrySettings =
                ZipEntrySettings.builder()
                                .compression(CompressionEnum.LZMA, CompressionLevelEnum.SUPER_FAST)
                                .lzmaEosMarker(false).build();

        Path zip = getZip();

        ZipIt.zip(zip).settings(ZipSettings.of(entrySettings)).add(filesDirBikes);

        assertThatZipFile(zip)
                .isSolid()
                .root().matches(dirBikesAssert);
    }

    public void shouldUseCompressStoreWhenFileEmpty() {
        Path zip = getZip();
        ZipIt.zip(zip).settings(ZipSettings.of(CompressionEnum.LZMA)).add(fileEmpty);
        CentralDirectory.FileHeader fileHeader = ZipInfo.zip(zip).getFileHeader(fileNameEmpty);
        assertThat(fileHeader.getCompression()).isSameAs(Compression.STORE);
    }

}
