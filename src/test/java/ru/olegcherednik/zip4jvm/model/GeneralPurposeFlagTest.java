package ru.olegcherednik.zip4jvm.model;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.utils.BitUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT0;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT1;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT11;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT2;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT3;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT6;

/**
 * @author Oleg Cherednik
 * @since 28.09.2019
 */
@Test
public class GeneralPurposeFlagTest {

    public void shouldSetEncryptedWhenBit0Set() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.isEncrypted()).isFalse();

        generalPurposeFlag.read(BIT0);
        assertThat(generalPurposeFlag.isEncrypted()).isTrue();
    }

    public void shouldSetCompressionLevelFastestWhenBit1Bit2Set() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.getCompressionLevel()).isSameAs(CompressionLevel.NORMAL);

        generalPurposeFlag.read(BIT1 | BIT2);
        assertThat(generalPurposeFlag.getCompressionLevel()).isSameAs(CompressionLevel.SUPER_FAST);

        generalPurposeFlag.read(BIT1);
        assertThat(generalPurposeFlag.getCompressionLevel()).isSameAs(CompressionLevel.MAXIMUM);

        generalPurposeFlag.read(BIT2);
        assertThat(generalPurposeFlag.getCompressionLevel()).isSameAs(CompressionLevel.FAST);
    }

    public void shouldSetDataDescriptorAvailableWhenBit3Set() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.isDataDescriptorAvailable()).isFalse();

        generalPurposeFlag.read(BIT3);
        assertThat(generalPurposeFlag.isDataDescriptorAvailable()).isTrue();
    }

    public void shouldSetStrongEncryptionWhenBit6Set() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.isStrongEncryption()).isFalse();

        generalPurposeFlag.read(BIT6);
        assertThat(generalPurposeFlag.isStrongEncryption()).isTrue();
    }

    public void shouldSetUtf8WhenBit11Set() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.isUtf8()).isFalse();

        generalPurposeFlag.read(BIT11);
        assertThat(generalPurposeFlag.isUtf8()).isTrue();
    }

    public void shouldSetBit0WhenEncrypted() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.isEncrypted()).isFalse();
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(CompressionMethod.STORE), BIT0)).isFalse();

        generalPurposeFlag.setEncrypted(true);
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(CompressionMethod.STORE), BIT0)).isTrue();
    }

    public void shouldSetBit1Bit2WhenCompressionLevel() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.getCompressionLevel()).isSameAs(CompressionLevel.NORMAL);
        assertThat(BitUtils.isBitClear(generalPurposeFlag.getAsInt(CompressionMethod.STORE), BIT1 | BIT2)).isTrue();

        generalPurposeFlag.setCompressionLevel(CompressionLevel.SUPER_FAST);
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(CompressionMethod.DEFLATE), BIT1 | BIT2)).isTrue();

        generalPurposeFlag.setCompressionLevel(CompressionLevel.MAXIMUM);
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(CompressionMethod.DEFLATE), BIT1)).isTrue();
        assertThat(BitUtils.isBitClear(generalPurposeFlag.getAsInt(CompressionMethod.DEFLATE), BIT2)).isTrue();

        generalPurposeFlag.setCompressionLevel(CompressionLevel.FAST);
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(CompressionMethod.DEFLATE), BIT2)).isTrue();
        assertThat(BitUtils.isBitClear(generalPurposeFlag.getAsInt(CompressionMethod.DEFLATE), BIT1)).isTrue();
    }

    public void shouldSetBit3WhenDataDescriptorAvailable() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.isDataDescriptorAvailable()).isFalse();
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(CompressionMethod.STORE), BIT3)).isFalse();

        generalPurposeFlag.setDataDescriptorAvailable(true);
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(CompressionMethod.STORE), BIT3)).isTrue();
    }

    public void shouldSetBit6WhenStrongEncryption() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.isStrongEncryption()).isFalse();
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(CompressionMethod.STORE), BIT6)).isFalse();

        generalPurposeFlag.setStrongEncryption(true);
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(CompressionMethod.STORE), BIT6)).isTrue();
    }

    public void shouldSetBit11WhenUtf8() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.isUtf8()).isFalse();
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(CompressionMethod.STORE), BIT11)).isFalse();

        generalPurposeFlag.setUtf8(true);
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(CompressionMethod.STORE), BIT11)).isTrue();
    }

}
