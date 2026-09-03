package io.airlift.compress.zstd;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 03.09.2026
 */
@RequiredArgsConstructor
public final class ByteArrayWithOffs {

    public final byte[] buf;

    public int putInt(int offs, int x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        buf[offs + 2] = (byte) ((x & 0xFF0000) >> 8 * 2);
        buf[offs + 3] = (byte) ((x & 0xFF000000) >> 8 * 3);
        return Constants.SIZE_OF_INT;
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

    public void copyMemory(long srcOffset,
                           byte[] out, long destOffset,
                           long bytes) {
        System.arraycopy(buf, (int) srcOffset, out, (int) destOffset, (int) bytes);
    }

}
