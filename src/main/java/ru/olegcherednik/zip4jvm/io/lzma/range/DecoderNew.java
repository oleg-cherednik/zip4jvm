package ru.olegcherednik.zip4jvm.io.lzma.range;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;
import java.util.Arrays;

public class DecoderNew {

    static final int kTopMask = ~((1 << 24) - 1);

    private static final int kNumBitModelTotalBits = 11;
    private static final int kBitModelTotal = 1 << kNumBitModelTotalBits;
    private static final int kNumMoveBits = 5;

    private final DataInput in;
    private int code;
    private int range = -1;

    public DecoderNew(DataInput in) throws IOException {
        this.in = in;

        for (int i = 0; i < 5; i++)
            code = (code << 8) | in.readByte();
    }

    public final int decodeDirectBits(int numTotalBits) throws IOException {
        int result = 0;
        for (int i = numTotalBits; i != 0; i--) {
            range >>>= 1;
            int t = ((code - range) >>> 31);
            code -= range & (t - 1);
            result = (result << 1) | (1 - t);

            if ((range & kTopMask) == 0) {
                code = (code << 8) | in.readByte();
                range <<= 8;
            }
        }
        return result;
    }

    public int decodeBit(short[] probs, int index) throws IOException {
        int v = probs[index];
        int bound = (range >>> kNumBitModelTotalBits) * v;

        if ((code ^ 0x80000000) < (bound ^ 0x80000000)) {
            range = bound;
            probs[index] = (short)(v + ((kBitModelTotal - v) >>> kNumMoveBits));

            if ((range & kTopMask) == 0) {
                code = (code << 8) | in.readByte();
                range <<= 8;
            }

            return 0;
        }

        range -= bound;
        code -= bound;
        probs[index] = (short)(v - (v >>> kNumMoveBits));

        if ((range & kTopMask) == 0) {
            code = (code << 8) | in.readByte();
            range <<= 8;
        }

        return 1;
    }

    public static void InitBitModels(short[] probs) {
        Arrays.fill(probs, (short)(kBitModelTotal >>> 1));
    }
}
