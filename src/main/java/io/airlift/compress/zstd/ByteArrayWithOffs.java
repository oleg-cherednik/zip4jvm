package io.airlift.compress.zstd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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

    public byte getByte(int offs) {
        return buf[offs];
    }

    public int getByte() {
        int res = getByte(offs) & 0xFF;
        offs += Constants.SIZE_OF_BYTE;
        return res;
    }

    public short getShort(int offs) {
        int val = 0;

        for (int i = 0; i < Constants.SIZE_OF_SHORT; i++)
            val = ((buf[offs + i] & 0xFF) << 8 * i) | val;

        return (short) val;
    }

    public int getShort() {
        int res = getShort(offs) & 0xFFFF;
        offs += Constants.SIZE_OF_SHORT;
        return res;
    }

    public int getInt(int offs) {
        long val = 0;

        for (int i = 0; i < Constants.SIZE_OF_INT; i++)
            val = ((long) (buf[offs + i] & 0xFF) << 8 * i) | val;

        return (int) val;
    }

    public int getInt() {
        int res = getInt(offs);
        offs += Constants.SIZE_OF_INT;
        return res;
    }

    public long getLong(int offs) {
        long val = 0;

        for (int i = 0; i < Constants.SIZE_OF_LONG; i++)
            val = ((long) (buf[offs + i] & 0xFF) << 8 * i) | val;

        return val;
    }

    public long getLong() {
        long res = getLong(offs);
        offs += Constants.SIZE_OF_LONG;
        return res;
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

    public void copyMemory(int inOffs, byte[] out, int outOffs, int bytes) {
        System.arraycopy(buf, inOffs, out, outOffs, bytes);
        offs += bytes;
    }

    public void copyMemory(byte[] out, int outOffs, int bytes) {
        copyMemory(offs, out, outOffs, bytes);
    }

    @Override
    public String toString() {
        return String.format("size: %s, offs: %s", buf.length, offs);
    }
}
