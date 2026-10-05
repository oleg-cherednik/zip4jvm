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
 * @since 04.10.2026
 */
@RequiredArgsConstructor
public class ReadByteArrayWithOffs {

    protected final byte[] buf;
    @Getter
    protected int offs;
    @Setter
    @Getter
    private int inputLimit;

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
            val = (getByte() << 8 * i) | val;

        return (short) val;
    }

    public int getInt() {
        long val = 0;

        for (int i = 0; i < SIZE_OF_INT; i++)
            val = ((long) getByte() << 8 * i) | val;

        return (int) val;
    }

    public long getLong() {
        long val = 0;

        for (int i = 0; i < SIZE_OF_LONG; i++)
            val = ((long) getByte() << 8 * i) | val;

        return val;
    }

    public void copyMemory(int inOffs, byte[] out, int outOffs, int bytes) {
        System.arraycopy(buf, inOffs, out, outOffs, bytes);
        offs += bytes;
    }

    public void copyMemory(byte[] out, int bytes) {
        System.arraycopy(buf, offs, out, 0, bytes);
        offs += bytes;
    }

    public void copyMemory(ByteArrayWithOffs out, int bytes) {
        copyMemory(offs, out.buf, out.getOffs(), bytes);
        out.setOffs(out.getOffs() + bytes);
    }

    public byte[] readBytes(int total) {
        byte[] buf = new byte[total];
        System.arraycopy(this.buf, offs, buf, 0, total);
        offs += total;
        return buf;
    }

    @Override
    public String toString() {
        return String.format("size: %s, offs: %s", buf.length, offs);
    }

}
