/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.model;

import ru.olegcherednik.zip4jvm.model.settings.CompressionLevelEnum;
import ru.olegcherednik.zip4jvm.utils.BitUtils;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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

        generalPurposeFlag = new GeneralPurposeFlag(BIT0);
        assertThat(generalPurposeFlag.isEncrypted()).isTrue();
    }

    public void shouldSetCompressionLevelFastestWhenBit1Bit2Set() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.getCompressionLevel()).isSameAs(CompressionLevelEnum.NORMAL);

        generalPurposeFlag = new GeneralPurposeFlag(BIT1 | BIT2);
        assertThat(generalPurposeFlag.getCompressionLevel()).isSameAs(CompressionLevelEnum.SUPER_FAST);

        generalPurposeFlag = new GeneralPurposeFlag(BIT1);
        assertThat(generalPurposeFlag.getCompressionLevel()).isSameAs(CompressionLevelEnum.MAXIMUM);

        generalPurposeFlag = new GeneralPurposeFlag(BIT2);
        assertThat(generalPurposeFlag.getCompressionLevel()).isSameAs(CompressionLevelEnum.FAST);
    }

    public void shouldSetDataDescriptorAvailableWhenBit3Set() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.isDataDescriptorAvailable()).isFalse();

        generalPurposeFlag = new GeneralPurposeFlag(BIT3);
        assertThat(generalPurposeFlag.isDataDescriptorAvailable()).isTrue();
    }

    public void shouldSetStrongEncryptionWhenBit6Set() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.isStrongEncryption()).isFalse();

        generalPurposeFlag = new GeneralPurposeFlag(BIT6);
        assertThat(generalPurposeFlag.isStrongEncryption()).isTrue();
    }

    public void shouldSetUtf8WhenBit11Set() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.isUtf8()).isFalse();

        generalPurposeFlag = new GeneralPurposeFlag(BIT11);
        assertThat(generalPurposeFlag.isUtf8()).isTrue();
    }

    public void shouldSetBit0WhenEncrypted() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.isEncrypted()).isFalse();
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(Compression.STORE), BIT0)).isFalse();

        generalPurposeFlag.setEncrypted(true);
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(Compression.STORE), BIT0)).isTrue();
    }

    public void shouldSetBit1Bit2WhenCompressionLevel() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.getCompressionLevel()).isSameAs(CompressionLevelEnum.NORMAL);
        assertThat(BitUtils.isBitClear(generalPurposeFlag.getAsInt(Compression.STORE), BIT1 | BIT2)).isTrue();

        generalPurposeFlag.setCompressionLevel(CompressionLevelEnum.SUPER_FAST);
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(Compression.DEFLATE), BIT1 | BIT2)).isTrue();

        generalPurposeFlag.setCompressionLevel(CompressionLevelEnum.MAXIMUM);
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(Compression.DEFLATE), BIT1)).isTrue();
        assertThat(BitUtils.isBitClear(generalPurposeFlag.getAsInt(Compression.DEFLATE), BIT2)).isTrue();

        generalPurposeFlag.setCompressionLevel(CompressionLevelEnum.FAST);
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(Compression.DEFLATE), BIT2)).isTrue();
        assertThat(BitUtils.isBitClear(generalPurposeFlag.getAsInt(Compression.DEFLATE), BIT1)).isTrue();
    }

    public void shouldSetBit3WhenDataDescriptorAvailable() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.isDataDescriptorAvailable()).isFalse();
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(Compression.STORE), BIT3)).isFalse();

        generalPurposeFlag.setDataDescriptorAvailable(true);
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(Compression.STORE), BIT3)).isTrue();
    }

    public void shouldSetBit6WhenStrongEncryption() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.isStrongEncryption()).isFalse();
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(Compression.STORE), BIT6)).isFalse();

        generalPurposeFlag.setStrongEncryption(true);
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(Compression.STORE), BIT6)).isTrue();
    }

    public void shouldSetBit11WhenUtf8() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.isUtf8()).isFalse();
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(Compression.STORE), BIT11)).isFalse();

        generalPurposeFlag.setUtf8(true);
        assertThat(BitUtils.isBitSet(generalPurposeFlag.getAsInt(Compression.STORE), BIT11)).isTrue();
    }

    public void shouldRetrieveImplodedBitsWhenFileImploded() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.getAsInt(Compression.FILE_IMPLODED)).isEqualTo(0x0);

        generalPurposeFlag.setSlidingDictionarySize(SlidingDictionarySize.SD_8K);
        generalPurposeFlag.setShannonFanoTreesNumber(ShannonFanoTreesNumber.THREE);
        assertThat(generalPurposeFlag.getAsInt(Compression.FILE_IMPLODED)).isEqualTo(BIT1 | BIT2);

        generalPurposeFlag.setSlidingDictionarySize(SlidingDictionarySize.SD_8K);
        generalPurposeFlag.setShannonFanoTreesNumber(ShannonFanoTreesNumber.TWO);
        assertThat(generalPurposeFlag.getAsInt(Compression.FILE_IMPLODED)).isEqualTo(BIT1);

        generalPurposeFlag.setSlidingDictionarySize(SlidingDictionarySize.SD_4K);
        generalPurposeFlag.setShannonFanoTreesNumber(ShannonFanoTreesNumber.THREE);
        assertThat(generalPurposeFlag.getAsInt(Compression.FILE_IMPLODED)).isEqualTo(BIT2);
    }

    public void shouldRetrieveLzmaBitsWhenLzma() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.getAsInt(Compression.LZMA)).isEqualTo(0x0);

        generalPurposeFlag.setLzmaEosMarker(true);
        assertThat(generalPurposeFlag.getAsInt(Compression.LZMA)).isEqualTo(BIT1);
    }

    @Test(dataProvider = "compressionMethods")
    public void shouldRetrieveDeflateBitsWhenDeflate(Compression compression) {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        assertThat(generalPurposeFlag.getAsInt(compression)).isEqualTo(0x0);

        generalPurposeFlag.setCompressionLevel(CompressionLevelEnum.MAXIMUM);
        assertThat(generalPurposeFlag.getAsInt(compression)).isEqualTo(BIT1);

        generalPurposeFlag.setCompressionLevel(CompressionLevelEnum.FAST);
        assertThat(generalPurposeFlag.getAsInt(compression)).isEqualTo(BIT2);
        generalPurposeFlag.setCompressionLevel(CompressionLevelEnum.SUPER_FAST);
        assertThat(generalPurposeFlag.getAsInt(compression)).isEqualTo(BIT1 | BIT2);
    }

    @DataProvider(name = "compressionMethods", parallel = true)
    public static Object[][] compressionMethods() {
        return new Object[][] {
                { Compression.DEFLATE },
                { Compression.DEFLATE_64 } };
    }

}
