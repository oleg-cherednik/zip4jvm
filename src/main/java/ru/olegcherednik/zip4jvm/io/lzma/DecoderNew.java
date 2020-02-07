package ru.olegcherednik.zip4jvm.io.lzma;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.lzma.range.BitTreeDecoderNew;
import ru.olegcherednik.zip4jvm.io.lzma.slidingwindow.OutWindowNew;

import java.io.IOException;

public class DecoderNew implements AutoCloseable {

    class LenDecoder {

        short[] m_Choice = new short[2];
        BitTreeDecoderNew[] m_LowCoder = new BitTreeDecoderNew[Base.kNumPosStatesMax];
        BitTreeDecoderNew[] m_MidCoder = new BitTreeDecoderNew[Base.kNumPosStatesMax];
        BitTreeDecoderNew m_HighCoder = new BitTreeDecoderNew(Base.kNumHighLenBits);
        int m_NumPosStates = 0;

        public void Create(int numPosStates) {
            for (; m_NumPosStates < numPosStates; m_NumPosStates++) {
                m_LowCoder[m_NumPosStates] = new BitTreeDecoderNew(Base.kNumLowLenBits);
                m_MidCoder[m_NumPosStates] = new BitTreeDecoderNew(Base.kNumMidLenBits);
            }
        }

        public void init() {
            ru.olegcherednik.zip4jvm.io.lzma.range.DecoderNew.InitBitModels(m_Choice);
            for (int posState = 0; posState < m_NumPosStates; posState++) {
                m_LowCoder[posState].init();
                m_MidCoder[posState].init();
            }
            m_HighCoder.init();
        }

