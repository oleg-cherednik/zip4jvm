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
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.testng.annotations.Test;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.contentDirSrc;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 26.07.2026
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class CompressionDeflate64Test extends BaseTest {

    public void shouldCreateSingleZipWhenDeflate64Compression() {
        ZipEntrySettings entrySettings = ZipEntrySettings.of(CompressionEnum.DEFLATE_64);
        ZipSettings settings = ZipSettings.builder().entrySettings(entrySettings).build();

        Path zip = getZip();

        ZipIt.zip(zip).settings(settings).add(contentDirSrc);

        assertThatZipFile(zip, password)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().matches(rootAssert);
    }

    public void shouldCreateSplitZipWhenDeflate64Compression() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(CompressionEnum.DEFLATE_64)
                                          .splitSize(SIZE_1MB).build();

        Path zip = getZip();

        ZipIt.zip(zip).settings(settings).add(contentDirSrc);

        assertThatZipFile(zip)
                .withParent(dir -> dir.hasOnlyRegularFiles(6))
                .root().matches(rootAssert);
    }

}
