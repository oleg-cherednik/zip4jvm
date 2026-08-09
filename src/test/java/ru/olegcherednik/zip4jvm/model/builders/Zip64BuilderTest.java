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

import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import org.testng.annotations.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 08.04.2023
 */
@Test
public class Zip64BuilderTest {

    public void shouldRetrieveNullObjectWhenNotZip64() {
        SrcZip srcZip = mock(SrcZip.class);
        ZipModel zipModel = new ZipModel(srcZip);
        assertThat(zipModel.isZip64()).isFalse();

        Zip64Builder builder = new Zip64Builder(zipModel, 1);
        assertThat(builder.build()).isSameAs(Zip64.NULL);
    }

    public void shouldRetrieveTwoEntryCountWhenDiskEntriesForNotSplitZip() {
        SrcZip srcZip = mock(SrcZip.class);
        ZipModel zipModel = new ZipModel(srcZip);
        zipModel.setZip64(true);

        ZipEntry one = mock(ZipEntry.class);
        ZipEntry two = mock(ZipEntry.class);

        when(one.getFileName()).thenReturn("one.txt");
        when(two.getFileName()).thenReturn("two.txt");

        zipModel.addZipEntry(one);
        zipModel.addZipEntry(two);

        assertThat(zipModel.getTotalEntries()).isEqualTo(2);

        Zip64 res = new Zip64Builder(zipModel, 0).build();
        assertThat(res.getEndCentralDirectoryLocator().getMainDiskNo()).isEqualTo(0);
        assertThat(res.getEndCentralDirectoryLocator().getTotalDisks()).isEqualTo(1);
        assertThat(res.getEndCentralDirectory().getDiskEntries()).isEqualTo(2);
        assertThat(res.getEndCentralDirectory().getTotalEntries()).isEqualTo(2);
    }

    public void shouldRetrieveOneEntryCountWhenDiskEntriesForSplitZip() {
        SrcZip srcZip = mock(SrcZip.class);
        ZipModel zipModel = new ZipModel(srcZip);
        zipModel.setZip64(true);
        zipModel.setTotalDisks(1);

        ZipEntry one = mock(ZipEntry.class);
        ZipEntry two = mock(ZipEntry.class);

        when(one.getFileName()).thenReturn("one.txt");
        when(two.getFileName()).thenReturn("two.txt");

        when(one.getDiskNo()).thenReturn(0);
        when(one.getDiskNo()).thenReturn(1);

        zipModel.addZipEntry(one);
        zipModel.addZipEntry(two);

        assertThat(zipModel.getTotalEntries()).isEqualTo(2);

        Zip64 res = new Zip64Builder(zipModel, 1).build();
        assertThat(res.getEndCentralDirectoryLocator().getMainDiskNo()).isEqualTo(1);
        assertThat(res.getEndCentralDirectoryLocator().getTotalDisks()).isEqualTo(2);
        assertThat(res.getEndCentralDirectory().getDiskEntries()).isEqualTo(1);
        assertThat(res.getEndCentralDirectory().getTotalEntries()).isEqualTo(2);
    }

}
