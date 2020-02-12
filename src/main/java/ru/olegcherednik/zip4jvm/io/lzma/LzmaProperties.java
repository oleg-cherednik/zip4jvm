package ru.olegcherednik.zip4jvm.io.lzma;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 05.02.2020
 */
@Getter
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Deprecated
// TODO it could be used in LZMA
public final class LzmaProperties {

    private final int lc; // literal context bits
    private final int lp; // literal position bits
    private final int pb; // position bits
    private final int dictionarySize;

    public int write(DataOutput out) throws IOException {
        out.writeByte((byte)((pb * 5 + lp) * 9 + lc));
        out.writeDword(dictionarySize);
        return 5;
    }

    public static LzmaProperties read(DataInput in) throws IOException {
        int v = in.readByte() & 0xFF;
        int lc = v % 9;
        int lp = (v / 9) % 5;
        int pb = v / (9 * 5);

        int dictionarySize = (int)in.readDword();

        if (dictionarySize < 0)
            throw new IllegalArgumentException("Incorrect stream properties");

        return new LzmaProperties(lc, lp, pb, Math.max(4096, dictionarySize));
    }

}
