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
package ru.olegcherednik.zip4jvm.model.builders;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;

/**
 * @author Oleg Cherednik
 * @since 29.09.2019
 */
@Test
public class FileHeaderBuilderTest {

    public void shouldCreateFileHeaderWhenZip64Entry() {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().zip64(true).utf8(true).build();
        ZipEntry zipEntry = ZipEntryBuilder.regularFile(fileDucati, fileNameDucati, entrySettings);

        CentralDirectory.FileHeader fileHeader = new FileHeaderBuilder(zipEntry).build();
        assertThat(fileHeader).isNotNull();

        assertThat(fileHeader.getCompressedSize()).isEqualTo(ZipModel.LOOK_IN_EXTRA_FIELD);
        assertThat(fileHeader.getUncompressedSize()).isEqualTo(ZipModel.LOOK_IN_EXTRA_FIELD);
        assertThat(fileHeader.getDiskNo()).isEqualTo(ZipModel.MAX_TOTAL_DISKS);

        Zip64.ExtendedInfo extendedInfo = fileHeader.getExtraField().getExtendedInfo();
        assertThat(extendedInfo).isNotSameAs(Zip64.ExtendedInfo.NULL);
        assertThat(extendedInfo.getUncompressedSize()).isEqualTo(PathUtils.size(fileDucati));
        assertThat(extendedInfo.getCompressedSize()).isEqualTo(0);
        assertThat(extendedInfo.getDiskNo()).isEqualTo(0);
    }
}
