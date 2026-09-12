package io.airlift.compress.zstd.fse;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import lombok.RequiredArgsConstructor;

/**
 * Forward (little-endian, least significant bit first) bit reader over {@link ByteArrayWithOffs}.
 * <p>
 * Bytes are pulled from the input <b>lazily, one at a time, and only when their bits are actually
 * required</b>, therefore the input offset is always {@code ceil(readBits / 8)} bytes ahead of the
 * position it had when the reader was created; i.e. when the last bit of the FSE table has been
 * consumed, {@code in} points exactly to the first byte behind the table.
 * <p>
 * Bits behind the end of the available data are read as zero (and mark the reader as overflown).
 *
 * @author Oleg Cherednik
 * @since 12.09.2026
 */
@RequiredArgsConstructor
final class BitReader {

    private final ByteArrayWithOffs in;
    private final int totalBytes;

    /** buffered bits; the least significant bit is the next bit to be read */
    private long buf;
    /** number of valid bits in {@link #buf} */
    private int bufBits;
    /** total number of bits consumed so far */
    private int readBits;
    /** total number of bytes pulled out of {@link #in} so far */
    private int readBytes;

    /** Next {@code count} bits, without consuming them. */
    public int peek(int count) {
        fetch(count);
        return (int) (buf & ((1L << count) - 1));
    }

    public void skip(int count) {
        fetch(count);
        buf >>>= count;
        bufBits -= count;
        readBits += count;
    }

    public int read(int count) {
        int res = peek(count);
        skip(count);
        return res;
    }

    /** {@code true} if more bits were consumed than the input actually contains */
    public boolean isOverflow() {
        return readBits > totalBytes * 8;
    }

    private void fetch(int count) {
        while (bufBits < count) {
            if (readBytes < totalBytes) {
                buf |= (long) (in.getByte() & 0xFF) << bufBits;
                readBytes++;
            }

            bufBits += 8;
        }
    }
}