        public int Decode(ru.olegcherednik.zip4jvm.io.lzma.range.DecoderNew rangeDecoder, int posState) throws IOException {
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

    class LiteralDecoderNew {

        class Decoder2 {

            short[] m_Decoders = new short[0x300];

            public void Init() {
                ru.olegcherednik.zip4jvm.io.lzma.range.DecoderNew.InitBitModels(m_Decoders);
            }

            public byte DecodeNormal(ru.olegcherednik.zip4jvm.io.lzma.range.DecoderNew rangeDecoder) throws IOException {
                int symbol = 1;
                do
                    symbol = (symbol << 1) | rangeDecoder.decodeBit(m_Decoders, symbol);
                while (symbol < 0x100);
                return (byte)symbol;
            }

            public byte DecodeWithMatchByte(ru.olegcherednik.zip4jvm.io.lzma.range.DecoderNew rangeDecoder, byte matchByte) throws IOException {
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

    short[] m_IsMatchDecoders = new short[Base.kNumStates << Base.kNumPosStatesBitsMax];
    short[] m_IsRepDecoders = new short[Base.kNumStates];
    short[] m_IsRepG0Decoders = new short[Base.kNumStates];
    short[] m_IsRepG1Decoders = new short[Base.kNumStates];
    short[] m_IsRepG2Decoders = new short[Base.kNumStates];
    short[] m_IsRep0LongDecoders = new short[Base.kNumStates << Base.kNumPosStatesBitsMax];

    BitTreeDecoderNew[] m_PosSlotDecoder = new BitTreeDecoderNew[Base.kNumLenToPosStates];
    short[] m_PosDecoders = new short[Base.kNumFullDistances - Base.kEndPosModelIndex];

    BitTreeDecoderNew m_PosAlignDecoder = new BitTreeDecoderNew(Base.kNumAlignBits);

    LenDecoder m_LenDecoder = new LenDecoder();
    LenDecoder m_RepLenDecoder = new LenDecoder();

    LiteralDecoderNew m_LiteralDecoder = new LiteralDecoderNew();

    int m_DictionarySize = -1;
    int m_DictionarySizeCheck = -1;

    int m_PosStateMask;


    private final ru.olegcherednik.zip4jvm.io.lzma.range.DecoderNew rangeDecoder;
    private final OutWindowNew m_OutWindow;

    private int state = Base.StateInit();
    private int rep0 = 0, rep1 = 0, rep2 = 0, rep3 = 0;
    private long nowPos64;
    private byte prv;
    private long size;

    public DecoderNew(DataInput in, LzmaProperties properties, long size) throws IOException {
        rangeDecoder = new ru.olegcherednik.zip4jvm.io.lzma.range.DecoderNew(in);
        m_OutWindow = new OutWindowNew(Math.max(Math.max(properties.getDictionarySize(), 1), 1 << 12));
        this.size = size;

        for (int i = 0; i < Base.kNumLenToPosStates; i++)
            m_PosSlotDecoder[i] = new BitTreeDecoderNew(Base.kNumPosSlotBits);

        setLcLpPb(properties.getLc(), properties.getLp(), properties.getPb());
        setDictionarySize(properties.getDictionarySize());

        init();
    }

    private void setLcLpPb(int lc, int lp, int pb) {
        m_LiteralDecoder.Create(lp, lc);
        int numPosStates = 1 << pb;
        m_LenDecoder.Create(numPosStates);
        m_RepLenDecoder.Create(numPosStates);
        m_PosStateMask = numPosStates - 1;
    }

    private void setDictionarySize(int dictionarySize) {
        m_DictionarySize = dictionarySize;
        m_DictionarySizeCheck = Math.max(m_DictionarySize, 1);
    }

    private void init() {
        ru.olegcherednik.zip4jvm.io.lzma.range.DecoderNew.InitBitModels(m_IsMatchDecoders);
        ru.olegcherednik.zip4jvm.io.lzma.range.DecoderNew.InitBitModels(m_IsRep0LongDecoders);
        ru.olegcherednik.zip4jvm.io.lzma.range.DecoderNew.InitBitModels(m_IsRepDecoders);
        ru.olegcherednik.zip4jvm.io.lzma.range.DecoderNew.InitBitModels(m_IsRepG0Decoders);
        ru.olegcherednik.zip4jvm.io.lzma.range.DecoderNew.InitBitModels(m_IsRepG1Decoders);
        ru.olegcherednik.zip4jvm.io.lzma.range.DecoderNew.InitBitModels(m_IsRepG2Decoders);
        ru.olegcherednik.zip4jvm.io.lzma.range.DecoderNew.InitBitModels(m_PosDecoders);

        m_LiteralDecoder.init();

        for (int i = 0; i < Base.kNumLenToPosStates; i++)
            m_PosSlotDecoder[i].init();

        m_LenDecoder.init();
        m_RepLenDecoder.init();
        m_PosAlignDecoder.init();
    }

    private final byte[] buf = new byte[1024];

    private boolean stop;

    public int decode(byte[] buf, int offs, int len) throws IOException {
        int res = 0;

        while (res < len) {
            int cur = m_OutWindow.flush(buf, offs, len - res);
            res += cur;
            offs += cur;

            if(cur == 0 && stop)
                return -1;

            if (decodeStep()) {
                stop = true;
                res += m_OutWindow.flush(buf, offs, len - res);
                break;
            }
        }

        return res;
    }

    private boolean decodeStep() throws IOException {
        if (nowPos64 >= size || stop)
            return true;

//        System.out.println(nowPos64);
        int posState = (int)nowPos64 & m_PosStateMask;

        if (rangeDecoder.decodeBit(m_IsMatchDecoders, (state << Base.kNumPosStatesBitsMax) + posState) == 0) {
            LiteralDecoderNew.Decoder2 literalDecoder = m_LiteralDecoder.GetDecoder((int)nowPos64, prv);
            prv = Base.StateIsCharState(state) ? literalDecoder.DecodeNormal(rangeDecoder)
                                               : literalDecoder.DecodeWithMatchByte(rangeDecoder, m_OutWindow.get(rep0));

            m_OutWindow.writeByte(prv);
            state = Base.StateUpdateChar(state);
            nowPos64++;
        } else {
            int len;

            if (rangeDecoder.decodeBit(m_IsRepDecoders, state) == 1) {
                len = 0;
                if (rangeDecoder.decodeBit(m_IsRepG0Decoders, state) == 0) {
                    if (rangeDecoder.decodeBit(m_IsRep0LongDecoders, (state << Base.kNumPosStatesBitsMax) + posState) == 0) {
                        state = Base.StateUpdateShortRep(state);
                        len = 1;
                    }
                } else {
                    int distance;
                    if (rangeDecoder.decodeBit(m_IsRepG1Decoders, state) == 0)
                        distance = rep1;
                    else {
                        if (rangeDecoder.decodeBit(m_IsRepG2Decoders, state) == 0)
                            distance = rep2;
                        else {
                            distance = rep3;
                            rep3 = rep2;
                        }
                        rep2 = rep1;
                    }
                    rep1 = rep0;
                    rep0 = distance;
                }
                if (len == 0) {
                    len = m_RepLenDecoder.Decode(rangeDecoder, posState) + Base.kMatchMinLen;
                    state = Base.StateUpdateRep(state);
                }
            } else {
                rep3 = rep2;
                rep2 = rep1;
                rep1 = rep0;
                len = Base.kMatchMinLen + m_LenDecoder.Decode(rangeDecoder, posState);
                state = Base.StateUpdateMatch(state);
                int posSlot = m_PosSlotDecoder[Base.GetLenToPosState(len)].Decode(rangeDecoder);

                if (posSlot >= Base.kStartPosModelIndex) {
                    int numDirectBits = (posSlot >> 1) - 1;
                    rep0 = (2 | (posSlot & 1)) << numDirectBits;

                    if (posSlot < Base.kEndPosModelIndex)
                        rep0 += BitTreeDecoderNew.ReverseDecode(m_PosDecoders,
                                rep0 - posSlot - 1, rangeDecoder, numDirectBits);
                    else {
                        rep0 += rangeDecoder.decodeDirectBits(numDirectBits - Base.kNumAlignBits) << Base.kNumAlignBits;
                        rep0 += m_PosAlignDecoder.ReverseDecode(rangeDecoder);
                        if (rep0 < 0) {
                            if (rep0 == -1)
                                return true;
                            throw new IOException("Error in data stream");
                        }
                    }
                } else
                    rep0 = posSlot;
            }

            if (rep0 >= nowPos64 || rep0 >= m_DictionarySizeCheck)
                throw new IOException("Error in data stream");

            m_OutWindow.writeBlock(rep0, len);
            nowPos64 += len;
            prv = m_OutWindow.get(0);
        }

        return false;
    }

    @Override
    public void close() throws IOException {
//        flush();
    }
}
