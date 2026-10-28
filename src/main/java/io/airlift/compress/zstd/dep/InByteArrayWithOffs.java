package io.airlift.compress.zstd.dep;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static io.airlift.compress.zstd.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.Constants.SIZE_OF_INT;
import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;
import static io.airlift.compress.zstd.Constants.SIZE_OF_SHORT;

/**
 * This class is used only for input.
 *
 * @author Oleg Cherednik
 * @since 03.09.2026
 */
@RequiredArgsConstructor
public final class InByteArrayWithOffs {

    public final byte[] buf;
    @Getter
    private int offs;
    @Setter
    @Getter
    private int limit;

    public void setOffs(int offs) {
        this.offs = offs;
    }

    public int available() {
        return buf.length - offs;
    }

    public int getByte() {
        int res = buf[offs] & 0xFF;
        offs += SIZE_OF_BYTE;
        return res;
    }

    public int getShort() {
        int val = 0;

        for (int i = 0; i < SIZE_OF_SHORT; i++)
            val = ((buf[offs + i] & 0xFF) << 8 * i) | val;

        offs += SIZE_OF_SHORT;
        return (short) val;
    }

    public int getInt() {
        long val = 0;

        for (int i = 0; i < SIZE_OF_INT; i++)
            val = ((long) (buf[offs + i] & 0xFF) << 8 * i) | val;

        offs += SIZE_OF_INT;
        return (int) val;
    }

    public long getLong() {
        long val = 0;

        for (int i = 0; i < SIZE_OF_LONG; i++)
            val = ((long) (buf[offs + i] & 0xFF) << 8 * i) | val;

        offs += SIZE_OF_LONG;
        return val;
    }

    public void copyMemory(int inOffs, byte[] out, int outOffs, int bytes) {
        System.arraycopy(buf, inOffs, out, outOffs, bytes);
        offs += bytes;
    }

    @Override
    public String toString() {
        return String.format("size: %s, offs: %s", buf.length, offs);
    }

}
