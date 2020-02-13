package ru.olegcherednik.zip4jvm.io.lzma.xz.rangecoder;

import java.io.Closeable;
import java.util.Arrays;

public abstract class RangeCoder implements Closeable {

    static final int SHIFT_BITS = 8;
    static final int TOP_MASK = 0xFF000000;
    static final int BIT_MODEL_TOTAL_BITS = 11;
    static final int BIT_MODEL_TOTAL = 1 << BIT_MODEL_TOTAL_BITS;
    static final short PROB_INIT = (short)(BIT_MODEL_TOTAL / 2);
    static final int MOVE_BITS = 5;

    public static void initProbs(short[] probs) {
        Arrays.fill(probs, PROB_INIT);
    }

}
