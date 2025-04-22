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
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipUnicodePathExtraFieldRecord;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 22.04.2025
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class InfoZipUnicodePathExtraFieldRecordViewTest {

    private static final String SIZE_15_BYTES = "  - size:                                           15 bytes";
    private static final String TITLE = "(0x7075) InfoZIP Unicode Path:       "
            + "               5296740 (0x0050D264) bytes";
    private static final String VERSION_ONE = "  version:                                          1";

    public void shouldRetrieveVersionOneRecordWhenVersionOne() {
        Block block = createBlock();

        InfoZipUnicodePathExtraFieldRecord.Payload payload =
                InfoZipUnicodePathExtraFieldRecord.VersionOnePayload.builder()
                                                                    .crc32(0xAABBCC)
                                                                    .name("Олег Чередник").build();

        InfoZipUnicodePathExtraFieldRecord record =
                InfoZipUnicodePathExtraFieldRecord.builder().dataSize(11).payload(payload).build();
        String[] lines = Zip4jvmSuite.execute(createView(record, 0, block));

        assertThat(lines).hasSize(7);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_15_BYTES);
        assertThat(lines[2]).isEqualTo(VERSION_ONE);
        assertThat(lines[3]).isEqualTo("  NameCRC32:                                        0x00AABBCC");
        assertThat(lines[4]).isEqualTo("                                                    UTF-8");
        assertThat(lines[5]).isEqualTo("D0 9E D0 BB D0 B5 D0 B3 20 D0 A7 D0 B5 D1 80 D0 B5  Олег Чере");
        assertThat(lines[6]).isEqualTo("D0 B4 D0 BD D0 B8 D0 BA                             дник");
    }

    public void shouldRetrieveUnknownVersionRecordWhenVersionNotOne() {
        Block block = createBlock();

        InfoZipUnicodePathExtraFieldRecord.Payload payload =
                InfoZipUnicodePathExtraFieldRecord.UnknownPayload.builder()
                                                                 .version(2)
                                                                 .data(new byte[] { 0x0, 0x1, 0x2, 0x3 })
                                                                 .build();

        InfoZipUnicodePathExtraFieldRecord record =
                InfoZipUnicodePathExtraFieldRecord.builder().dataSize(11).payload(payload).build();

        String[] lines = Zip4jvmSuite.execute(createView(record, 0, block));

        assertThat(lines).hasSize(4);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_15_BYTES);
        assertThat(lines[2]).isEqualTo("  version:                                          2 (unknown)");
        assertThat(lines[3]).isEqualTo("00 01 02 03");
    }

    private static Block createBlock() {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(15L);
        when(block.getDiskOffs()).thenReturn(5296740L);
        return block;
    }

    private static InfoZipUnicodePathExtraFieldRecordView createView(InfoZipUnicodePathExtraFieldRecord record,
                                                                     long totalDisks,
                                                                     Block block) {
        return InfoZipUnicodePathExtraFieldRecordView.builder()
                                                     .offs(0)
                                                     .columnWidth(52)
                                                     .totalDisks(totalDisks)
                                                     .record(record)
                                                     .block(block).build();
    }

}
