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

    private void decOffs(int bytes) {
        in.decOffs(bytes);
    }

    public void init() {
        int lastByte = getLastByte();
        verify(lastByte != 0, in.getOffs() + in.getBuf().length, "Bitstream end mark not present");

        bitsConsumed = SIZE_OF_LONG - highestBit(lastByte);

        if (in.getTotalBytes() >= SIZE_OF_LONG) {  /* normal case */
            decOffs(SIZE_OF_LONG - 1);
            bits = getLong();
        } else {
            // a stream shorter than SIZE_OF_LONG is read in one go, starting from its very first byte
            decOffs(in.getOffs());
            bits = readTail(in.getTotalBytes());
            bitsConsumed += (SIZE_OF_LONG - in.getTotalBytes()) * 8;
        }
    }

    public boolean load() {
        if (bitsConsumed > 64) {
            overflow = true;
            return true;
        }

        if (in.getOffs() == 0)
            return true;

        int bytes = bitsConsumed >>> 3; // divide by 8

        if (in.getOffs() >= SIZE_OF_LONG) {
            if (bytes > 0) {
                in.decOffs(bytes);
                bits = getLong();
            }
            bitsConsumed &= 0b111;
            return false;
        }

        if (in.getOffs() < bytes) {
            bytes = in.getOffs();
            in.decOffs(in.getOffs());
            bitsConsumed -= bytes * SIZE_OF_LONG;
            bits = getLong();
            return true;
        }

        in.decOffs(bytes);
        bits = in.getLong();
        bitsConsumed -= bytes * SIZE_OF_LONG;
        return false;
    }

    private long readTail(int inputSize) {
        int offs = in.getOffs();
        long bits = in.getBuf()[offs] & 0xFF;

        switch (inputSize) {
            case 7:
                bits |= (in.getBuf()[offs + 6] & 0xFFL) << 48;
            case 6:
                bits |= (in.getBuf()[offs + 5] & 0xFFL) << 40;
            case 5:
                bits |= (in.getBuf()[offs + 4] & 0xFFL) << 32;
            case 4:
                bits |= (in.getBuf()[offs + 3] & 0xFFL) << 24;
            case 3:
                bits |= (in.getBuf()[offs + 2] & 0xFFL) << 16;
            case 2:
                bits |= (in.getBuf()[offs + 1] & 0xFFL) << 8;
        }

        return bits;
    }

    public void decodeTail(ByteArrayWithOffs in,
                           ByteArrayWithOffs out, int outOffs,
                           final long outputLimit) {
        // closer to the end
        while (outOffs < outputLimit) {
            BitInputStream.LoaderNew loader = new BitInputStream.LoaderNew(this.in, bits, bitsConsumed);
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

        verify(BitInputStream.isEndOfStream(0, this.in.getOffs(), bitsConsumed),
               this.in.getFromOffs(), "Bit stream is not fully consumed");
    }

    public void decodeSymbol(ByteArrayWithOffs out, int offs) {
        int value = (int) BitInputStream.peekBitsFast(bitsConsumed, bits, tableLog);
        out.putByte(offs, symbols[value]);
        bitsConsumed += numbersOfBits[value];
    }

}
