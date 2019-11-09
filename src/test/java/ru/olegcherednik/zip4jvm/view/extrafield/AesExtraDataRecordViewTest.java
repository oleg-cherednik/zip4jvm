package ru.olegcherednik.zip4jvm.view.extrafield;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
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
public class AesExtraDataRecordViewTest {

    public void shouldRetrieveMultipleLinesWhenViewAesRecord() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(11L);
        when(block.getOffs()).thenReturn(255603L);


        AesExtraDataRecord record = AesExtraDataRecord.builder()
                                                      .dataSize(7)
                                                      .versionNumber(2)
                                                      .vendor("AE")
                                                      .strength(AesStrength.S256)
                                                      .compressionMethod(CompressionMethod.DEFLATE).build();

        String[] lines = Zip4jvmSuite.execute(AesExtraDataRecordView.builder()
                                                                    .record(record)
                                                                    .generalPurposeFlag(new GeneralPurposeFlag(2057))
                                                                    .block(block)
                                                                    .columnWidth(52).build());
        assertThat(lines).hasSize(6);
        assertThat(lines[0]).isEqualTo("(0x9901) AES Encryption Tag:                        11 bytes");
        assertThat(lines[1]).isEqualTo("  - location:                                       255603 (0x0003E673) bytes");
        assertThat(lines[2]).isEqualTo("  Encryption Tag Version:                           AE-2");
        assertThat(lines[3]).isEqualTo("  Encryption Key Bits:                              256");
        assertThat(lines[4]).isEqualTo("  compression method (08):                          deflated");
        assertThat(lines[5]).isEqualTo("    compression sub-type (deflation):               normal");
    }

    public void shouldRetrieveEmptyStringWhenRecordNull() throws IOException {
        String[] lines = Zip4jvmSuite.execute(AesExtraDataRecordView.builder()
                                                                    .record(AesExtraDataRecord.NULL)
                                                                    .generalPurposeFlag(mock(GeneralPurposeFlag.class))
                                                                    .block(mock(Block.class))
                                                                    .columnWidth(52).build());
        assertThat(lines).hasSize(1);
        assertThat(lines[0]).isEmpty();
    }

}
