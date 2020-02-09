package ru.olegcherednik.zip4jvm.io.lzma;

import ru.olegcherednik.zip4jvm.io.lzma.range.RangeBitTreeDecoder;
import ru.olegcherednik.zip4jvm.io.lzma.range.RangeDecoder;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 08.02.2020
 */
class LzmaLengthDecoder {

    private final short[] choice = RangeDecoder.createBitModel(2);
    private final RangeBitTreeDecoder[] lowCoder = new RangeBitTreeDecoder[Base.kNumPosStatesMax];
    private final RangeBitTreeDecoder[] midCoder = new RangeBitTreeDecoder[Base.kNumPosStatesMax];
    private final RangeBitTreeDecoder highCoder = new RangeBitTreeDecoder(Base.kNumHighLenBits);
    private int numPosStates;

    public void create(int numPosStates) {
        for (; this.numPosStates < numPosStates; this.numPosStates++) {
            lowCoder[this.numPosStates] = new RangeBitTreeDecoder(Base.kNumLowLenBits);
            midCoder[this.numPosStates] = new RangeBitTreeDecoder(Base.kNumMidLenBits);
        }
    }

    public int decode(RangeDecoder rangeDecoder, int posState) throws IOException {
        if (rangeDecoder.decodeBit(choice, 0) == 0)
            return lowCoder[posState].decode(rangeDecoder);

        int symbol = Base.kNumLowLenSymbols;

        if (rangeDecoder.decodeBit(choice, 1) == 0)
            symbol += midCoder[posState].decode(rangeDecoder);
        else
            symbol += Base.kNumMidLenSymbols + highCoder.decode(rangeDecoder);

        return symbol;
    }
}
