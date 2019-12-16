package ru.olegcherednik.zip4jvm.view.centraldirectory;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 11.11.2019
 */
@Test
public class DigitalSignatureViewTest {

    public void shouldRetrieveAllLinesWhenDigitalSignatureExists() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(33L);
        when(block.getOffs()).thenReturn(255614L);

        CentralDirectory.DigitalSignature digitalSignature = new CentralDirectory.DigitalSignature();
        digitalSignature.setSignatureData(new byte[] { 0x0, 0x1, 0x2, 0x3 });

        String[] lines = Zip4jvmSuite.execute(new DigitalSignatureView(digitalSignature, block, 2, 52));

        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("(PK0505) Digital signature");
        assertThat(lines[1]).isEqualTo("==========================");
        assertThat(lines[2]).isEqualTo("  - location:                                       255614 (0x0003E67E) bytes");
        assertThat(lines[3]).isEqualTo("  - size:                                           33 bytes");
        assertThat(lines[4]).isEqualTo("  00 01 02 03");
    }
}
