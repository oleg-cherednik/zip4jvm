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
    private final LzmaLiteralDecoder literalDecoder;
    private final LzmaLengthDecoder lengthDecoder;
    private final LzmaLengthDecoder repLenDecoder;

    private final RangeDecoder rangeDecoder;
    private final OutWindow outWindow;
    private final long size;
    private final int[] rep = new int[4];

    RangeBitTreeDecoder m_PosAlignDecoder = new RangeBitTreeDecoder(Base.kNumAlignBits);





    int m_DictionarySize = -1;
    int m_DictionarySizeCheck = -1;

    int m_PosStateMask;

    private int state = Base.StateInit();

    private long nowPos64;
    private byte prv;
    private boolean stop;

    public LzmaDecoder(DataInput in, LzmaProperties properties, long size) throws IOException {
        this.size = size;
        rangeDecoder = new RangeDecoder(in);
        outWindow = new OutWindow(properties.getDictionarySize());
        literalDecoder = new LzmaLiteralDecoder(properties.getLp(), properties.getLc());
        lengthDecoder = new LzmaLengthDecoder(properties.getPb());
        repLenDecoder = new LzmaLengthDecoder(properties.getPb());

        setLcLpPb(properties.getLc(), properties.getLp(), properties.getPb());
        setDictionarySize(properties.getDictionarySize());
    }

    private void setLcLpPb(int lc, int lp, int pb) {
        int numPosStates = 1 << pb;
        m_PosStateMask = numPosStates - 1;
    }

    private void setDictionarySize(int dictionarySize) {
        m_DictionarySize = dictionarySize;
        m_DictionarySizeCheck = Math.max(m_DictionarySize, 1);
    }

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
            LzmaLiteralDecoder.Decoder literalDecoder = this.literalDecoder.getDecoder((int)nowPos64, prv);
            prv = Base.StateIsCharState(state) ? literalDecoder.decodeNormal(rangeDecoder)
                                               : literalDecoder.decodeWithMatchByte(rangeDecoder, outWindow.get(rep[0]));

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
                        distance = rep[1];
                    else {
                        if (rangeDecoder.decodeBit(repG2Decoders, state) == 0)
                            distance = rep[2];
                        else {
                            distance = rep[3];
                            rep[3] = rep[2];
                        }
                        rep[2] = rep[1];
                    }
                    rep[1] = rep[0];
                    rep[0] = distance;
                }
                if (len == 0) {
                    len = repLenDecoder.decode(rangeDecoder, posState) + Base.kMatchMinLen;
                    state = Base.StateUpdateRep(state);
                }
            } else {
                rep[3] = rep[2];
                rep[2] = rep[1];
                rep[1] = rep[0];
                len = Base.kMatchMinLen + lengthDecoder.decode(rangeDecoder, posState);
                state = Base.StateUpdateMatch(state);
                int posSlot = posSlotDecoders[Base.GetLenToPosState(len)].decode(rangeDecoder);

                if (posSlot >= Base.kStartPosModelIndex) {
                    int numDirectBits = (posSlot >> 1) - 1;
                    rep[0] = (2 | (posSlot & 1)) << numDirectBits;

                    if (posSlot < Base.kEndPosModelIndex)
                        rep[0] += RangeBitTreeDecoder.reverseDecode(posDecoders,
                                rep[0] - posSlot - 1, rangeDecoder, numDirectBits);
                    else {
                        rep[0] += rangeDecoder.decodeDirectBits(numDirectBits - Base.kNumAlignBits) << Base.kNumAlignBits;
                        rep[0] += m_PosAlignDecoder.reverseDecode(rangeDecoder);
                        if (rep[0] < 0) {
                            if (rep[0] == -1)
                                return true;
                            throw new IOException("Error in data stream");
                        }
                    }
                } else
                    rep[0] = posSlot;
            }

            if (rep[0] >= nowPos64 || rep[0] >= m_DictionarySizeCheck)
                throw new IOException("Error in data stream");

            outWindow.writeBlock(rep[0], len);
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
