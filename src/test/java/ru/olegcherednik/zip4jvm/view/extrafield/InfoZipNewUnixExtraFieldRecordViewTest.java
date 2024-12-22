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

import org.testng.annotations.Test;

import java.io.IOException;
import java.io.PrintStream;

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

    public void shouldRetrieveVersionOneRecordWhenVersionOne() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(15L);
        when(block.getDiskOffs()).thenReturn(5296740L);

        InfoZipNewUnixExtraFieldRecord.Payload payload = InfoZipNewUnixExtraFieldRecord.VersionOnePayload.builder()
                                                                                                         .uid("aaa")
                                                                                                         .gid("bbb")
                                                                                                         .build();

        InfoZipNewUnixExtraFieldRecord record = InfoZipNewUnixExtraFieldRecord.builder()
                                                                              .dataSize(11)
                                                                              .payload(payload).build();

        String[] lines = Zip4jvmSuite.execute(InfoZipNewUnixExtraFieldRecordView.builder()
                                                                                .record(record)
                                                                                .block(block)
                                                                                .position(0, 52, 0).build());

        assertThat(lines).hasSize(5);
        assertThat(lines[0])
                .isEqualTo("(0x7875) new InfoZIP Unix/OS2/NT:                   5296740 (0x0050D264) bytes");
        assertThat(lines[1]).isEqualTo("  - size:                                           15 bytes");
        assertThat(lines[2]).isEqualTo("  version:                                          1");
        assertThat(lines[3]).isEqualTo("  User identifier (UID):                            aaa");
        assertThat(lines[4]).isEqualTo("  Group Identifier (GID):                           bbb");
    }

    public void shouldRetrieveUnknownVersionRecordWhenVersionNotOne() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(15L);
        when(block.getDiskOffs()).thenReturn(5296740L);

        InfoZipNewUnixExtraFieldRecord.Payload payload =
                InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload.builder()
                                                                    .version(2)
                                                                    .data(new byte[] { 0x0, 0x1, 0x2, 0x3 })
                                                                    .build();

        InfoZipNewUnixExtraFieldRecord record = InfoZipNewUnixExtraFieldRecord.builder()
                                                                              .dataSize(11)
                                                                              .payload(payload).build();

        String[] lines = Zip4jvmSuite.execute(InfoZipNewUnixExtraFieldRecordView.builder()
                                                                                .record(record)
                                                                                .block(block)
                                                                                .position(0, 52, 0).build());

        assertThat(lines).hasSize(4);
        assertThat(lines[0])
                .isEqualTo("(0x7875) new InfoZIP Unix/OS2/NT:                   5296740 (0x0050D264) bytes");
        assertThat(lines[1]).isEqualTo("  - size:                                           15 bytes");
        assertThat(lines[2]).isEqualTo("  version:                                          2 (unknown)");
        assertThat(lines[3]).isEqualTo("00 01 02 03");
    }

    public void shouldRetrieveEmptyStringWhenRecordNull() throws IOException {
        try (PrintStream out = mock(PrintStream.class)) {
            InfoZipNewUnixExtraFieldRecordView view =
                    InfoZipNewUnixExtraFieldRecordView.builder()
                                                      .record(InfoZipNewUnixExtraFieldRecord.NULL)
                                                      .block(mock(Block.class))
                                                      .position(0, 52, 0).build();
            assertThat(view.printTextInfo(out)).isFalse();
        }
    }

    public void shouldRetrieveVersionOneRecordWithDiskWhenSplit() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(15L);
        when(block.getDiskOffs()).thenReturn(5296740L);
        when(block.getDiskNo()).thenReturn(5);
        when(block.getFileName()).thenReturn("src.zip");

        InfoZipNewUnixExtraFieldRecord.Payload payload = InfoZipNewUnixExtraFieldRecord.VersionOnePayload.builder()
                                                                                                         .uid("aaa")
                                                                                                         .gid("bbb")
                                                                                                         .build();

        InfoZipNewUnixExtraFieldRecord record = InfoZipNewUnixExtraFieldRecord.builder()
                                                                              .dataSize(11)
                                                                              .payload(payload).build();

        String[] lines = Zip4jvmSuite.execute(InfoZipNewUnixExtraFieldRecordView.builder()
                                                                                .record(record)
                                                                                .block(block)
                                                                                .position(0, 52, 5).build());

        assertThat(lines).hasSize(6);
        assertThat(lines[0])
                .isEqualTo("(0x7875) new InfoZIP Unix/OS2/NT:                   5296740 (0x0050D264) bytes");
        assertThat(lines[1]).isEqualTo("  - disk (0005):                                    src.zip");
        assertThat(lines[2]).isEqualTo("  - size:                                           15 bytes");
        assertThat(lines[3]).isEqualTo("  version:                                          1");
        assertThat(lines[4]).isEqualTo("  User identifier (UID):                            aaa");
        assertThat(lines[5]).isEqualTo("  Group Identifier (GID):                           bbb");
    }

}
