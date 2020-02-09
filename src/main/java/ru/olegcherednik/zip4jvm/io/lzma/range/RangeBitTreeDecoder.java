package ru.olegcherednik.zip4jvm.io.lzma.range;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 08.02.2020
 */
public class RangeBitTreeDecoder {

    private final short[] models;
    private final int numBitLevel;

    public RangeBitTreeDecoder(int numBitLevel) {
        this.numBitLevel = numBitLevel;
        models = RangeDecoder.createBitModel(1 << numBitLevel);
    }

    public int decode(RangeDecoder rangeDecoder) throws IOException {
        int m = 1;

        for (int i = numBitLevel; i > 0; i--)
            m = (m << 1) + rangeDecoder.decodeBit(models, m);

        return m - (1 << numBitLevel);
    }

    public int reverseDecode(RangeDecoder rangeDecoder) throws IOException {
        int symbol = 0;

        for (int i = 0, m = 1; i < numBitLevel; i++) {
            int bit = rangeDecoder.decodeBit(models, m);
            m = (m << 1) + bit;
            symbol |= bit << i;
        }

        return symbol;
    }

    public static int reverseDecode(short[] models, int startIndex, RangeDecoder rangeDecoder, int numBitLevel) throws IOException {
        int symbol = 0;

        for (int i = 0, m = 1; i < numBitLevel; i++) {
            int bit = rangeDecoder.decodeBit(models, startIndex + m);
            m = (m << 1) + bit;
            symbol |= bit << i;
        }

        return symbol;
    }
}
