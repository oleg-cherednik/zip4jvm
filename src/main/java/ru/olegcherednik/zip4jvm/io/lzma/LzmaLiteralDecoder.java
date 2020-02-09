package ru.olegcherednik.zip4jvm.io.lzma;

import ru.olegcherednik.zip4jvm.io.lzma.range.RangeDecoder;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 08.02.2020
 */
class LzmaLiteralDecoder {

    private Decoder2[] m_Coders;
    int m_NumPrevBits;
    int m_NumPosBits;
    int m_PosMask;

    public void create(int lp, int lc) {
        if (m_Coders != null && m_NumPrevBits == lc && m_NumPosBits == lp)
            return;

        m_NumPosBits = lp;
        m_PosMask = (1 << lp) - 1;
        m_NumPrevBits = lc;

        m_Coders = new Decoder2[1 << (m_NumPrevBits + m_NumPosBits)];

        for (int i = 0; i < m_Coders.length; i++)
            m_Coders[i] = new Decoder2();
    }

    Decoder2 GetDecoder(int pos, byte prevByte) {
        return m_Coders[((pos & m_PosMask) << m_NumPrevBits) + ((prevByte & 0xFF) >>> (8 - m_NumPrevBits))];
    }

    static class Decoder2 {

        private final short[] decoders = RangeDecoder.createBitModel(0x300);

        public byte decodeNormal(RangeDecoder rangeDecoder) throws IOException {
            int symbol = 1;

            do {
                symbol = (symbol << 1) | rangeDecoder.decodeBit(decoders, symbol);
            } while (symbol < 0x100);

            return (byte)symbol;
        }

        public byte decodeWithMatchByte(RangeDecoder rangeDecoder, byte matchByte) throws IOException {
            int symbol = 1;

            do {
                int matchBit = (matchByte >> 7) & 1;
                matchByte <<= 1;

                int bit = rangeDecoder.decodeBit(decoders, ((1 + matchBit) << 8) + symbol);
                symbol = (symbol << 1) | bit;

                if (matchBit == bit)
                    continue;

                while (symbol < 0x100) {
                    symbol = (symbol << 1) | rangeDecoder.decodeBit(decoders, symbol);
                }

                break;
            } while (symbol < 0x100);

            return (byte)symbol;
        }
    }
}
