package ru.olegcherednik.zip4jvm.io.lzma;

import ru.olegcherednik.zip4jvm.io.lzma.range.RangeDecoder;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 08.02.2020
 */
class LzmaLiteralDecoder {

    class Decoder2 {

        short[] m_Decoders = new short[0x300];

        public void Init() {
            RangeDecoder.initBitModels(m_Decoders);
        }

        public byte DecodeNormal(RangeDecoder rangeDecoder) throws IOException {
            int symbol = 1;
            do
                symbol = (symbol << 1) | rangeDecoder.decodeBit(m_Decoders, symbol);
            while (symbol < 0x100);
            return (byte)symbol;
        }

        public byte DecodeWithMatchByte(RangeDecoder rangeDecoder, byte matchByte) throws IOException {
            int symbol = 1;
            do {
                int matchBit = (matchByte >> 7) & 1;
                matchByte <<= 1;
                int bit = rangeDecoder.decodeBit(m_Decoders, ((1 + matchBit) << 8) + symbol);
                symbol = (symbol << 1) | bit;
                if (matchBit != bit) {
                    while (symbol < 0x100)
                        symbol = (symbol << 1) | rangeDecoder.decodeBit(m_Decoders, symbol);
                    break;
                }
            }
            while (symbol < 0x100);
            return (byte)symbol;
        }
    }

    Decoder2[] m_Coders;
    int m_NumPrevBits;
    int m_NumPosBits;
    int m_PosMask;

    public void Create(int numPosBits, int numPrevBits) {
        if (m_Coders != null && m_NumPrevBits == numPrevBits && m_NumPosBits == numPosBits)
            return;
        m_NumPosBits = numPosBits;
        m_PosMask = (1 << numPosBits) - 1;
        m_NumPrevBits = numPrevBits;
        int numStates = 1 << (m_NumPrevBits + m_NumPosBits);
        m_Coders = new Decoder2[numStates];
        for (int i = 0; i < numStates; i++)
            m_Coders[i] = new Decoder2();
    }

    public void init() {
        int numStates = 1 << (m_NumPrevBits + m_NumPosBits);
        for (int i = 0; i < numStates; i++)
            m_Coders[i].Init();
    }

    Decoder2 GetDecoder(int pos, byte prevByte) {
        return m_Coders[((pos & m_PosMask) << m_NumPrevBits) + ((prevByte & 0xFF) >>> (8 - m_NumPrevBits))];
    }
}
