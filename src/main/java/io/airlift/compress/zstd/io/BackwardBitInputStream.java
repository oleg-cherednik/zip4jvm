package io.airlift.compress.zstd.io;

import io.airlift.compress.zstd.Out;
import lombok.Getter;

import static io.airlift.compress.zstd.model.Constants.SIZE_OF_LONG;
import static io.airlift.compress.zstd.model.Util.highestBit;
import static io.airlift.compress.zstd.model.Util.verify;

/**
 * @author Oleg Cherednik
 * @since 26.09.2026
 */
@Getter
public class BackwardBitInputStream {

    private final BackwardDecorator in;

    private final int tableLog;
    private final byte[] symbols;
    private final byte[] numbersOfBits;

    private long bits;
    private int bitsConsumed;
    private boolean overflow;

    public BackwardBitInputStream(BackwardDecorator in, int tableLog, byte[] symbols, byte[] numbersOfBits) {
        this.in = in;
        this.tableLog = tableLog;
        this.symbols = symbols;
        this.numbersOfBits = numbersOfBits;
        init();
    }

    public void init() {
        int lastByte = in.getLastByte();
        verify(lastByte != 0, "Bitstream end mark not present");

        int size = Math.min(in.getTotalBytes(), SIZE_OF_LONG);
        bits = readTail(size);
        bitsConsumed = SIZE_OF_LONG - highestBit(lastByte) + (SIZE_OF_LONG - size) * 8;
    }

    public boolean load() {
        if (bitsConsumed > 64) {
            overflow = true;
            return true;
        }

        // offs is right before the loaded window, i.e. on the next byte to load
        if (in.getOffs() < 0)
            return true;

        // shift in a new byte for each fully consumed one, until the stream start is reached
        int bytes = bitsConsumed >>> 3; // divide by 8

        for (; bytes > 0 && in.getOffs() >= 0; bytes--) {
            bits = (bits << 8) | in.getByte();
            bitsConsumed -= 8;
        }

        return bytes > 0;
    }

    private long readTail(int size) {
        long val = 0;

        for (int i = 0; i < size; i++)
            val = (val << 8) | in.getByte();

        return val;
    }

    public void decodeTail(Out out) {
        while (out.available() > 0) {
            BackwardBitInputDecorator bbid = new BackwardBitInputDecorator(in, bits, bitsConsumed);
            bitsConsumed = bbid.getBitsConsumed();
            bits = bbid.getBits();

            if (bbid.isDone())
                break;

            decodeSymbol(out);
        }

        // not more data in bit stream, so no need to reload
        while (out.available() > 0)
            decodeSymbol(out);

        // all bytes are loaded (offs is right before the stream start) and all bits are consumed
        verify(in.getOffs() < 0 && bitsConsumed == Long.SIZE, "Bit stream is not fully consumed");
    }

    public void decodeSymbol(Out out) {
        int value = (int) BackwardBitInputDecorator.peekBitsFast(bitsConsumed, bits, tableLog);
        out.writeByte(symbols[value]);
        bitsConsumed += numbersOfBits[value];
    }

}
