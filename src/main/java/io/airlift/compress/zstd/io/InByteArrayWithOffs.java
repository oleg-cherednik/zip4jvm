package io.airlift.compress.zstd.io;

import io.airlift.compress.zstd.CompressIn;
import lombok.Getter;
import lombok.Setter;

import static io.airlift.compress.zstd.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.Constants.SIZE_OF_INT;
import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;

/**
 * This class is used only for input.
 *
 * @author Oleg Cherednik
 * @since 03.09.2026
 */
@Getter
@Setter
public final class InByteArrayWithOffs implements CompressIn {

    private final byte[] buf;
    private int offs;
    private int size;
    private int limit;

    public InByteArrayWithOffs(byte[] buf) {
        this.buf = buf;
        limit = buf.length;
    }

    // ---------- CompressIn ----------

    @Override
    public int available() {
        return limit - offs;
    }

    @Override
    public int getByte() {
        int res = buf[offs] & 0xFF;
        offs += SIZE_OF_BYTE;
        return res;
    }

    @Override
    public int getInt() {
        long val = 0;

        for (int i = 0; i < SIZE_OF_INT; i++)
            val = ((long) (buf[offs + i] & 0xFF) << 8 * i) | val;

        offs += SIZE_OF_INT;
        return (int) val;
    }

    @Override
    public long getLong() {
        long val = 0;

        for (int i = 0; i < SIZE_OF_LONG; i++)
            val = ((long) (buf[offs + i] & 0xFF) << 8 * i) | val;

        offs += SIZE_OF_LONG;
        return val;
    }

    @Override
    public int read(byte[] buf, int offs, int len) {
        int available = available();
        if (available <= 0)
            return -1;

        int nowRead = Math.min(available, len);
        System.arraycopy(this.buf, this.offs, buf, offs, nowRead);
        this.offs += nowRead;
        return nowRead;
    }

    @Override
    public String toString() {
        return String.format("size: %s, offs: %s", buf.length, offs);
    }

}
