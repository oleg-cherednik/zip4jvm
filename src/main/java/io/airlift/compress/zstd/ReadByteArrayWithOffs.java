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

    @Override
    public String toString() {
        return String.format("size: %s, offs: %s", buf.length, offs);
    }

}
