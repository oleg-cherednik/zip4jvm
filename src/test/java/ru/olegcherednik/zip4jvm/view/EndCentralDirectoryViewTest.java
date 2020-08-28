package ru.olegcherednik.zip4jvm.view;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        when(block.getRelativeOffs()).thenReturn(255614L);

        EndCentralDirectory endCentralDirectory = createEndCentralDirectory(255533L);

        String[] lines = Zip4jvmSuite.execute(new EndCentralDirectoryView(endCentralDirectory, block, Charsets.UTF_8, 2, 52, 0));
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

    public void shouldRetrieveZip64NoteWhenEndCentralDirectoryWithLargeOffs() throws IOException {
        for (long centralDirectoryOffs : Arrays.asList(Zip64.LIMIT_DWORD, Zip64.LIMIT_DWORD + 100)) {
            Block block = mock(Block.class);
            when(block.getSize()).thenReturn(33L);
            when(block.getRelativeOffs()).thenReturn(255614L);

            EndCentralDirectory endCentralDirectory = createEndCentralDirectory(centralDirectoryOffs);

            String[] lines = Zip4jvmSuite.execute(new EndCentralDirectoryView(endCentralDirectory, block, Charsets.UTF_8, 2, 52, 0));
            assertThat(lines).hasSize(14);
            assertThat(lines[0]).isEqualTo("(PK0506) End of Central directory record");
            assertThat(lines[1]).isEqualTo("========================================");
            assertThat(lines[2]).isEqualTo("  - location:                                       255614 (0x0003E67E) bytes");
            assertThat(lines[3]).isEqualTo("  - size:                                           33 bytes");
            assertThat(lines[4]).isEqualTo("  part number of this part (0001):                  2");
            assertThat(lines[5]).isEqualTo("  part number of start of central dir (0002):       3");
            assertThat(lines[6]).isEqualTo("  number of entries in central dir in this part:    13");
            assertThat(lines[7]).isEqualTo("  total number of entries in central dir:           15");
            assertThat(lines[8]).isEqualTo("  size of central dir:                              81 (0x00000051) bytes");
            assertThat(lines[9]).isEqualTo("  relative offset of central dir:                   4294967295 (0xFFFFFFFF) bytes");
            assertThat(lines[10]).isEqualTo("    (see real value in ZIP64 record)");
            assertThat(lines[11]).isEqualTo("  zipfile comment length:                           11 bytes");
            assertThat(lines[12]).isEqualTo("                                                    UTF-8");
            assertThat(lines[13]).isEqualTo("  70 61 73 73 77 6F 72 64 3A 20 31                  password: 1");
        }
    }

    public void shouldThrowIllegalArgumentExceptionWhenSomeParametersNull() {
        assertThatThrownBy(() -> new EndCentralDirectoryView(null, mock(Block.class), Charsets.UTF_8, 2, 52, 0))
                .isExactlyInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new EndCentralDirectoryView(mock(EndCentralDirectory.class), null, Charsets.UTF_8, 2, 52, 0))
                .isExactlyInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new EndCentralDirectoryView(mock(EndCentralDirectory.class), mock(Block.class), null, 2, 52, 0))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    public void shouldRetrieveAllLinesWithDiskWhenSplitZip() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(33L);
        when(block.getRelativeOffs()).thenReturn(255614L);
        when(block.getDisk()).thenReturn(5L);
        when(block.getFileName()).thenReturn("src.zip");

        EndCentralDirectory endCentralDirectory = createEndCentralDirectory(255533L);

        String[] lines = Zip4jvmSuite.execute(new EndCentralDirectoryView(endCentralDirectory, block, Charsets.UTF_8, 2, 52, 5));
        assertThat(lines).hasSize(14);
        assertThat(lines[0]).isEqualTo("(PK0506) End of Central directory record");
        assertThat(lines[1]).isEqualTo("========================================");
        assertThat(lines[2]).isEqualTo("  - disk (0005):                                    src.zip");
        assertThat(lines[3]).isEqualTo("  - location:                                       255614 (0x0003E67E) bytes");
        assertThat(lines[4]).isEqualTo("  - size:                                           33 bytes");
        assertThat(lines[5]).isEqualTo("  part number of this part (0001):                  2");
        assertThat(lines[6]).isEqualTo("  part number of start of central dir (0002):       3");
        assertThat(lines[7]).isEqualTo("  number of entries in central dir in this part:    13");
        assertThat(lines[8]).isEqualTo("  total number of entries in central dir:           15");
        assertThat(lines[9]).isEqualTo("  size of central dir:                              81 (0x00000051) bytes");
        assertThat(lines[10]).isEqualTo("  relative offset of central dir:                   255533 (0x0003E62D) bytes");
        assertThat(lines[11]).isEqualTo("  zipfile comment length:                           11 bytes");
        assertThat(lines[12]).isEqualTo("                                                    UTF-8");
        assertThat(lines[13]).isEqualTo("  70 61 73 73 77 6F 72 64 3A 20 31                  password: 1");
    }

    private static EndCentralDirectory createEndCentralDirectory(long centralDirectoryOffs) {
        EndCentralDirectory endCentralDirectory = new EndCentralDirectory();
        endCentralDirectory.setTotalDisks(1);
        endCentralDirectory.setMainDisk(2);
        endCentralDirectory.setDiskEntries(13);
        endCentralDirectory.setTotalEntries(15);
        endCentralDirectory.setCentralDirectorySize(81);
        endCentralDirectory.setCentralDirectoryRelativeOffs(centralDirectoryOffs);
        endCentralDirectory.setComment("password: 1");
        return endCentralDirectory;
    }
}
