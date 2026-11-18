package io.airlift.compress.zstd.model;

import io.airlift.compress.zstd.In;
import lombok.Getter;

import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT0;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT1;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT2;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT3;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT4;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT5;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT6;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT7;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.getValue;

/**
 * see: 3.1.1.1.2.
 *
 * @author Oleg Cherednik
 * @since 18.11.2026
 */
@Getter
public class WindowDescriptor {

    private int mantissa;
    private int exponent;

    public static WindowDescriptor decode(In in) {
        int b = in.readByte();

        WindowDescriptor wd = new WindowDescriptor();
        wd.mantissa = getValue(b, BIT0 | BIT1 | BIT2);
        wd.exponent = getValue(b, BIT3 | BIT4 | BIT5 | BIT6 | BIT7);

        return wd;
    }

}
