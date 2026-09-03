package io.airlift.compress.zstd;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 03.09.2026
 */
@RequiredArgsConstructor
public final class ByteArrayWithOffs {

    public final byte[] buf;

    public byte getByte(int offs) {
        return buf[offs];
    }

    public int getInt(int offset) {
        long val = 0;

        for (int i = 0; i < 4; i++)
            val = ((long) (buf[offset + i] & 0xFF) << 8 * i) | val;

        return (int) val;
    }

    public long getLong(int offset) {
        long val = 0;

        for (int i = 0; i < 8; i++)
            val = ((long) (buf[offset + i] & 0xFF) << 8 * i) | val;

        return val;
    }

    public int putByte(int offs, byte x) {
        buf[offs] = x;
        return Constants.SIZE_OF_BYTE;
    }

    public int putShort(int offs, short x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        return Constants.SIZE_OF_SHORT;
    }

    public int putInt(int offs, int x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        buf[offs + 2] = (byte) ((x & 0xFF0000) >> 8 * 2);
        buf[offs + 3] = (byte) ((x & 0xFF000000) >> 8 * 3);
        return Constants.SIZE_OF_INT;
    }

    public int putLong(int offs, long x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        buf[offs + 2] = (byte) ((x & 0xFF0000) >> 8 * 2);
        buf[offs + 3] = (byte) ((x & 0xFF000000) >> 8 * 3);
        buf[offs + 4] = (byte) ((x & 0xFF00000000L) >> 8 * 4);
        buf[offs + 5] = (byte) ((x & 0xFF0000000000L) >> 8 * 5);
        buf[offs + 6] = (byte) ((x & 0xFF000000000000L) >> 8 * 6);
        buf[offs + 7] = (byte) ((x & 0xFF00000000000000L) >> 8 * 7);
        return Constants.SIZE_OF_LONG;
    }

    public void copyMemory(long srcOffset,
                           byte[] out, long destOffset,
                           long bytes) {
        System.arraycopy(buf, (int) srcOffset, out, (int) destOffset, (int) bytes);
    }

}
