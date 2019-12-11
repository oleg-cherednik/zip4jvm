package ru.olegcherednik.zip4jvm.view.crypto;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.ByteArrayBlock;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
@Test
public class PkwareEncryptionHeaderBlockViewTest {

    public void shouldRetrieveMultipleLinesWhenPkwareEncryptionHeader() throws IOException {
        PkwareEncryptionHeaderBlock encryptionHeader = mock(PkwareEncryptionHeaderBlock.class);
        ByteArrayBlock data = mock(ByteArrayBlock.class);

        when(encryptionHeader.getData()).thenReturn(data);
        when(data.getSize()).thenReturn(12L);
        when(data.getOffs()).thenReturn(60L);
        when(data.getData()).thenReturn(new byte[] { 0x0, 0x1, 0x2, 0x3 });

        String[] lines = Zip4jvmSuite.execute(new PkwareEncryptionHeaderView(encryptionHeader, 1, 2, 52));
        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("#2 (PKWARE) encryption header");
        assertThat(lines[1]).isEqualTo("-----------------------------");
        assertThat(lines[2]).isEqualTo("  data:                                             60 (0x0000003C) bytes");
        assertThat(lines[3]).isEqualTo("    - size:                                         12 bytes");
        assertThat(lines[4]).isEqualTo("  00 01 02 03");
    }

}
