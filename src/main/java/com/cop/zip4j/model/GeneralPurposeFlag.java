package com.cop.zip4j.model;

import com.cop.zip4j.utils.BitUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.IntSupplier;

import static com.cop.zip4j.utils.BitUtils.BIT0;
import static com.cop.zip4j.utils.BitUtils.BIT1;
import static com.cop.zip4j.utils.BitUtils.BIT11;
import static com.cop.zip4j.utils.BitUtils.BIT2;
import static com.cop.zip4j.utils.BitUtils.BIT3;
import static com.cop.zip4j.utils.BitUtils.BIT6;

/**
 * see 4.4.4
 *
 * @author Oleg Cherednik
 * @since 12.03.2019
 */
@Getter
@Setter
@NoArgsConstructor
public class GeneralPurposeFlag implements IntSupplier {

    private boolean encrypted;
    private CompressionLevel compressionLevel;
    /** {@link DataDescriptor} */
    private boolean dataDescriptorExists;
    private boolean strongEncryption;
    private boolean utf8;

    public void read(int data) {
        encrypted = BitUtils.isBitSet(data, BIT0);

        if (BitUtils.isBitSet(data, BIT1 | BIT2))
            compressionLevel = CompressionLevel.FASTEST;
        else if (BitUtils.isBitSet(data, BIT2))
            compressionLevel = CompressionLevel.FAST;
        else compressionLevel = BitUtils.isBitSet(data, BIT1) ? CompressionLevel.MAXIMUM : CompressionLevel.NORMAL;

        dataDescriptorExists = BitUtils.isBitSet(data, BIT3);
        strongEncryption = BitUtils.isBitSet(data, BIT6);
        utf8 = BitUtils.isBitSet(data, BIT11);
    }

    @Override
    public int getAsInt() {
        int data = BitUtils.updateBits(0, BIT0, encrypted);

        if (compressionLevel == CompressionLevel.MAXIMUM)
            data = BitUtils.setBits(data, BIT1);
        else if (compressionLevel == CompressionLevel.FAST)
            data = BitUtils.setBits(data, BIT2);
        else if (compressionLevel == CompressionLevel.FASTEST)
            data = BitUtils.setBits(data, BIT1 | BIT2);

        data = BitUtils.updateBits(data, BIT3, dataDescriptorExists);
        data = BitUtils.updateBits(data, BIT6, strongEncryption);
        data = BitUtils.updateBits(data, BIT11, utf8);

        return data;
    }
}
