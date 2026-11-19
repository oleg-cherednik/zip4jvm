package io.airlift.compress.zstd.model;

import io.airlift.compress.zstd.In;
import lombok.Getter;

import static io.airlift.compress.zstd.model.Constants.MIN_WINDOW_LOG;
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

    public long getWindowSize() {
        long base = 1L << (MIN_WINDOW_LOG + exponent);
        return base + (base / 8) * mantissa;
    }

    public void withWindowSize(int windowSize) {
        int base = Integer.highestOneBit(windowSize);

        exponent = 32 - Integer.numberOfLeadingZeros(base) - 1;

        if (exponent < MIN_WINDOW_LOG)
            throw new IllegalArgumentException("Minimum window size is " + (1 << MIN_WINDOW_LOG));

        int remainder = windowSize - base;

        if (remainder % (base / 8) != 0)
            throw new IllegalArgumentException(
                    "Window size of magnitude 2^" + exponent + " must be multiple of " + (base / 8));

        // mantissa is guaranteed to be between 0-7
        mantissa = remainder / (base / 8);
    }

}
