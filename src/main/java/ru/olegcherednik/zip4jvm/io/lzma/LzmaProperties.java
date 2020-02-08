package ru.olegcherednik.zip4jvm.io.lzma;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 05.02.2020
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LzmaProperties {

    public static final int SIZE = 5;

    private final int lc; // literal context bits
    private final int lp; // literal position bits
    private final int pb; // position bits
    private final int dictionarySize;

    public static LzmaProperties read(DataInput in) throws IOException {
        int v = in.readByte() & 0xFF;
        int lc = v % 9;
        int lp = (v / 9) % 5;
        int pb = v / (9 * 5);

        if (pb > Base.kNumPosStatesBitsMax)
            throw new IllegalArgumentException("Incorrect properties");

        int dictionarySize = (int)in.readDword();

        if (dictionarySize < 0)
            throw new IllegalArgumentException("Incorrect stream properties");

        return new LzmaProperties(lc, lp, pb, dictionarySize);
    }

}
