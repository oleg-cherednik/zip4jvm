package io.airlift.compress.zstd.dep;

import ru.olegcherednik.zip4jvm.io.in.DataInput;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.compress.XXHash64;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static io.airlift.compress.zstd.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.Constants.SIZE_OF_INT;
import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;

/**
 * This class is used only for output.
 *
 * @author Oleg Cherednik
 * @since 03.09.2026
 */
@RequiredArgsConstructor
public final class OutByteArrayWithOffs {

    private final byte[] buf;
    @Getter
    private int offs;
    @Setter
    @Getter
    private int limit;
    private XXHash64 checksum;

    public void initChecksum() {
        checksum = new XXHash64();
    }

    public long getChecksum() {
        return checksum.getValue();
    }

    public void cleanChecksum() {
        checksum = null;
    }

    public int available() {
        return buf.length - offs;
    }

    public void putByte(byte x) {
        buf[offs] = x;

        if (checksum != null)
            checksum.update(buf, offs, SIZE_OF_BYTE);

        offs += SIZE_OF_BYTE;
    }

    public void putInt(int x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        buf[offs + 2] = (byte) ((x & 0xFF0000) >> 8 * 2);
        buf[offs + 3] = (byte) ((x & 0xFF000000) >> 8 * 3);

        if (checksum != null)
            checksum.update(buf, offs, SIZE_OF_INT);

        offs += SIZE_OF_INT;
    }

    public void putLong(long x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        buf[offs + 2] = (byte) ((x & 0xFF0000) >> 8 * 2);
        buf[offs + 3] = (byte) ((x & 0xFF000000L) >> 8 * 3);
        buf[offs + 4] = (byte) ((x & 0xFF00000000L) >> 8 * 4);
        buf[offs + 5] = (byte) ((x & 0xFF0000000000L) >> 8 * 5);
        buf[offs + 6] = (byte) ((x & 0xFF000000000000L) >> 8 * 6);
        buf[offs + 7] = (byte) ((x & 0xFF00000000000000L) >> 8 * 7);

        if (checksum != null)
            checksum.update(buf, offs, SIZE_OF_LONG);

        offs += SIZE_OF_LONG;
    }

    public void copyMemory(ByteArrayWithOffs in, int bytes) {
        int nowRead = in.read(buf, offs, bytes);

        if (checksum != null)
            checksum.update(buf, offs, nowRead);

        offs += nowRead;
    }

    public void copyMemory(DataInput in, int bytes) {
        int nowRead = in.read(buf, offs, bytes);

        if (checksum != null)
            checksum.update(buf, offs, nowRead);

        offs += nowRead;
    }

    /**
     * Copies {@code length} bytes starting {@code offset} bytes back from the current position (LZ77 match). The
     * source may overlap the destination ({@code offset < length}): then the output repeats with period
     * {@code offset}.
     */
    public void copyMatch(int offset, int length) {
        final int start = offs;
        final int src = offs - offset;
        int remaining = length;

        while (remaining > 0) {
            // [src, offs) is already written and never overlaps [offs, offs + bytes); for overlapped match the
            // distance doubles on every iteration as the copied part is a repetition of the source pattern
            int bytes = Math.min(offs - src, remaining);
            System.arraycopy(buf, src, buf, offs, bytes);
            offs += bytes;
            remaining -= bytes;
        }

        if (checksum != null)
            checksum.update(buf, start, length);
    }

    @Override
    public String toString() {
        return String.format("size: %s, offs: %s", buf.length, offs);
    }

}
