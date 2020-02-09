package ru.olegcherednik.zip4jvm.io.lzma;

import ru.olegcherednik.zip4jvm.io.lzma.range.RangeDecoder;

import java.io.IOException;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 08.02.2020
 */
class LzmaLiteralDecoder {

    private final int posMask;
    private final Decoder[] decoders;
    private final int numPrevBits;

    public LzmaLiteralDecoder(int lp, int lc) {
        posMask = (1 << lp) - 1;
        numPrevBits = lc;
        decoders = IntStream.range(0, 1 << (lc + lp))
                            .mapToObj(i -> new Decoder())
                            .toArray(Decoder[]::new);
    }

    Decoder getDecoder(int pos, byte prevByte) {
        return decoders[((pos & posMask) << numPrevBits) + ((prevByte & 0xFF) >>> (8 - numPrevBits))];
    }

    public static class Decoder {

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
