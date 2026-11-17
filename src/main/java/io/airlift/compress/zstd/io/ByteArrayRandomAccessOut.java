package io.airlift.compress.zstd.io;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.dep.InByteArrayWithOffs;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Arrays;

import static io.airlift.compress.zstd.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.Constants.SIZE_OF_INT;
import static io.airlift.compress.zstd.Constants.SIZE_OF_SHORT;

/**
 * @author Oleg Cherednik
 * @since 17.11.2026
 */
@Getter
public class ByteArrayRandomAccessOut implements RandomAccessOut {

    private static final int OFFS_ZERO = 0;

    public final byte[] buf;
    @Setter
    private int offs;
    @Setter
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

    public void putInt(int x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        buf[offs + 2] = (byte) ((x & 0xFF0000) >> 8 * 2);
        buf[offs + 3] = (byte) ((x & 0xFF000000) >> 8 * 3);
        offs += SIZE_OF_INT;
    }

    public void copyMemory(ByteArrayWithOffs in) {
        System.arraycopy(in.buf, 0, buf, offs, in.getOffs());
        offs += in.getOffs();
    }

    //    @Override
    public void copyMemory(OutByteArrayWithOffs in, int size) {
        System.arraycopy(in.buf, 0, buf, offs, size);
        offs += size;
    }

    public int read(byte[] buf, int offs, int len) {
        int available = Math.min(available(), len);
        System.arraycopy(this.buf, this.offs, buf, offs, available);
        this.offs += available;
        return available;
    }

    public void copyMemory(InByteArrayWithOffs in, int size) {
        System.arraycopy(in.buf, in.getOffs(), buf, offs, size);
        offs += size;
    }

    public void copyMemory(ByteArrayRandomAccessOut in, int size) {
        System.arraycopy(in.buf, 0, buf, offs, size);
        offs += size;
    }

    // ---------- Objects ----------

    @Override
    public String toString() {
        return String.format("size: %s, offs: %s", buf.length, offs);
    }

    @Override
    public void close() throws IOException {

    }
}
