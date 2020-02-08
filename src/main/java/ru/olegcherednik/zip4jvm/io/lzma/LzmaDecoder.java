package ru.olegcherednik.zip4jvm.io.lzma;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.lzma.range.RangeBitTreeDecoder;
import ru.olegcherednik.zip4jvm.io.lzma.range.RangeDecoder;
import ru.olegcherednik.zip4jvm.io.lzma.slidingwindow.OutWindowNew;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 08.02.2020
 */
public class LzmaDecoder implements Closeable {

    short[] m_IsMatchDecoders = new short[Base.kNumStates << Base.kNumPosStatesBitsMax];
    short[] m_IsRepDecoders = new short[Base.kNumStates];
    short[] m_IsRepG0Decoders = new short[Base.kNumStates];
    short[] m_IsRepG1Decoders = new short[Base.kNumStates];
    short[] m_IsRepG2Decoders = new short[Base.kNumStates];
    short[] m_IsRep0LongDecoders = new short[Base.kNumStates << Base.kNumPosStatesBitsMax];

    RangeBitTreeDecoder[] m_PosSlotDecoder = new RangeBitTreeDecoder[Base.kNumLenToPosStates];
    short[] m_PosDecoders = new short[Base.kNumFullDistances - Base.kEndPosModelIndex];

    RangeBitTreeDecoder m_PosAlignDecoder = new RangeBitTreeDecoder(Base.kNumAlignBits);

    LzmaLenDecoder m_LenDecoder = new LzmaLenDecoder();
    LzmaLenDecoder m_RepLenDecoder = new LzmaLenDecoder();

    LzmaLiteralDecoder m_LiteralDecoder = new LzmaLiteralDecoder();

    int m_DictionarySize = -1;
    int m_DictionarySizeCheck = -1;

    int m_PosStateMask;


    private final RangeDecoder rangeDecoder;
    private final OutWindowNew m_OutWindow;

    private int state = Base.StateInit();
    private int rep0 = 0, rep1 = 0, rep2 = 0, rep3 = 0;
    private long nowPos64;
    private byte prv;
    private long size;

    public LzmaDecoder(DataInput in, LzmaProperties properties, long size) throws IOException {
        rangeDecoder = new RangeDecoder(in);
        m_OutWindow = new OutWindowNew(Math.max(Math.max(properties.getDictionarySize(), 1), 1 << 12));
        this.size = size;

        for (int i = 0; i < Base.kNumLenToPosStates; i++)
            m_PosSlotDecoder[i] = new RangeBitTreeDecoder(Base.kNumPosSlotBits);

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
        RangeDecoder.initBitModels(m_IsMatchDecoders);
        RangeDecoder.initBitModels(m_IsRep0LongDecoders);
        RangeDecoder.initBitModels(m_IsRepDecoders);
        RangeDecoder.initBitModels(m_IsRepG0Decoders);
        RangeDecoder.initBitModels(m_IsRepG1Decoders);
        RangeDecoder.initBitModels(m_IsRepG2Decoders);
        RangeDecoder.initBitModels(m_PosDecoders);

        m_LiteralDecoder.init();

        m_LenDecoder.init();
        m_RepLenDecoder.init();
    }

    private boolean stop;

    public int decode(byte[] buf, int offs, int len) throws IOException {
        int res = 0;

        while (res < len) {
            int cur = m_OutWindow.flush(buf, offs, len - res);
            res += cur;
            offs += cur;

            if (cur == 0 && stop)
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

        int posState = (int)nowPos64 & m_PosStateMask;

        if (rangeDecoder.decodeBit(m_IsMatchDecoders, (state << Base.kNumPosStatesBitsMax) + posState) == 0) {
            LzmaLiteralDecoder.Decoder2 literalDecoder = m_LiteralDecoder.GetDecoder((int)nowPos64, prv);
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
                        rep0 += RangeBitTreeDecoder.ReverseDecode(m_PosDecoders,
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
