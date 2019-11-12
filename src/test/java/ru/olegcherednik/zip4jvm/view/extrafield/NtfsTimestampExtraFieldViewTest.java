package ru.olegcherednik.zip4jvm.view.extrafield;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.os.NtfsTimestampExtraField;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
@Test
public class NtfsTimestampExtraFieldViewTest {

    public void shouldRetrieveAllDataWhenAllDataSet() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(36L);
        when(block.getOffs()).thenReturn(11208273272L);

        NtfsTimestampExtraField.Tag tagOne = NtfsTimestampExtraField.OneTag.builder()
                                                                           .lastModificationTime(1522212737955L)
                                                                           .lastAccessTime(1571000455361L)
                                                                           .creationTime(1554845102859L).build();

        NtfsTimestampExtraField.Tag tagUnknown = NtfsTimestampExtraField.UnknownTag.builder()
                                                                                   .signature(0x0002)
                                                                                   .data(new byte[] { 0x0, 0x1, 0x2, 0x3 }).build();

        NtfsTimestampExtraField record = NtfsTimestampExtraField.builder()
                                                                .dataSize(32)
                                                                .tags(Arrays.asList(tagOne, tagUnknown)).build();

        String[] lines = Zip4jvmSuite.execute(NtfsTimestampExtraFieldView.builder()
                                                                         .record(record)
                                                                         .block(block)
                                                                         .columnWidth(52).build());

        assertThat(lines).hasSize(9);
        assertThat(lines[0]).isEqualTo("(0x000A) NTFS Timestamps:                           11208273272 (0x29C10AD78) bytes");
        assertThat(lines[1]).isEqualTo("  - size:                                           36 bytes");
        assertThat(lines[2]).isEqualTo("  - total tags:                                     2");
        assertThat(lines[3]).isEqualTo("  (0x0001) Tag1:                                    24 bytes");
        assertThat(lines[4]).isEqualTo("    Creation Date:                                  2019-04-10 00:25:02");
        assertThat(lines[5]).isEqualTo("    Last Modified Date:                             2018-03-28 07:52:17");
        assertThat(lines[6]).isEqualTo("    Last Accessed Date:                             2019-10-14 00:00:55");
        assertThat(lines[7]).isEqualTo("  (0x0002) Unknown Tag:                             4 bytes");
        assertThat(lines[8]).isEqualTo("00 01 02 03");
    }

    public void shouldRetrieveEmptyStringWhenRecordNull() throws IOException {
        String[] lines = Zip4jvmSuite.execute(NtfsTimestampExtraFieldView.builder()
                                                                         .record(NtfsTimestampExtraField.NULL)
                                                                         .block(mock(Block.class))
                                                                         .columnWidth(52).build());
        assertThat(lines).hasSize(1);
        assertThat(lines[0]).isEmpty();
    }
}
