package io.airlift.compress.zstd.io;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

import static io.airlift.compress.zstd.model.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.model.Constants.SIZE_OF_INT;
import static io.airlift.compress.zstd.model.Constants.SIZE_OF_SHORT;

/**
 * @author Oleg Cherednik
 * @since 17.11.2026
 */
@Getter
@Setter
public class ByteArrayRandomAccessOut implements RandomAccessOut {

    private static final int OFFS_ZERO = 0;

    @Getter //(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final byte[] buf;
    private int offs;
    private int limit;

    public ByteArrayRandomAccessOut(int limit) {
        buf = new byte[limit];
        this.limit = limit;
    }

    public RandomAccessOut getOutForCopy() {
        limit = offs;
        offs = OFFS_ZERO;
        return this;
    }

    public void reset() {
        offs = OFFS_ZERO;
        limit = buf.length;
        Arrays.fill(buf, (byte) 0);
    }

    // ---------- Out ----------

    @Override
    public int available() {
        return limit - offs;
    }

    @Override
    public void putByte(byte x) {
        buf[offs] = x;
        offs += SIZE_OF_BYTE;
    }

    @Override
    public void putShort(short x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        offs += SIZE_OF_SHORT;
    }

    @Override
    public void putInt(int x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        buf[offs + 2] = (byte) ((x & 0xFF0000) >> 8 * 2);
        buf[offs + 3] = (byte) ((x & 0xFF000000) >> 8 * 3);
        offs += SIZE_OF_INT;
    }

    @Override
    public void copyMemory(ByteArrayWithOffs in) {
        System.arraycopy(in.buf, 0, buf, offs, in.getOffs());
        offs += in.getOffs();
    }

    @Override
    public void copyMemory(RandomAccessOut rao) {
        int nowRead = rao.read(buf, offs, rao.available());
        offs += nowRead;
    }

    @Override
    public void copyMemory(ByteArrayRandomAccessIn in, int size) {
        System.arraycopy(in.getBuf(), in.getOffs(), buf, offs, size);
        offs += size;
    }

    // ---------- RandomAccessOut ----------

    @Override
    public int read(byte[] buf, int offs, int len) {
        int available = Math.min(available(), len);
        System.arraycopy(this.buf, this.offs, buf, offs, available);
        this.offs += available;
        return available;
    }

    // ---------- Objects ----------

    @Override
    public String toString() {
        return String.format("limit: %s, offs: %s", limit, offs);
    }

}
