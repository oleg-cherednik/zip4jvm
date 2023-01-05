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

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AlignmentExtraFieldRecord;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 06.01.2023
 */
@Test
public class AlignmentExtraFieldRecordViewTest {

    public void shouldRetrieveAllDataWhenAllDataSet() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(36L);
        when(block.getRelativeOffs()).thenReturn(11208273272L);

        AlignmentExtraFieldRecord record = AlignmentExtraFieldRecord.builder()
                                                                    .dataSize(4)
                                                                    .data(new byte[] { 0x0, 0x1, 0x2, 0x3 })
                                                                    .build();

        String[] lines = Zip4jvmSuite.execute(AlignmentExtraFieldRecordView.builder()
                                                                           .record(record)
                                                                           .block(block)
                                                                           .data(new byte[] { 0x0, 0x1, 0x2, 0x3 })
                                                                           .position(0, 52, 0).build());

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo("(0xD935) Android Alignment Tag:                     37 (0x00000025) bytess");
        assertThat(lines[1]).isEqualTo("  - size:                                           8 bytes");
        assertThat(lines[2]).isEqualTo("00 01 02 03");
    }
//
//    public void shouldRetrieveAllDataWithDiskWhenSplit() throws IOException {
//        Block block = mock(Block.class);
//        when(block.getSize()).thenReturn(36L);
//        when(block.getRelativeOffs()).thenReturn(11208273272L);
//        when(block.getDiskNo()).thenReturn(5);
//        when(block.getFileName()).thenReturn("src.zip");
//
//        UnknownExtraFieldRecord record = new UnknownExtraFieldRecord(0x0666, ArrayUtils.EMPTY_BYTE_ARRAY);
//
//        String[] lines = Zip4jvmSuite.execute(UnknownExtraFieldRecordView.builder()
//                                                                         .record(record)
//                                                                         .block(block)
//                                                                         .data(new byte[] { 0x0, 0x1, 0x2, 0x3 })
//                                                                         .position(0, 52, 5).build());
//
//        assertThat(lines).hasSize(4);
//        assertThat(lines[0]).isEqualTo("(0x0666) Unknown:                                   11208273272 (0x29C10AD78) bytes");
//        assertThat(lines[1]).isEqualTo("  - disk (0005):                                    src.zip");
//        assertThat(lines[2]).isEqualTo("  - size:                                           36 bytes");
//        assertThat(lines[3]).isEqualTo("00 01 02 03");
//    }
}
