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
package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipUnicodeCommentExtraFieldRecord;

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
public class InfoZipUnicodeCommentExtraFieldRecordViewTest {

    private static final String SIZE_15_BYTES = "  - size:                                           15 bytes";
    private static final String TITLE = "(0x6375) InfoZIP Unicode Comment:      "
            + "             5296740 (0x0050D264) bytes";
    private static final String VERSION_ONE = "  version:                                          1";

    public void shouldRetrieveVersionOneRecordWhenVersionOne() {
        Block block = createBlock();

        InfoZipUnicodeCommentExtraFieldRecord.Payload payload =
                InfoZipUnicodeCommentExtraFieldRecord.VersionOnePayload.builder()
                                                                       .crc32(0xAABBCC)
                                                                       .comment("линия_1\nлиния_2\nлиния_3").build();

        InfoZipUnicodeCommentExtraFieldRecord record =
                InfoZipUnicodeCommentExtraFieldRecord.builder().dataSize(11).payload(payload).build();
        String[] lines = Zip4jvmSuite.execute(createView(record, 0, block));

        assertThat(lines).hasSize(8);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_15_BYTES);
        assertThat(lines[2]).isEqualTo(VERSION_ONE);
        assertThat(lines[3]).isEqualTo("  ComCRC32:                                         0x00AABBCC");
        assertThat(lines[4]).isEqualTo("                                                    UTF-8");
        assertThat(lines[5]).isEqualTo("D0 BB D0 B8 D0 BD D0 B8 D1 8F 5F 31 0A D0 BB D0 B8  линия_1.ли");
        assertThat(lines[6]).isEqualTo("D0 BD D0 B8 D1 8F 5F 32 0A D0 BB D0 B8 D0 BD D0 B8  ния_2.лини");
        assertThat(lines[7]).isEqualTo("D1 8F 5F 33                                         я_3");
    }

    public void shouldRetrieveUnknownVersionRecordWhenVersionNotOne() {
        Block block = createBlock();

        InfoZipUnicodeCommentExtraFieldRecord.Payload payload =
                InfoZipUnicodeCommentExtraFieldRecord.UnknownPayload.builder()
                                                                    .version(2)
                                                                    .data(new byte[] { 0x0, 0x1, 0x2, 0x3 })
                                                                    .build();

        InfoZipUnicodeCommentExtraFieldRecord record =
                InfoZipUnicodeCommentExtraFieldRecord.builder().dataSize(11).payload(payload).build();

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

    private static InfoZipUnicodeCommentExtraFieldRecordView createView(InfoZipUnicodeCommentExtraFieldRecord record,
                                                                        long totalDisks,
                                                                        Block block) {
        return InfoZipUnicodeCommentExtraFieldRecordView.builder()
                                                        .offs(0)
                                                        .columnWidth(52)
                                                        .totalDisks(totalDisks)
                                                        .record(record)
                                                        .block(block).build();
    }

}
