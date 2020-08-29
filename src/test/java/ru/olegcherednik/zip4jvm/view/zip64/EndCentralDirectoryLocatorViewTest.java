package ru.olegcherednik.zip4jvm.view.zip64;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.Zip64View;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 11.11.2019
 */
@Test
public class EndCentralDirectoryLocatorViewTest {

    public void shouldRetrieveAllLinesWhenLocatorExists() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(20L);
        when(block.getRelativeOffs()).thenReturn(11208273384L);

        Zip64.EndCentralDirectoryLocator locator = mock(Zip64.EndCentralDirectoryLocator.class);
        when(locator.getMainDiskNo()).thenReturn(1L);
        when(locator.getEndCentralDirectoryRelativeOffs()).thenReturn(11208273328L);
        when(locator.getTotalDisks()).thenReturn(5L);

        String[] lines = Zip4jvmSuite.execute(new Zip64View.EndCentralDirectoryLocatorView(locator, block, 2, 52, 0));
        assertThat(lines).hasSize(7);
        assertThat(lines[0]).isEqualTo("(PK0607) ZIP64 End of Central directory locator");
        assertThat(lines[1]).isEqualTo("===============================================");
        assertThat(lines[2]).isEqualTo("  - location:                                       11208273384 (0x29C10ADE8) bytes");
        assertThat(lines[3]).isEqualTo("  - size:                                           20 bytes");
        assertThat(lines[4]).isEqualTo("  part number of new-end-of-central-dir (0001):     2");
        assertThat(lines[5]).isEqualTo("  relative offset of new-end-of-central-dir:        11208273328 (0x29C10ADB0) bytes");
        assertThat(lines[6]).isEqualTo("  total number of parts in archive:                 5");
    }

    public void shouldRetrieveAllLineWithDiskWhenSplitZip() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(20L);
        when(block.getRelativeOffs()).thenReturn(11208273384L);
        when(block.getDiskNo()).thenReturn(5);
        when(block.getFileName()).thenReturn("src.zip");

        Zip64.EndCentralDirectoryLocator locator = mock(Zip64.EndCentralDirectoryLocator.class);
        when(locator.getMainDiskNo()).thenReturn(1L);
        when(locator.getEndCentralDirectoryRelativeOffs()).thenReturn(11208273328L);
        when(locator.getTotalDisks()).thenReturn(5L);

        String[] lines = Zip4jvmSuite.execute(new Zip64View.EndCentralDirectoryLocatorView(locator, block, 2, 52, 5));
        assertThat(lines).hasSize(8);
        assertThat(lines[0]).isEqualTo("(PK0607) ZIP64 End of Central directory locator");
        assertThat(lines[1]).isEqualTo("===============================================");
        assertThat(lines[2]).isEqualTo("  - disk (0005):                                    src.zip");
        assertThat(lines[3]).isEqualTo("  - location:                                       11208273384 (0x29C10ADE8) bytes");
        assertThat(lines[4]).isEqualTo("  - size:                                           20 bytes");
        assertThat(lines[5]).isEqualTo("  part number of new-end-of-central-dir (0001):     2");
        assertThat(lines[6]).isEqualTo("  relative offset of new-end-of-central-dir:        11208273328 (0x29C10ADB0) bytes");
        assertThat(lines[7]).isEqualTo("  total number of parts in archive:                 5");
    }
}
