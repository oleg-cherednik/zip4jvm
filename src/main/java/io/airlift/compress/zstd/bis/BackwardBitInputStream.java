package io.airlift.compress.zstd.bis;

import io.airlift.compress.zstd.BackwardDecorator;
import io.airlift.compress.zstd.ByteArrayWithOffs;
import lombok.Getter;

import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;
import static io.airlift.compress.zstd.Util.highestBit;
import static io.airlift.compress.zstd.Util.verify;

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

    private int getLastByte() {
        return in.getLastByte();
    }

    private long getLong() {
        return in.getLong();
    }

    public void init() {
        int lastByte = getLastByte();
        verify(lastByte != 0, 0x0, "Bitstream end mark not present");

        bitsConsumed = SIZE_OF_LONG - highestBit(lastByte);

        if (in.getTotalBytes() >= SIZE_OF_LONG) {  /* normal case */
            bits = getLong();
        } else {
            bits = readTail();
            bitsConsumed += (SIZE_OF_LONG - in.getTotalBytes()) * 8;
        }
    }

    public boolean load() {
        if (bitsConsumed > 64) {
            overflow = true;
            return true;
        }

        // getLong() leaves offs right before the start of the loaded window
        int offs = in.getOffs() + 1;

        if (offs == 0)
            return true;

        int bytes = bitsConsumed >>> 3; // divide by 8

        if (offs >= SIZE_OF_LONG) {
            if (bytes > 0) {
                in.incOffs(SIZE_OF_LONG - bytes);
                bits = getLong();
            }
            bitsConsumed &= 0b111;
            return false;
        }

        if (offs < bytes) {
            bytes = offs;
            in.incOffs(SIZE_OF_LONG - bytes);
            bitsConsumed -= bytes * SIZE_OF_LONG;
            bits = getLong();
            return true;
        }

        in.incOffs(SIZE_OF_LONG - bytes);
        bits = getLong();
        bitsConsumed -= bytes * SIZE_OF_LONG;
        return false;
    }

    private long readTail() {
        long val = 0;

        for (int i = 0; i < in.getTotalBytes(); i++)
            val = (val << 8) | in.getByte();

        return val;
    }

    public void decodeTail(ByteArrayWithOffs out, int outOffs,
                           final long outputLimit) {
        // closer to the end
        while (outOffs < outputLimit) {
            BitInputStream.Loader loader = new BitInputStream.Loader(in, bits, bitsConsumed);
            bitsConsumed = loader.getBitsConsumed();
            bits = loader.getBits();

            if (loader.isDone())
                break;

            decodeSymbol(out, outOffs++);
        }

        // not more data in bit stream, so no need to reload
        while (outOffs < outputLimit) {
            decodeSymbol(out, outOffs++);
        }

        verify(BitInputStream.isEndOfStream(0, this.in.getOffs() + 1, bitsConsumed),
               this.in.getFromOffs(), "Bit stream is not fully consumed");
    }

    public void decodeSymbol(ByteArrayWithOffs out, int offs) {
        int value = (int) BitInputStream.peekBitsFast(bitsConsumed, bits, tableLog);
        out.putByte(offs, symbols[value]);
        bitsConsumed += numbersOfBits[value];
    }

}
