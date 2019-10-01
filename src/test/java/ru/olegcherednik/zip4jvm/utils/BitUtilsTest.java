package ru.olegcherednik.zip4jvm.utils;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT2;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT3;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT4;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT5;

/**
 * @author Oleg Cherednik
 * @since 01.10.2019
 */
@Test
public class BitUtilsTest {

    public void shouldRetrieveTrueWhenAllRequiredBitsAreSet() {
        assertThat(BitUtils.isBitSet(BIT3 | BIT5, BIT3)).isTrue();
        assertThat(BitUtils.isBitSet(BIT3 | BIT5, BIT3 | BIT5)).isTrue();
        assertThat(BitUtils.isBitSet(BIT3 | BIT5, BIT2)).isFalse();
        assertThat(BitUtils.isBitSet(BIT3 | BIT5, BIT3 | BIT4)).isFalse();
    }

    public void shouldRetrieveTrueWhenAllRequiredBitsAreCleared() {
        assertThat(BitUtils.isBitClear(BIT3 | BIT5, BIT2)).isTrue();
        assertThat(BitUtils.isBitClear(BIT3 | BIT5, BIT2 | BIT4)).isTrue();
        assertThat(BitUtils.isBitClear(BIT3 | BIT5, BIT3)).isFalse();
        assertThat(BitUtils.isBitClear(BIT3 | BIT5, BIT2 | BIT5)).isFalse();
    }

    public void shouldRetrieveValueWithSetBitsWhenRequiredBitsToSet() {
        assertThat(BitUtils.setBits(BIT3, BIT3)).isEqualTo(BIT3);
        assertThat(BitUtils.setBits(BIT3, BIT4)).isEqualTo(BIT3 | BIT4);
    }

    public void shouldRetrieveValueWithClearedBitsWhenRequiredBitsToClear() {
        assertThat(BitUtils.clearBits(BIT3, BIT3)).isZero();
        assertThat(BitUtils.clearBits(BIT3 | BIT4, BIT4)).isEqualTo(BIT3);
    }
}
