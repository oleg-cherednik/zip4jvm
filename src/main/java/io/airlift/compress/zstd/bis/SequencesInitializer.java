package io.airlift.compress.zstd.bis;

import io.airlift.compress.zstd.BackwardDecorator;
import io.airlift.compress.zstd.ByteArrayWithOffs;
import lombok.Getter;

import static io.airlift.compress.zstd.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;
import static io.airlift.compress.zstd.Util.highestBit;
import static io.airlift.compress.zstd.Util.verify;

/**
 * @author Oleg Cherednik
 * @since 26.09.2026
 */
public class SequencesInitializer {

    private final BackwardDecorator bbis;
    private final ByteArrayWithOffs in;
    private final int inOffs;
    private final int inputLimit;

    @Getter
    private long bits;
    @Getter
    private int curOffs;
    @Getter
    private int bitsConsumed;

    public SequencesInitializer(BackwardDecorator bbis, ByteArrayWithOffs in, int inOffs, int inputLimit) {
        this.bbis = bbis;
        this.in = in;
        this.inOffs = inOffs;
        this.inputLimit = inputLimit;
        init();
    }

    private void init() {
        int lastByte = bbis.getLastByte() & 0xFF;
        verify(lastByte != 0, inputLimit, "Bitstream end mark not present");

        bitsConsumed = SIZE_OF_LONG - highestBit(lastByte);
        int totalBytes = bbis.getTotalBytes();

        if (totalBytes >= SIZE_OF_LONG) {  /* normal case */
            bbis.decOffs(SIZE_OF_LONG - 1);
            curOffs = inputLimit - SIZE_OF_LONG;
            bits = bbis.getLong();
        } else {
            bits = readTail(totalBytes);
            curOffs = inOffs;
            bitsConsumed += (SIZE_OF_LONG - totalBytes) * 8;
        }
    }

    private long readTail(int totalBytes) {
        long bits = 0;

        for (int i = 0; i < totalBytes; i++) {
            bits = (bits << 8) | bbis.getByte();
            bbis.decOffs(SIZE_OF_BYTE);

            if (i + 1 < totalBytes)
                bbis.decOffs(SIZE_OF_BYTE);
        }

        return bits;
    }

    private long readTail(int offs, int inputSize) {
        long bits = in.getByte(offs) & 0xFF;

        switch (inputSize) {
            case 7:
                bits |= (in.getByte(offs + 6) & 0xFFL) << 48;
            case 6:
                bits |= (in.getByte(offs + 5) & 0xFFL) << 40;
            case 5:
                bits |= (in.getByte(offs + 4) & 0xFFL) << 32;
            case 4:
                bits |= (in.getByte(offs + 3) & 0xFFL) << 24;
            case 3:
                bits |= (in.getByte(offs + 2) & 0xFFL) << 16;
            case 2:
                bits |= (in.getByte(offs + 1) & 0xFFL) << 8;
        }

        return bits;
    }
}
