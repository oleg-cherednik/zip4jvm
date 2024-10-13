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
import ru.olegcherednik.zip4jvm.model.extrafield.records.NtfsTimestampExtraFieldRecord;

import org.testng.annotations.Test;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
@Test
@SuppressWarnings({ "NewClassNamingConvention", "FieldNamingConvention" })
public class NtfsTimestampExtraFieldRecordViewTest {

    private static final long lastModifiedTime = 1571903182001L;
    private static final long lastAccessTime = 1571703185000L;
    private static final long creationTime = 1572903182000L;

    public void shouldRetrieveAllDataWhenAllDataSet() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(36L);
        when(block.getRelativeOffs()).thenReturn(11208273272L);

        NtfsTimestampExtraFieldRecord.Tag tagOne =
                NtfsTimestampExtraFieldRecord.OneTag.builder()
                                                    .lastModificationTime(lastModifiedTime)
                                                    .lastAccessTime(lastAccessTime)
                                                    .creationTime(creationTime)
                                                    .build();

        NtfsTimestampExtraFieldRecord.Tag tagUnknown =
                NtfsTimestampExtraFieldRecord.UnknownTag.builder()
                                                        .signature(0x0002)
                                                        .data(new byte[] { 0x0, 0x1, 0x2, 0x3 })
                                                        .build();

        NtfsTimestampExtraFieldRecord record =
                NtfsTimestampExtraFieldRecord.builder()
                                             .dataSize(32)
                                             .tags(Arrays.asList(tagOne, tagUnknown))
                                             .build();

        String[] lines = Zip4jvmSuite.execute(NtfsTimestampExtraFieldRecordView.builder()
                                                                               .record(record)
                                                                               .block(block)
                                                                               .position(0, 52, 0).build());

        assertThat(lines).hasSize(9);
        assertThat(lines[0]).isEqualTo(
                "(0x000A) NTFS Timestamp:                            11208273272 (0x29C10AD78) bytes");
        assertThat(lines[1]).isEqualTo("  - size:                                           36 bytes");
        assertThat(lines[2]).isEqualTo("  - total tags:                                     2");
        assertThat(lines[3]).isEqualTo("  (0x0001) Tag1:                                    24 bytes");
        assertThat(lines[4]).isEqualTo("    Creation Date:                                  2019-11-04 21:33:02");
        assertThat(lines[5]).isEqualTo("    Last Modified Date:                             2019-10-24 07:46:22");
        assertThat(lines[6]).isEqualTo("    Last Accessed Date:                             2019-10-22 00:13:05");
        assertThat(lines[7]).isEqualTo("  (0x0002) Unknown Tag:                             4 bytes");
        assertThat(lines[8]).isEqualTo("00 01 02 03");
    }

    public void shouldRetrieveEmptyStringWhenRecordNull() throws IOException {
        try (PrintStream out = mock(PrintStream.class)) {
            NtfsTimestampExtraFieldRecordView view =
                    NtfsTimestampExtraFieldRecordView.builder()
                                                     .record(NtfsTimestampExtraFieldRecord.NULL)
                                                     .block(mock(Block.class))
                                                     .position(0, 52, 0).build();
            assertThat(view.printTextInfo(out)).isFalse();
        }
    }

    public void shouldRetrieveAllDataWithDiskWhenSplit() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(36L);
        when(block.getRelativeOffs()).thenReturn(11208273272L);
        when(block.getDiskNo()).thenReturn(5);
        when(block.getFileName()).thenReturn("src.zip");

        NtfsTimestampExtraFieldRecord.Tag tagOne =
                NtfsTimestampExtraFieldRecord.OneTag.builder()
                                                    .lastModificationTime(lastModifiedTime)
                                                    .lastAccessTime(lastAccessTime)
                                                    .creationTime(creationTime)
                                                    .build();

        NtfsTimestampExtraFieldRecord.Tag tagUnknown =
                NtfsTimestampExtraFieldRecord.UnknownTag.builder()
                                                        .signature(0x0002)
                                                        .data(new byte[] { 0x0, 0x1, 0x2, 0x3 })
                                                        .build();

        NtfsTimestampExtraFieldRecord record = NtfsTimestampExtraFieldRecord.builder()
                                                                            .dataSize(32)
                                                                            .tags(Arrays.asList(tagOne, tagUnknown))
                                                                            .build();

        String[] lines = Zip4jvmSuite.execute(NtfsTimestampExtraFieldRecordView.builder()
                                                                               .record(record)
                                                                               .block(block)
                                                                               .position(0, 52, 5).build());

        assertThat(lines).hasSize(10);
        assertThat(lines[0]).isEqualTo(
                "(0x000A) NTFS Timestamp:                            11208273272 (0x29C10AD78) bytes");
        assertThat(lines[1]).isEqualTo("  - disk (0005):                                    src.zip");
        assertThat(lines[2]).isEqualTo("  - size:                                           36 bytes");
        assertThat(lines[3]).isEqualTo("  - total tags:                                     2");
        assertThat(lines[4]).isEqualTo("  (0x0001) Tag1:                                    24 bytes");
        assertThat(lines[5]).isEqualTo("    Creation Date:                                  2019-11-04 21:33:02");
        assertThat(lines[6]).isEqualTo("    Last Modified Date:                             2019-10-24 07:46:22");
        assertThat(lines[7]).isEqualTo("    Last Accessed Date:                             2019-10-22 00:13:05");
        assertThat(lines[8]).isEqualTo("  (0x0002) Unknown Tag:                             4 bytes");
        assertThat(lines[9]).isEqualTo("00 01 02 03");
    }
}

