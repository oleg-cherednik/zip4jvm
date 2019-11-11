package ru.olegcherednik.zip4jvm.view.entry;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
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
public class DataDescriptorViewTest {

    public void shouldRetrieveAllLinesWhenDataDescriptorExists() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(16L);
        when(block.getOffs()).thenReturn(255496L);

        DataDescriptor dataDescriptor = DataDescriptor.builder()
                                                      .crc32(3992319659L)
                                                      .compressedSize(255436L)
                                                      .uncompressedSize(293823L).build();

        String[] lines = Zip4jvmSuite.execute(DataDescriptorView.builder()
                                                                .dataDescriptor(dataDescriptor)
                                                                .block(block)
                                                                .pos(1)
                                                                .offs(2)
                                                                .columnWidth(52).build());

        assertThat(lines).hasSize(7);
        assertThat(lines[0]).isEqualTo("#2 (PK0708) Data descriptor");
        assertThat(lines[1]).isEqualTo("---------------------------");
        assertThat(lines[2]).isEqualTo("  - location:                                       255496 (0x0003E608) bytes");
        assertThat(lines[3]).isEqualTo("  - size:                                           16 bytes");
        assertThat(lines[4]).isEqualTo("  32-bit CRC value:                                 0xEDF5F6AB");
        assertThat(lines[5]).isEqualTo("  compressed size:                                  255436 bytes");
        assertThat(lines[6]).isEqualTo("  uncompressed size:                                293823 bytes");
    }
}
