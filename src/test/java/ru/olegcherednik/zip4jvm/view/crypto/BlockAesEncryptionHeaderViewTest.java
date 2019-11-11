package ru.olegcherednik.zip4jvm.view.crypto;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.BlockAesEncryptionHeader;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 10.11.2019
 */
@Test
public class BlockAesEncryptionHeaderViewTest {

    public void shouldRetrieveMultipleLinesWhenAesEncryptionHeader() throws IOException {
        BlockAesEncryptionHeader encryptionHeader = mock(BlockAesEncryptionHeader.class);
        Diagnostic.ByteArrayBlock salt = mock(Diagnostic.ByteArrayBlock.class);
        Diagnostic.ByteArrayBlock passwordChecksum = mock(Diagnostic.ByteArrayBlock.class);
        Diagnostic.ByteArrayBlock mac = mock(Diagnostic.ByteArrayBlock.class);

        when(encryptionHeader.getSalt()).thenReturn(salt);
        when(encryptionHeader.getPasswordChecksum()).thenReturn(passwordChecksum);
        when(encryptionHeader.getMac()).thenReturn(mac);

        when(salt.getSize()).thenReturn(16L);
        when(salt.getOffs()).thenReturn(65L);
        when(salt.getData()).thenReturn(new byte[] { 0x0, 0x1, 0x2, 0x3, 0x4 });

        when(passwordChecksum.getSize()).thenReturn(2L);
        when(passwordChecksum.getOffs()).thenReturn(81L);
        when(passwordChecksum.getData()).thenReturn(new byte[] { 0x5, 0x6 });

        when(mac.getSize()).thenReturn(10L);
        when(mac.getOffs()).thenReturn(255507L);
        when(mac.getData()).thenReturn(new byte[] { 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF });

        String[] lines = Zip4jvmSuite.execute(BlockAesEncryptionHeaderView.builder()
                                                                          .encryptionHeader(encryptionHeader)
                                                                          .pos(1)
                                                                          .offs(2)
                                                                          .columnWidth(52).build());
        assertThat(lines).hasSize(11);
        assertThat(lines[0]).isEqualTo("#2 (AES) encryption header");
        assertThat(lines[1]).isEqualTo("--------------------------");
        assertThat(lines[2]).isEqualTo("  salt:                                             65 (0x00000041) bytes");
        assertThat(lines[3]).isEqualTo("    - size:                                         16 bytes");
        assertThat(lines[4]).isEqualTo("  00 01 02 03 04");
        assertThat(lines[5]).isEqualTo("  password checksum:                                81 (0x00000051) bytes");
        assertThat(lines[6]).isEqualTo("    - size:                                         2 bytes");
        assertThat(lines[7]).isEqualTo("  05 06");
        assertThat(lines[8]).isEqualTo("  mac:                                              255507 (0x0003E613) bytes");
        assertThat(lines[9]).isEqualTo("    - size:                                         10 bytes");
        assertThat(lines[10]).isEqualTo("  07 08 09 0A 0B 0C 0D 0E 0F");
    }
}
