package ru.olegcherednik.zip4jvm.view.extrafield;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
@Test
public class UnknownExtraFieldRecordViewTest {

    public void shouldRetrieveAllDataWhenAllDataSet() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(36L);
        when(block.getRelativeOffs()).thenReturn(11208273272L);

        ExtraField.Record.Unknown record = ExtraField.Record.Unknown.builder().signature(0x0666).build();

        String[] lines = Zip4jvmSuite.execute(UnknownExtraFieldRecordView.builder()
                                                                         .record(record)
                                                                         .block(block)
                                                                         .data(new byte[] { 0x0, 0x1, 0x2, 0x3 })
                                                                         .position(0, 52, 0).build());

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo("(0x0666) Unknown:                                   11208273272 (0x29C10AD78) bytes");
        assertThat(lines[1]).isEqualTo("  - size:                                           36 bytes");
        assertThat(lines[2]).isEqualTo("00 01 02 03");
    }

    public void shouldRetrieveAllDataWithDiskWhenSplit() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(36L);
        when(block.getRelativeOffs()).thenReturn(11208273272L);
        when(block.getDiskNo()).thenReturn(5);
        when(block.getFileName()).thenReturn("src.zip");

        ExtraField.Record.Unknown record = ExtraField.Record.Unknown.builder().signature(0x0666).build();

        String[] lines = Zip4jvmSuite.execute(UnknownExtraFieldRecordView.builder()
                                                                         .record(record)
                                                                         .block(block)
                                                                         .data(new byte[] { 0x0, 0x1, 0x2, 0x3 })
                                                                         .position(0, 52, 5).build());

        assertThat(lines).hasSize(4);
        assertThat(lines[0]).isEqualTo("(0x0666) Unknown:                                   11208273272 (0x29C10AD78) bytes");
        assertThat(lines[1]).isEqualTo("  - disk (0005):                                    src.zip");
        assertThat(lines[2]).isEqualTo("  - size:                                           36 bytes");
        assertThat(lines[3]).isEqualTo("00 01 02 03");
    }
}
