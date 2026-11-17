package io.airlift.compress.zstd.io;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static io.airlift.compress.zstd.model.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.model.Constants.SIZE_OF_LONG;

/**
 * @author Oleg Cherednik
 * @since 03.09.2026
 */
@RequiredArgsConstructor
public final class ByteArrayWithOffs {

    public final byte[] buf;
    @Getter
    @Setter
    private int offs;
    @Setter
    @Getter
    private int limit;

    public ByteArrayWithOffs(byte[] buf, int offs) {
        this(buf, offs, buf.length);
    }

    public ByteArrayWithOffs(byte[] buf, int offs, int limit) {
        this.buf = buf;
        this.offs = offs;
        this.limit = limit;
    }

    public int available() {
        return buf.length - offs;
    }

    public int getByte() {
        int res = buf[offs] & 0xFF;
        offs += SIZE_OF_BYTE;
        return res;
    }

    /**
     * Reads the byte right before offs (backward), i.e. offs is moved back first.
     */
    public int getBytePrev() {
        offs -= SIZE_OF_BYTE;
        return buf[offs] & 0xFF;
    }

    public long getLong() {
        long val = 0;

        for (int i = 0; i < SIZE_OF_LONG; i++)
            val = ((long) (buf[offs + i] & 0xFF) << 8 * i) | val;

        offs += SIZE_OF_LONG;
        return val;
    }

    public int read(byte[] buf, int offs, int len) {
        int maxLen = Math.min(available(), len);

        for (int i = 0; i < maxLen; i++)
            buf[offs + i] = (byte) getByte();

        return maxLen;
    }

    public int putByte(int offs, byte x) {
        buf[offs] = x;
        return SIZE_OF_BYTE;
    }

    public void putByte(byte x) {
        putByte(offs, x);
        offs += SIZE_OF_BYTE;
    }

    public void copyMemory(ByteArrayRandomAccessIn in, int bytes) {
        for (int i = 0; i < bytes; i++)
            putByte((byte) in.getByte());
    }

    @Override
    public String toString() {
        return String.format("size: %s, offs: %s", buf.length, offs);
    }

}
