package ru.olegcherednik.zip4jvm.io.lzma;

import ru.olegcherednik.zip4jvm.io.lzma.range.RangeBitTreeDecoder;
import ru.olegcherednik.zip4jvm.io.lzma.range.RangeDecoder;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 08.02.2020
 */
class LzmaLenDecoder {

    short[] m_Choice = new short[2];
    RangeBitTreeDecoder[] m_LowCoder = new RangeBitTreeDecoder[Base.kNumPosStatesMax];
    RangeBitTreeDecoder[] m_MidCoder = new RangeBitTreeDecoder[Base.kNumPosStatesMax];
    RangeBitTreeDecoder m_HighCoder = new RangeBitTreeDecoder(Base.kNumHighLenBits);
    int m_NumPosStates = 0;

    public void Create(int numPosStates) {
        for (; m_NumPosStates < numPosStates; m_NumPosStates++) {
            m_LowCoder[m_NumPosStates] = new RangeBitTreeDecoder(Base.kNumLowLenBits);
            m_MidCoder[m_NumPosStates] = new RangeBitTreeDecoder(Base.kNumMidLenBits);
        }
    }

    public void init() {
        RangeDecoder.initBitModels(m_Choice);
    }

    public int Decode(RangeDecoder rangeDecoder, int posState) throws IOException {
        if (rangeDecoder.decodeBit(m_Choice, 0) == 0)
            return m_LowCoder[posState].Decode(rangeDecoder);
        int symbol = Base.kNumLowLenSymbols;
        if (rangeDecoder.decodeBit(m_Choice, 1) == 0)
            symbol += m_MidCoder[posState].Decode(rangeDecoder);
        else
            symbol += Base.kNumMidLenSymbols + m_HighCoder.Decode(rangeDecoder);
        return symbol;
    }
}
