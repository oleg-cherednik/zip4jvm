package ru.olegcherednik.zip4jvm.io.lzma.range;

public class RangeBitTreeDecoder {

    private final short[] models;
    private final int numBitLevels;

    public RangeBitTreeDecoder(int numBitLevels) {
        this.numBitLevels = numBitLevels;
        models = RangeDecoder.createBitModel(1 << numBitLevels);
    }

    public int Decode(RangeDecoder rangeDecoder) throws java.io.IOException {
        int m = 1;
        for (int bitIndex = numBitLevels; bitIndex != 0; bitIndex--)
            m = (m << 1) + rangeDecoder.decodeBit(models, m);
        return m - (1 << numBitLevels);
    }

    public int ReverseDecode(RangeDecoder rangeDecoder) throws java.io.IOException {
        int m = 1;
        int symbol = 0;
        for (int bitIndex = 0; bitIndex < numBitLevels; bitIndex++) {
            int bit = rangeDecoder.decodeBit(models, m);
            m <<= 1;
            m += bit;
            symbol |= (bit << bitIndex);
        }
        return symbol;
    }

    public static int ReverseDecode(short[] Models, int startIndex,
            RangeDecoder rangeDecoder, int NumBitLevels) throws java.io.IOException {
        int m = 1;
        int symbol = 0;
        for (int bitIndex = 0; bitIndex < NumBitLevels; bitIndex++) {
            int bit = rangeDecoder.decodeBit(Models, startIndex + m);
            m <<= 1;
            m += bit;
            symbol |= (bit << bitIndex);
        }
        return symbol;
    }
}
