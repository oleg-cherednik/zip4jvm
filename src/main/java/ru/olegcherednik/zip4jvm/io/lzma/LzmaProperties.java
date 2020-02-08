package ru.olegcherednik.zip4jvm.io.lzma;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 05.02.2020
 */
public final class LzmaProperties {

    public static final int SIZE = 5;

    private int lc; // literal context bits
    private int lp; // literal position bits
    private int pb; // position bits
    private int dictionarySize;

    public int getLc() {
        return lc;
    }

    public int getLp() {
        return lp;
    }

    public int getPb() {
        return pb;
    }

    public int getDictionarySize() {
        return dictionarySize;
    }

    public void read(DataInput in) throws IOException {
        int v = in.readByte() & 0xFF;
        lc = v % 9;
        lp = (v / 9) % 5;
        pb = v / (9 * 5);

        if (pb > Base.kNumPosStatesBitsMax)
            throw new IllegalArgumentException("Incorrect properties");

        dictionarySize = (int)in.readDword();

        if (dictionarySize < 0)
            throw new IllegalArgumentException("Incorrect stream properties");
    }

}
