package ru.olegcherednik.zip4jvm.view;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 10.11.2019
 */
@Test
public class EndCentralDirectoryViewTest {

    public void shouldRetrieveAllLinesWhenEndCentralDirectoryExists() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(33L);
        when(block.getOffs()).thenReturn(255614L);

        EndCentralDirectory endCentralDirectory = new EndCentralDirectory();
        endCentralDirectory.setTotalDisks(1);
        endCentralDirectory.setMainDisk(2);
        endCentralDirectory.setDiskEntries(13);
        endCentralDirectory.setTotalEntries(15);
        endCentralDirectory.setCentralDirectorySize(81);
        endCentralDirectory.setCentralDirectoryOffs(255533L);
        endCentralDirectory.setComment("password: 1");

        String[] lines = Zip4jvmSuite.execute(EndCentralDirectoryView.builder()
                                                                     .endCentralDirectory(endCentralDirectory)
                                                                     .block(block)
                                                                     .charset(Charsets.UTF_8)
                                                                     .position(2, 52).build());
        assertThat(lines).hasSize(13);
        assertThat(lines[0]).isEqualTo("(PK0506) End of Central directory record");
        assertThat(lines[1]).isEqualTo("========================================");
        assertThat(lines[2]).isEqualTo("  - location:                                       255614 (0x0003E67E) bytes");
        assertThat(lines[3]).isEqualTo("  - size:                                           33 bytes");
        assertThat(lines[4]).isEqualTo("  part number of this part (0001):                  2");
        assertThat(lines[5]).isEqualTo("  part number of start of central dir (0002):       3");
        assertThat(lines[6]).isEqualTo("  number of entries in central dir in this part:    13");
        assertThat(lines[7]).isEqualTo("  total number of entries in central dir:           15");
        assertThat(lines[8]).isEqualTo("  size of central dir:                              81 (0x00000051) bytes");
        assertThat(lines[9]).isEqualTo("  relative offset of central dir:                   255533 (0x0003E62D) bytes");
        assertThat(lines[10]).isEqualTo("  zipfile comment length:                           11 bytes");
        assertThat(lines[11]).isEqualTo("                                                    UTF-8");
        assertThat(lines[12]).isEqualTo("  70 61 73 73 77 6F 72 64 3A 20 31                  password: 1");
    }
}
