package ru.olegcherednik.zip4jvm.io.lzma.xz;

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
public final class LzmaProperties {

    /**
     * The largest dictionary size supported by this implementation.
     * <p>
     * LZMA allows dictionaries up to one byte less than 4 GiB. This implementation supports only 16 bytes less than 2 GiB. This limitation is due
     * to Java using signed 32-bit integers for array indexing. The limitation shouldn't matter much in practice since so huge dictionaries are not
     * normally used.
     */
    public static final int DICTIONARY_SIZE_MAX = Integer.MAX_VALUE & ~15;
    public static final int DICTIONARY_SIZE_MIN = 4096;

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

        checkDictionarySize(dictionarySize);

        return new LzmaProperties(lc, lp, pb, dictionarySizeInRange(dictionarySize));
    }

    private static void checkDictionarySize(int dictionarySize) {
        if (dictionarySize < 0)
            throw new IllegalArgumentException("Incorrect LZMA dictionary size: " + dictionarySize);
        if (dictionarySize > DICTIONARY_SIZE_MAX)
            throw new IllegalArgumentException("Incorrect LZMA dictionary size is too big for this implementation: " + dictionarySize);
    }

    private static int dictionarySizeInRange(int dictionarySize) {
        return Math.max(DICTIONARY_SIZE_MIN, Math.min(dictionarySize, DICTIONARY_SIZE_MAX));
    }

}
