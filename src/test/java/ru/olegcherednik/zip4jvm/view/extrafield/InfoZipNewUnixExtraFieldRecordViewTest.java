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
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipNewUnixExtraFieldRecord;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class InfoZipNewUnixExtraFieldRecordViewTest {

    private static final String SIZE_15_BYTES = "  - size:                                           15 bytes";
    private static final String TITLE = "(0x7875) new InfoZIP Unix/OS2/NT:   "
            + "                5296740 (0x0050D264) bytes";
    private static final String VERSION_ONE = "  version:                                          1";

    public void shouldRetrieveVersionOneRecordWhenVersionOne() {
        Block block = createBlock();

        InfoZipNewUnixExtraFieldRecord.Payload payload =
                InfoZipNewUnixExtraFieldRecord.VersionOnePayload.builder().uid("aaa").gid("bbb").build();
        InfoZipNewUnixExtraFieldRecord record =
                InfoZipNewUnixExtraFieldRecord.builder().dataSize(11).payload(payload).build();
        String[] lines = Zip4jvmSuite.execute(createView(record, 0, block));

        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_15_BYTES);
        assertThat(lines[2]).isEqualTo(VERSION_ONE);
        assertThat(lines[3]).isEqualTo("  User identifier (UID):                            aaa");
        assertThat(lines[4]).isEqualTo("  Group Identifier (GID):                           bbb");
    }

    public void shouldRetrieveUnknownVersionRecordWhenVersionNotOne() {
        Block block = createBlock();

        InfoZipNewUnixExtraFieldRecord.Payload payload =
                InfoZipNewUnixExtraFieldRecord.UnknownPayload.builder()
                                                             .version(2)
                                                             .data(new byte[] { 0x0, 0x1, 0x2, 0x3 })
                                                             .build();

        InfoZipNewUnixExtraFieldRecord record =
                InfoZipNewUnixExtraFieldRecord.builder().dataSize(11).payload(payload).build();

        String[] lines = Zip4jvmSuite.execute(createView(record, 0, block));

        assertThat(lines).hasSize(4);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_15_BYTES);
        assertThat(lines[2]).isEqualTo("  version:                                          2 (unknown)");
        assertThat(lines[3]).isEqualTo("00 01 02 03");
    }

    public void shouldRetrieveVersionOneRecordWithDiskWhenSplit() {
        Block block = createBlock();
        when(block.getDiskNo()).thenReturn(5);
        when(block.getFileName()).thenReturn("src.zip");

        InfoZipNewUnixExtraFieldRecord.Payload payload =
                InfoZipNewUnixExtraFieldRecord.VersionOnePayload.builder().uid("aaa").gid("bbb").build();
        InfoZipNewUnixExtraFieldRecord record =
                InfoZipNewUnixExtraFieldRecord.builder().dataSize(11).payload(payload).build();

        String[] lines = Zip4jvmSuite.execute(createView(record, 5, block));

        assertThat(lines).hasSize(6);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo("  - disk (0005):                                    src.zip");
        assertThat(lines[2]).isEqualTo(SIZE_15_BYTES);
        assertThat(lines[3]).isEqualTo(VERSION_ONE);
        assertThat(lines[4]).isEqualTo("  User identifier (UID):                            aaa");
        assertThat(lines[5]).isEqualTo("  Group Identifier (GID):                           bbb");
    }

    @Test(dataProvider = "idBlank")
    public void shouldNotPrintWhenUidBlank(String uid) {
        InfoZipNewUnixExtraFieldRecord record =
                InfoZipNewUnixExtraFieldRecord.builder()
                                              .payload(InfoZipNewUnixExtraFieldRecord.VersionOnePayload.builder()
                                                                                                       .uid(uid)
                                                                                                       .build())
                                              .build();
        String[] lines = Zip4jvmSuite.execute(createView(record, 0, createBlock()));

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_15_BYTES);
        assertThat(lines[2]).isEqualTo(VERSION_ONE);
    }

    @Test(dataProvider = "idBlank")
    public void shouldNotPrintWhenGidBlank(String gid) {
        InfoZipNewUnixExtraFieldRecord record =
                InfoZipNewUnixExtraFieldRecord.builder()
                                              .payload(InfoZipNewUnixExtraFieldRecord.VersionOnePayload.builder()
                                                                                                       .gid(gid)
                                                                                                       .build())
                                              .build();
        String[] lines = Zip4jvmSuite.execute(createView(record, 0, createBlock()));

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_15_BYTES);
        assertThat(lines[2]).isEqualTo(VERSION_ONE);
    }

    @DataProvider(name = "idBlank")
    public static Object[][] idData() {
        return new Object[][] {
                { null },
                { "" },
                { "    " }
        };
    }

    private static Block createBlock() {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(15L);
        when(block.getDiskOffs()).thenReturn(5296740L);
        return block;
    }

    private static InfoZipNewUnixExtraFieldRecordView createView(InfoZipNewUnixExtraFieldRecord record,
                                                                 long totalDisks,
                                                                 Block block) {
        return InfoZipNewUnixExtraFieldRecordView.builder()
                                                 .offs(0)
                                                 .columnWidth(52)
                                                 .totalDisks(totalDisks)
                                                 .record(record)
                                                 .block(block).build();
    }

}
