package io.airlift.compress.zstd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static io.airlift.compress.zstd.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.Constants.SIZE_OF_INT;
import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;
import static io.airlift.compress.zstd.Constants.SIZE_OF_SHORT;

/**
 * @author Oleg Cherednik
 * @since 03.09.2026
 */
@RequiredArgsConstructor
public final class ByteArrayWithOffs {

    public final byte[] buf;
    @Getter
    private int offs;
    @Setter
    @Getter
    private int inputLimit;

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

    /**
     * Reads the byte right before offs (backward), i.e. offs is moved back first.
     */
    public int getBytePrev() {
        offs -= SIZE_OF_BYTE;
        return buf[offs] & 0xFF;
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

    public int putByte(int offs, byte x) {
        buf[offs] = x;
        return SIZE_OF_BYTE;
    }

    public void putByte(byte x) {
        putByte(offs, x);
        offs += SIZE_OF_BYTE;
    }

    public int putShort(int offs, short x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        return SIZE_OF_SHORT;
    }

    public int putInt(int offs, int x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        buf[offs + 2] = (byte) ((x & 0xFF0000) >> 8 * 2);
        buf[offs + 3] = (byte) ((x & 0xFF000000) >> 8 * 3);
        return SIZE_OF_INT;
    }

    public void putInt(int x) {
        putInt(offs, x);
        offs += SIZE_OF_INT;
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
        return SIZE_OF_LONG;
    }

    public void putLong(long x) {
        putLong(offs, x);
        offs += SIZE_OF_LONG;
    }

    public void copyMemory(int inOffs, byte[] out, int outOffs, int bytes) {
        System.arraycopy(buf, inOffs, out, outOffs, bytes);
        offs += bytes;
    }

    public void copyMemory(byte[] out, int bytes) {
        System.arraycopy(buf, offs, out, 0, bytes);
        offs += bytes;
    }

    public void copyMemory(byte[] out, int outOffs, int bytes) {
        copyMemory(offs, out, outOffs, bytes);
    }

    public void copyMemory(ByteArrayWithOffs out, int bytes) {
        copyMemory(offs, out.buf, out.getOffs(), bytes);
        out.offs += bytes;
    }

    @Override
    public String toString() {
        return String.format("size: %s, offs: %s", buf.length, offs);
    }

}
