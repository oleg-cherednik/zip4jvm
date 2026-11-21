package io.airlift.compress.zstd.io;

import lombok.Getter;

import static io.airlift.compress.zstd.model.Constants.SIZE_OF_QWORD;
import static io.airlift.compress.zstd.model.Util.highestBit;
import static io.airlift.compress.zstd.model.Util.verify;

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
        verify(lastByte != 0, "Bitstream end mark not present");

        bitsConsumed = SIZE_OF_QWORD - highestBit(lastByte);
        int totalBytes = bbis.getTotalBytes();

        if (totalBytes >= SIZE_OF_QWORD) {  /* normal case */
            bits = bbis.getLong();
        } else {
            bits = readTail(totalBytes);
            bitsConsumed += (SIZE_OF_QWORD - totalBytes) * 8;
        }
    }

    private long readTail(int totalBytes) {
        long bits = 0;

        for (int i = 0; i < totalBytes; i++)
            bits = (bits << 8) | bbis.getByte();

        return bits;
    }

}
