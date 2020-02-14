package ru.olegcherednik.zip4jvm.io.lzma;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

import static ru.olegcherednik.zip4jvm.io.lzma.rangecoder.RangeCoder.PROB_INIT;

public abstract class LzmaCoder implements Closeable {

    static final int POS_STATES_MAX = 1 << 4;

    static final int MATCH_LEN_MIN = 2;
    public static final int MATCH_LEN_MAX = MATCH_LEN_MIN + LengthCoder.LOW_SYMBOLS
            + LengthCoder.MID_SYMBOLS
            + LengthCoder.HIGH_SYMBOLS - 1;

    static final int DIST_STATES = 4;
    static final int DIST_SLOTS = 1 << 6;
    static final int DIST_MODEL_START = 4;
    static final int DIST_MODEL_END = 14;
    static final int FULL_DISTANCES = 1 << (DIST_MODEL_END / 2);

    static final int ALIGN_BITS = 4;
    static final int ALIGN_SIZE = 1 << ALIGN_BITS;
    static final int ALIGN_MASK = ALIGN_SIZE - 1;

    final int posMask;

    final int[] reps = new int[4];
    final State state = new State();

    final short[][] isMatch = new short[State.STATES][POS_STATES_MAX];
    final short[] isRep = new short[State.STATES];
    final short[] isRep0 = new short[State.STATES];
    final short[] isRep1 = new short[State.STATES];
    final short[] isRep2 = new short[State.STATES];
    final short[][] isRep0Long = new short[State.STATES][POS_STATES_MAX];
    final short[][] distSlots = new short[DIST_STATES][DIST_SLOTS];
    final short[][] distSpecial = { new short[2], new short[2],
            new short[4], new short[4],
            new short[8], new short[8],
            new short[16], new short[16],
            new short[32], new short[32] };
    final short[] distAlign = new short[ALIGN_SIZE];

    static final int getDistState(int len) {
        return len < DIST_STATES + MATCH_LEN_MIN
               ? len - MATCH_LEN_MIN
               : DIST_STATES - 1;
    }

    protected LzmaCoder(int pb) {
        posMask = (1 << pb) - 1;

        for (int i = 0; i < isMatch.length; ++i)
            initProbs(isMatch[i]);

        initProbs(isRep);
        initProbs(isRep0);
        initProbs(isRep1);
        initProbs(isRep2);

        for (int i = 0; i < isRep0Long.length; ++i)
            initProbs(isRep0Long[i]);

        for (int i = 0; i < distSlots.length; ++i)
            initProbs(distSlots[i]);

        for (int i = 0; i < distSpecial.length; ++i)
            initProbs(distSpecial[i]);

        initProbs(distAlign);
    }

    private static void initProbs(short[] probs) {
        Arrays.fill(probs, PROB_INIT);
    }

    protected static short[] createArray(int size) {
        short[] arr = new short[size];
        initProbs(arr);
        return arr;
    }

    private static short[][] createArray(int rows, int cols) {
        short[][] arr = new short[rows][cols];

        for (short[] line : arr)
            initProbs(line);

        return arr;
    }

    protected abstract static class LiteralCoder {

        private final int lc;
        private final int literalPosMask;

        protected LiteralCoder(int lc, int lp) {
            this.lc = lc;
            literalPosMask = (1 << lp) - 1;
        }

        protected final int getSubCoderIndex(int prvByte, int pos) {
            int low = prvByte >> (8 - lc);
            int high = (pos & literalPosMask) << lc;
            return low + high;
        }
    }

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    protected abstract static class LengthCoder {

        protected static final int LOW_SYMBOLS = 1 << 3;
        protected static final int MID_SYMBOLS = 1 << 3;
        protected static final int HIGH_SYMBOLS = 1 << 8;

        protected final short[] choice = createArray(2);
        protected final short[][] low = createArray(POS_STATES_MAX, LOW_SYMBOLS);
        protected final short[][] mid = createArray(POS_STATES_MAX, MID_SYMBOLS);
        protected final short[] high = createArray(HIGH_SYMBOLS);

    }

    @Override
    public void close() throws IOException {

    }
}
