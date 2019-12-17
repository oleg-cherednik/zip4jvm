package ru.olegcherednik.zip4jvm.view.extrafield;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.model.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.IOException;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
@Test
public class AesExtraFieldRecordViewTest {

    public void shouldRetrieveMultipleLinesWhenViewAesRecord() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(11L);
        when(block.getOffs()).thenReturn(255603L);

        AesExtraFieldRecord record = AesExtraFieldRecord.builder()
                                                        .dataSize(7)
                                                        .versionNumber(2)
                                                        .vendor("AE")
                                                        .strength(AesStrength.S256)
                                                        .compressionMethod(CompressionMethod.DEFLATE).build();

        String[] lines = Zip4jvmSuite.execute(AesExtraFieldRecordView.builder()
                                                                     .record(record)
                                                                     .generalPurposeFlag(new GeneralPurposeFlag(2057))
                                                                     .block(block)
                                                                     .position(0, 52).build());
        assertThat(lines).hasSize(6);
        assertThat(lines[0]).isEqualTo("(0x9901) AES Encryption Tag:                        255603 (0x0003E673) bytes");
        assertThat(lines[1]).isEqualTo("  - size:                                           11 bytes");
        assertThat(lines[2]).isEqualTo("  Encryption Tag Version:                           AE-2");
        assertThat(lines[3]).isEqualTo("  Encryption Key Bits:                              256");
        assertThat(lines[4]).isEqualTo("  compression method (08):                          deflated");
        assertThat(lines[5]).isEqualTo("    compression sub-type (deflation):               normal");
    }

    public void shouldRetrieveEmptyStringWhenRecordNull() throws IOException {
        PrintStream out = mock(PrintStream.class);
        AesExtraFieldRecordView view = AesExtraFieldRecordView.builder()
                                                              .record(AesExtraFieldRecord.NULL)
                                                              .generalPurposeFlag(mock(GeneralPurposeFlag.class))
                                                              .block(mock(Block.class))
                                                              .position(0, 52).build();
        assertThat(view.print(out)).isFalse();
    }

}
