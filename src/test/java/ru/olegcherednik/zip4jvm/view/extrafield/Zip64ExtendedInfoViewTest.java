package ru.olegcherednik.zip4jvm.view.extrafield;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.Zip64;
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
                                                                   .columnWidth(52).build());

        assertThat(lines).hasSize(6);
        assertThat(lines[0]).isEqualTo("(0x0001) Zip64 Extended Information:                12 bytes");
        assertThat(lines[1]).isEqualTo("  - location:                                       5300395 (0x0050E0AB) bytes");
        assertThat(lines[2]).isEqualTo("  original compressed size:                         11322883953 bytes");
        assertThat(lines[3]).isEqualTo("  original uncompressed size:                       11208273150 bytes");
        assertThat(lines[4]).isEqualTo("  original relative offset of local header:         145 (0x00000091) bytes");
        assertThat(lines[5]).isEqualTo("  original part number of this part (0002):         2");
    }

    public void shouldRetrieveEmptyStringWhenRecordNull() throws IOException {
        String[] lines = Zip4jvmSuite.execute(Zip64ExtendedInfoView.builder()
                                                                   .record(Zip64.ExtendedInfo.NULL)
                                                                   .block(mock(Block.class))
                                                                   .columnWidth(52).build());
        assertThat(lines).hasSize(1);
        assertThat(lines[0]).isEmpty();
    }
}
