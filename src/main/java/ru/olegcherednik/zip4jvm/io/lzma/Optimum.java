package ru.olegcherednik.zip4jvm.io.lzma;

/**
 * @author Oleg Cherednik
 * @since 14.02.2020
 */
final class Optimum {

    private static final int INFINITY_PRICE = 1 << 30;

    final State state = new State();
    final int[] reps = new int[4];

    /** Cumulative price of arriving to this byte. */
    int price;

    int optPrev;
    int backPrev;
    boolean prev1IsLiteral;

    boolean hasPrev2;
    int optPrev2;
    int backPrev2;

    void reset() {
        price = INFINITY_PRICE;
    }

    void set1(int newPrice, int optCur, int back) {
        price = newPrice;
        optPrev = optCur;
        backPrev = back;
        prev1IsLiteral = false;
    }

    /**
     * Sets to indicate two LZMA symbols of which the first one is a literal.
     */
    void set2(int newPrice, int optCur, int back) {
        price = newPrice;
        optPrev = optCur + 1;
        backPrev = back;
        prev1IsLiteral = true;
        hasPrev2 = false;
    }

    /**
     * Sets to indicate three LZMA symbols of which the second one
     * is a literal.
     */
    void set3(int newPrice, int optCur, int back2, int len2, int back) {
        price = newPrice;
        optPrev = optCur + len2 + 1;
        backPrev = back;
        prev1IsLiteral = true;
        hasPrev2 = true;
        optPrev2 = optCur;
        backPrev2 = back2;
    }
}
