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
    @Getter
    private long bits;
    @Getter
    private int bitsConsumed;

    public SequencesInitializer(BackwardDecorator bbis) {
        this.bbis = bbis;
        init();
    }

    private void init() {
        int lastByte = bbis.getLastByte() & 0xFF;
        verify(lastByte != 0, bbis.getOffs(), "Bitstream end mark not present");

        bitsConsumed = SIZE_OF_LONG - highestBit(lastByte);
        int totalBytes = bbis.getTotalBytes();

        if (totalBytes >= SIZE_OF_LONG) {  /* normal case */
            bbis.decOffs(SIZE_OF_LONG - 1);
            bits = bbis.getLong();
        } else {
            bits = readTail(totalBytes);
            bitsConsumed += (SIZE_OF_LONG - totalBytes) * 8;
            // readTail() leaves offs right before the stream start (-1);
            // move it SIZE_OF_LONG bytes before the loaded window, as getLong() does
            bbis.decOffs(SIZE_OF_LONG - SIZE_OF_BYTE);
        }
    }

    private long readTail(int totalBytes) {
        long bits = 0;

        for (int i = 0; i < totalBytes; i++)
            bits = (bits << 8) | bbis.getByte();

        return bits;
    }

}
