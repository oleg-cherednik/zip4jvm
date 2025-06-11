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
package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
@Test
@SuppressWarnings({ "NewClassNamingConvention", "FieldNamingConvention" })
public class Zip64ExtendedInfoViewTest {

    private static final long compressedSize = 11208273150L;
    private static final long uncompressedSize = 11322883953L;

    private static final String SIZE_12_BYTES = "  - size:                                           12 bytes";
    private static final String TITLE = "(0x0001) Zip64 Extended Information:  "
            + "              5300395 (0x0050E0AB) bytes";

    public void shouldRetrieveAllDataWhenAllDataSet() throws IOException {
        Block block = createBlock();

        Zip64.ExtendedInfo record = createRecord();
        String[] lines = Zip4jvmSuite.execute(createView(record, 0, block));

        assertThat(lines).hasSize(6);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_12_BYTES);
        assertThat(lines[2]).isEqualTo("  original compressed size:                         11208273150 bytes");
        assertThat(lines[3]).isEqualTo("  original uncompressed size:                       11322883953 bytes");
        assertThat(lines[4]).isEqualTo("  original relative offset of local header:         145 (0x00000091) bytes");
        assertThat(lines[5]).isEqualTo("  original part number of this part (0002):         2");
    }

    public void shouldRetrieveAllDataWithDiskWhenSplit() throws IOException {
        Block block = createBlock();
        when(block.getDiskNo()).thenReturn(5);
        when(block.getFileName()).thenReturn("src.zip");

        Zip64.ExtendedInfo record = createRecord();
        String[] lines = Zip4jvmSuite.execute(createView(record, 5, block));

        assertThat(lines).hasSize(7);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo("  - disk (0005):                                    src.zip");
        assertThat(lines[2]).isEqualTo(SIZE_12_BYTES);
        assertThat(lines[3]).isEqualTo("  original compressed size:                         11208273150 bytes");
        assertThat(lines[4]).isEqualTo("  original uncompressed size:                       11322883953 bytes");
        assertThat(lines[5]).isEqualTo("  original relative offset of local header:         145 (0x00000091) bytes");
        assertThat(lines[6]).isEqualTo("  original part number of this part (0002):         2");
    }

    public void shouldNotPrintDiskNoOnlyWhenDiskNoOnly() {
        Zip64.ExtendedInfo record = Zip64.ExtendedInfo.builder()
                                                      .diskNo(2)
                                                      .localFileHeaderRelativeOffs(PkwareExtraField.NO_DATA)
                                                      .compressedSize(PkwareExtraField.NO_DATA)
                                                      .uncompressedSize(PkwareExtraField.NO_DATA)
                                                      .build();
        String[] lines = Zip4jvmSuite.execute(createView(record, 0, createBlock()));

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_12_BYTES);
        assertThat(lines[2]).isEqualTo("  original part number of this part (0002):         2");
    }

    public void shouldPrintCompressedSizeOnlyWhenCompressedSizeOnly() {
        Zip64.ExtendedInfo record = Zip64.ExtendedInfo.builder()
                                                      .diskNo(PkwareExtraField.NO_DATA)
                                                      .localFileHeaderRelativeOffs(PkwareExtraField.NO_DATA)
                                                      .compressedSize(compressedSize)
                                                      .uncompressedSize(PkwareExtraField.NO_DATA)
                                                      .build();
        String[] lines = Zip4jvmSuite.execute(createView(record, 0, createBlock()));

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_12_BYTES);
        assertThat(lines[2]).isEqualTo("  original compressed size:                         11208273150 bytes");
    }

    @Test(dataProvider = "nullRecord")
    public void shouldThrowIllegalArgumentExceptionWhenRecordNull(Zip64.ExtendedInfo record) {
        assertThatThrownBy(() -> createView(record, 0, createBlock()))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @DataProvider(name = "nullRecord")
    public static Object[][] lastModificationTimeData() {
        return new Object[][] {
                { null },
                { Zip64.ExtendedInfo.NULL }
        };
    }

    private static Block createBlock() {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(12L);
        when(block.getDiskOffs()).thenReturn(5300395L);
        return block;
    }

    private static Zip64.ExtendedInfo createRecord() {
        return Zip64.ExtendedInfo.builder()
                                 .diskNo(2)
                                 .localFileHeaderRelativeOffs(145)
                                 .compressedSize(compressedSize)
                                 .uncompressedSize(uncompressedSize)
                                 .build();
    }

    private static Zip64ExtendedInfoView createView(Zip64.ExtendedInfo record,
                                                    long totalDisks,
                                                    Block block) {
        return Zip64ExtendedInfoView.builder()
                                    .offs(0)
                                    .columnWidth(52)
                                    .totalDisks(totalDisks)
                                    .record(record)
                                    .block(block).build();
    }

}
