package ru.olegcherednik.zip4jvm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.utils.BitUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.IntSupplier;

import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT0;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT1;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT11;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT2;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT3;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT6;

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
    private boolean dataDescriptorAvailable;
    private boolean strongEncryption;
    private boolean utf8;

    public GeneralPurposeFlag(int data) {
        read(data);
    }

    public void read(int data) {
        encrypted = BitUtils.isBitSet(data, BIT0);

        if (BitUtils.isBitSet(data, BIT1 | BIT2))
            compressionLevel = CompressionLevel.FASTEST;
        else if (BitUtils.isBitSet(data, BIT2))
            compressionLevel = CompressionLevel.FAST;
        else compressionLevel = BitUtils.isBitSet(data, BIT1) ? CompressionLevel.MAXIMUM : CompressionLevel.NORMAL;

        dataDescriptorAvailable = BitUtils.isBitSet(data, BIT3);
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

        data = BitUtils.updateBits(data, BIT3, dataDescriptorAvailable);
        data = BitUtils.updateBits(data, BIT6, strongEncryption);
        data = BitUtils.updateBits(data, BIT11, utf8);

        return data;
    }

    private static final Charset IBM437 = Charset.forName("IBM437");

    public Charset getCharset() {
        return utf8 ? StandardCharsets.UTF_8 : IBM437;
    }
}
