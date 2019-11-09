package ru.olegcherednik.zip4jvm.view.crypto;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.PkwareEncryptionHeader;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
@Test
public class PkwareEncryptionHeaderViewTest {

    public void shouldRetrieveCompressionMethodTitleWhenSingleLine() throws IOException {
        PkwareEncryptionHeader encryptionHeader = mock(PkwareEncryptionHeader.class);
        Diagnostic.ByteArrayBlockB data = mock(Diagnostic.ByteArrayBlockB.class);

        when(encryptionHeader.getData()).thenReturn(data);
        when(data.getSize()).thenReturn(12L);
        when(data.getOffs()).thenReturn(60L);
        when(data.getData()).thenReturn(new byte[] { 0x0, 0x1, 0x2, 0x3 });

        String[] lines = Zip4jvmSuite.execute(PkwareEncryptionHeaderView.builder()
                                                                        .encryptionHeader(encryptionHeader)
                                                                        .pos(1)
                                                                        .offs(2)
                                                                        .columnWidth(52).build());
        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("#2 (PKWARE) encryption header");
        assertThat(lines[1]).isEqualTo("=============================");
        assertThat(lines[2]).isEqualTo("  data:                                             12 bytes");
        assertThat(lines[3]).isEqualTo("  - location:                                       60 (0x0000003C) bytes");
        assertThat(lines[4]).isEqualTo("  00 01 02 03");
    }

}
