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
package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.exception.SplitTriggerNotFoundException;
import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameWiesmann;
import static ru.olegcherednik.zip4jvm.TestData.fileWiesmann;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileFerrariAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileWiesmannAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 27.04.2019
 */
@Test
public class ZipFilesSplitTest extends BaseTest {

    public void shouldCreateNewSplitZipWithFiles() {
        Path zip = getZip();
        ZipSettings settings = ZipSettings.builder().splitSize(SIZE_1MB).build();

        ZipIt.zip(zip).settings(settings).add(fileBentley, fileFerrari, fileWiesmann);

        assertThatZipFile(zip)
                .withParent(dir -> dir.hasOnlyRegularFiles(3))
                .root().hasOnlyRegularFiles(3)
                .withRegularFile(fileNameBentley, fileBentleyAssert)
                .withRegularFile(fileNameFerrari, fileFerrariAssert)
                .withRegularFile(fileNameWiesmann, fileWiesmannAssert);
    }

    public void shouldSetTotalDiskWhenSplitZip64() {
        Path zip = getZip();
        ZipSettings settings = ZipSettings.builder()
                                          .zip64(true)
                                          .splitSize(SIZE_1MB).build();
        List<Path> files = Arrays.asList(fileBentley, fileFerrari, fileWiesmann);
        ZipIt.zip(zip).settings(settings).add(files);

        SrcZip srcZip = SrcZip.of(zip);
        ZipModelReader reader = new ZipModelReader(srcZip);
        reader.readCentralData();

        EndCentralDirectory endCentralDirectory = reader.getEndCentralDirectory();
        Zip64.EndCentralDirectoryLocator directoryLocator = reader.getZip64().getEndCentralDirectoryLocator();

        assertThat(endCentralDirectory.getMainDiskNo()).isEqualTo(ZipModel.MAX_TOTAL_DISKS);
        assertThat(endCentralDirectory.getTotalDisks()).isEqualTo(ZipModel.MAX_TOTAL_DISKS);
        assertThat(directoryLocator.getMainDiskNo()).isEqualTo(2);
        assertThat(directoryLocator.getTotalDisks()).isEqualTo(3);

        assertThat(ZipModelReader.getTotalDisks(srcZip)).isEqualTo(3);
    }

    public void shouldSetTotalDiskWhenSplit() {
        Path zip = getZip();
        ZipSettings settings = ZipSettings.builder().splitSize(SIZE_1MB).build();

        ZipIt.zip(zip).settings(settings).add(fileBentley, fileFerrari, fileWiesmann);

        SrcZip srcZip = SrcZip.of(zip);
        ZipModelReader reader = new ZipModelReader(srcZip);
        reader.readCentralData();

        EndCentralDirectory endCentralDirectory = reader.getEndCentralDirectory();

        assertThat(endCentralDirectory.getMainDiskNo()).isEqualTo(2);
        assertThat(endCentralDirectory.getTotalDisks()).isEqualTo(2);
        assertThat(reader.getZip64()).isSameAs(Zip64.NULL);

        assertThat(ZipModelReader.getTotalDisks(srcZip)).isEqualTo(3);
    }

    public void shouldThrowExceptionWhenAddSolidItemsToSplitZip() {
        ZipSettings settings = ZipSettings.builder().splitSize(SIZE_1MB).build();

        Path zip = getZip();
        ZipIt.zip(zip).settings(settings)
             .add(Arrays.asList(fileBentley, fileFerrari, fileWiesmann));


        assertThatThrownBy(() -> ZipIt.zip(zip).add(fileDucati))
                .isExactlyInstanceOf(SplitTriggerNotFoundException.class);
    }

}
