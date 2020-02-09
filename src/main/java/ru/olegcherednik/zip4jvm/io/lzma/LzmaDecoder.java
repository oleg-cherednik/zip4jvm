package ru.olegcherednik.zip4jvm.io.lzma;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.lzma.range.RangeBitTreeDecoder;
import ru.olegcherednik.zip4jvm.io.lzma.range.RangeDecoder;
import ru.olegcherednik.zip4jvm.io.lzma.slidingwindow.OutWindow;

import java.io.Closeable;
import java.io.IOException;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 08.02.2020
 */
public class LzmaDecoder implements Closeable {

    private final short[] matchDecoders = RangeDecoder.createBitModel(Base.kNumStates << Base.kNumPosStatesBitsMax);
    private final short[] repDecoders = RangeDecoder.createBitModel(Base.kNumStates);
    private final short[] repG0Decoders = RangeDecoder.createBitModel(Base.kNumStates);
    private final short[] repG1Decoders = RangeDecoder.createBitModel(Base.kNumStates);
    private final short[] repG2Decoders = RangeDecoder.createBitModel(Base.kNumStates);
    private final short[] rep0LongDecoders = RangeDecoder.createBitModel(Base.kNumStates << Base.kNumPosStatesBitsMax);
    private final short[] posDecoders = RangeDecoder.createBitModel(Base.kNumFullDistances - Base.kEndPosModelIndex);

    private final RangeBitTreeDecoder[] posSlotDecoders = IntStream.range(0, Base.kNumLenToPosStates)
                                                                   .mapToObj(i -> new RangeBitTreeDecoder(Base.kNumPosSlotBits))
                                                                   .toArray(RangeBitTreeDecoder[]::new);

    private final RangeDecoder rangeDecoder;
    private final OutWindow outWindow;
    private final long size;

    RangeBitTreeDecoder m_PosAlignDecoder = new RangeBitTreeDecoder(Base.kNumAlignBits);
    LzmaLengthDecoder m_LenDecoder = new LzmaLengthDecoder();
    LzmaLengthDecoder m_RepLenDecoder = new LzmaLengthDecoder();

    private LzmaLiteralDecoder literalDecoder = new LzmaLiteralDecoder();

    int m_DictionarySize = -1;
    int m_DictionarySizeCheck = -1;

    int m_PosStateMask;

    private int state = Base.StateInit();
    private int rep0 = 0, rep1 = 0, rep2 = 0, rep3 = 0;
    private long nowPos64;
    private byte prv;

    public LzmaDecoder(DataInput in, LzmaProperties properties, long size) throws IOException {
        this.size = size;
        rangeDecoder = new RangeDecoder(in);
        outWindow = new OutWindow(properties.getDictionarySize());

        setLcLpPb(properties.getLc(), properties.getLp(), properties.getPb());
        setDictionarySize(properties.getDictionarySize());
    }

    private void setLcLpPb(int lc, int lp, int pb) {
        literalDecoder.create(lp, lc);
        int numPosStates = 1 << pb;
        m_LenDecoder.create(numPosStates);
        m_RepLenDecoder.create(numPosStates);
        m_PosStateMask = numPosStates - 1;
    }

    private void setDictionarySize(int dictionarySize) {
        m_DictionarySize = dictionarySize;
        m_DictionarySizeCheck = Math.max(m_DictionarySize, 1);
    }

    private boolean stop;

    public int decode(byte[] buf, int offs, int len) throws IOException {
        int res = 0;

        while (res < len) {
            int cur = outWindow.flush(buf, offs, len - res);
            res += cur;
            offs += cur;

            if (cur == 0 && stop)
                return -1;

            if (decodeStep()) {
                stop = true;
                res += outWindow.flush(buf, offs, len - res);
                break;
            }
        }

        return res;
    }

    private boolean decodeStep() throws IOException {
        if (nowPos64 >= size || stop)
            return true;

        int posState = (int)nowPos64 & m_PosStateMask;

        if (rangeDecoder.decodeBit(matchDecoders, (state << Base.kNumPosStatesBitsMax) + posState) == 0) {
            LzmaLiteralDecoder.Decoder2 literalDecoder = this.literalDecoder.GetDecoder((int)nowPos64, prv);
            prv = Base.StateIsCharState(state) ? literalDecoder.decodeNormal(rangeDecoder)
                                               : literalDecoder.decodeWithMatchByte(rangeDecoder, outWindow.get(rep0));

            outWindow.writeByte(prv);
            state = Base.StateUpdateChar(state);
            nowPos64++;
        } else {
            int len;

            if (rangeDecoder.decodeBit(repDecoders, state) == 1) {
                len = 0;
                if (rangeDecoder.decodeBit(repG0Decoders, state) == 0) {
                    if (rangeDecoder.decodeBit(rep0LongDecoders, (state << Base.kNumPosStatesBitsMax) + posState) == 0) {
                        state = Base.StateUpdateShortRep(state);
                        len = 1;
                    }
                } else {
                    int distance;
                    if (rangeDecoder.decodeBit(repG1Decoders, state) == 0)
                        distance = rep1;
                    else {
                        if (rangeDecoder.decodeBit(repG2Decoders, state) == 0)
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
                    len = m_RepLenDecoder.decode(rangeDecoder, posState) + Base.kMatchMinLen;
                    state = Base.StateUpdateRep(state);
                }
            } else {
                rep3 = rep2;
                rep2 = rep1;
                rep1 = rep0;
                len = Base.kMatchMinLen + m_LenDecoder.decode(rangeDecoder, posState);
                state = Base.StateUpdateMatch(state);
                int posSlot = posSlotDecoders[Base.GetLenToPosState(len)].decode(rangeDecoder);

                if (posSlot >= Base.kStartPosModelIndex) {
                    int numDirectBits = (posSlot >> 1) - 1;
                    rep0 = (2 | (posSlot & 1)) << numDirectBits;

                    if (posSlot < Base.kEndPosModelIndex)
                        rep0 += RangeBitTreeDecoder.reverseDecode(posDecoders,
                                rep0 - posSlot - 1, rangeDecoder, numDirectBits);
                    else {
                        rep0 += rangeDecoder.decodeDirectBits(numDirectBits - Base.kNumAlignBits) << Base.kNumAlignBits;
                        rep0 += m_PosAlignDecoder.reverseDecode(rangeDecoder);
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

            outWindow.writeBlock(rep0, len);
            nowPos64 += len;
            prv = outWindow.get(0);
        }

        return false;
    }

    @Override
    public void close() throws IOException {
//        flush();
    }
}
