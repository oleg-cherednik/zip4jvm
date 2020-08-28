package ru.olegcherednik.zip4jvm.view.crypto;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;

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
public class PkwareEncryptionHeaderBlockViewTest {

    public void shouldRetrieveMultipleLinesWhenPkwareEncryptionHeader() throws IOException {
        PkwareEncryptionHeaderBlock block = mock(PkwareEncryptionHeaderBlock.class);

        when(block.getSize()).thenReturn(4L);
        when(block.getRelativeOffs()).thenReturn(60L);
        when(block.getData()).thenReturn(new byte[] { 0x0, 0x1, 0x2, 0x3 });

        String[] lines = Zip4jvmSuite.execute(new PkwareEncryptionHeaderView(block, 1, 2, 52, 0));
        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("#2 (PKWARE) encryption header");
        assertThat(lines[1]).isEqualTo("-----------------------------");
        assertThat(lines[2]).isEqualTo("  - location:                                       60 (0x0000003C) bytes");
        assertThat(lines[3]).isEqualTo("  - size:                                           4 bytes");
        assertThat(lines[4]).isEqualTo("  00 01 02 03");
    }

}
