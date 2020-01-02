package ru.olegcherednik.zip4jvm.view.extrafield;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;

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
public class Zip64ExtendedInfoViewTest {

    public void shouldRetrieveAllDataWhenAllDataSet() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(12L);
        when(block.getOffs()).thenReturn(5300395L);

        Zip64.ExtendedInfo record = Zip64.ExtendedInfo.builder()
                                                      .disk(2)
                                                      .localFileHeaderOffs(145)
                                                      .compressedSize(11208273150L)
                                                      .uncompressedSize(11322883953L).build();

        String[] lines = Zip4jvmSuite.execute(Zip64ExtendedInfoView.builder()
                                                                   .record(record)
                                                                   .block(block)
                                                                   .position(0, 52, 0).build());

        assertThat(lines).hasSize(6);
        assertThat(lines[0]).isEqualTo("(0x0001) Zip64 Extended Information:                5300395 (0x0050E0AB) bytes");
        assertThat(lines[1]).isEqualTo("  - size:                                           12 bytes");
        assertThat(lines[2]).isEqualTo("  original compressed size:                         11322883953 bytes");
        assertThat(lines[3]).isEqualTo("  original uncompressed size:                       11208273150 bytes");
        assertThat(lines[4]).isEqualTo("  original relative offset of local header:         145 (0x00000091) bytes");
        assertThat(lines[5]).isEqualTo("  original part number of this part (0002):         2");
    }

    public void shouldRetrieveFalseWhenRecordNull() throws IOException {
        PrintStream out = mock(PrintStream.class);
        Zip64ExtendedInfoView view = Zip64ExtendedInfoView.builder()
                                                          .record(Zip64.ExtendedInfo.NULL)
                                                          .block(mock(Block.class))
                                                          .position(0, 52, 0).build();
        assertThat(view.print(out)).isFalse();
    }

    public void shouldRetrieveAllDataWithDiskWhenSplit() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(12L);
        when(block.getOffs()).thenReturn(5300395L);
        when(block.getDisk()).thenReturn(5L);
        when(block.getFileName()).thenReturn("src.zip");

        Zip64.ExtendedInfo record = Zip64.ExtendedInfo.builder()
                                                      .disk(2)
                                                      .localFileHeaderOffs(145)
                                                      .compressedSize(11208273150L)
                                                      .uncompressedSize(11322883953L).build();

        String[] lines = Zip4jvmSuite.execute(Zip64ExtendedInfoView.builder()
                                                                   .record(record)
                                                                   .block(block)
                                                                   .position(0, 52, 5).build());

        assertThat(lines).hasSize(7);
        assertThat(lines[0]).isEqualTo("(0x0001) Zip64 Extended Information:                5300395 (0x0050E0AB) bytes");
        assertThat(lines[1]).isEqualTo("  - disk (0005):                                    src.zip");
        assertThat(lines[2]).isEqualTo("  - size:                                           12 bytes");
        assertThat(lines[3]).isEqualTo("  original compressed size:                         11322883953 bytes");
        assertThat(lines[4]).isEqualTo("  original uncompressed size:                       11208273150 bytes");
        assertThat(lines[5]).isEqualTo("  original relative offset of local header:         145 (0x00000091) bytes");
        assertThat(lines[6]).isEqualTo("  original part number of this part (0002):         2");
    }
}
