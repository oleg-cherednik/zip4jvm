package ru.olegcherednik.zip4jvm.io.lzma.range;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 08.02.2020
 */
public class RangeDecoder {

    public static final int TOP_MASK = -(1 << 24);

    private static final int NUM_BIT_MODEL_TOTAL_BITS = 11;
    private static final int BIT_MODEL_TOTAL = 1 << NUM_BIT_MODEL_TOTAL_BITS;
    private static final int NUM_MOVE_BITS = 5;

    private final DataInput in;
    private int code;
    private int range = -1;

    public RangeDecoder(DataInput in) throws IOException {
        this.in = in;

        for (int i = 0; i < NUM_MOVE_BITS; i++)
            code = (code << 8) | in.readByte();
    }

    public int decodeDirectBits(int numTotalBits) throws IOException {
        int res = 0;

        for (int i = numTotalBits; i > 0; i--) {
            range >>>= 1;
            int t = (code - range) >>> 31;
            code -= range & (t - 1);
            res = (res << 1) | (1 - t);

            if ((range & TOP_MASK) == 0) {
                code = (code << 8) | in.readByte();
                range <<= 8;
            }
        }

        return res;
    }

    public int decodeBit(short[] probs, int i) throws IOException {
        int v = probs[i];
        int bound = (range >>> NUM_BIT_MODEL_TOTAL_BITS) * v;

        if ((code ^ 0x80000000) < (bound ^ 0x80000000)) {
            range = bound;
            probs[i] = (short)(v + ((BIT_MODEL_TOTAL - v) >>> NUM_MOVE_BITS));

            if ((range & TOP_MASK) == 0) {
                code = (code << 8) | in.readByte();
                range <<= 8;
            }

            return 0;
        }

        range -= bound;
        code -= bound;
        probs[i] = (short)(v - (v >>> NUM_MOVE_BITS));

        if ((range & TOP_MASK) == 0) {
            code = (code << 8) | in.readByte();
            range <<= 8;
        }

        return 1;
    }

    public static void initBitModels(short... bits) {
        Arrays.fill(bits, (short)(BIT_MODEL_TOTAL >>> 1));
    }

    public static short[] createBitModel(int size) {
        short[] bits = new short[size];
        Arrays.fill(bits, (short)(BIT_MODEL_TOTAL >>> 1));
        return bits;
    }
}
