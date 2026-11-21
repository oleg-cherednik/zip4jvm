package io.airlift.compress.zstd.io;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Arrays;

import static io.airlift.compress.zstd.model.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.model.Constants.SIZE_OF_DWORD;
import static io.airlift.compress.zstd.model.Constants.SIZE_OF_QWORD;
import static io.airlift.compress.zstd.model.Constants.SIZE_OF_WORD;

/**
 * @author Oleg Cherednik
 * @since 17.11.2026
 */
@Getter
@Setter
public class ByteArrayRandomAccess implements RandomAccess {

    private static final int OFFS_ZERO = 0;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    public final byte[] buf;
    private int offs;
    private int limit;

    public ByteArrayRandomAccess(int limit) {
        this(new byte[limit], 0, limit);
    }

    public ByteArrayRandomAccess(byte[] buf, int offs, int limit) {
        this.buf = buf;
        this.offs = offs;
        this.limit = limit;
    }

    public RandomAccess getOutForCopy() {
        limit = offs;
        offs = OFFS_ZERO;
        return this;
    }

    public void reset() {
        offs = OFFS_ZERO;
        limit = buf.length;
        Arrays.fill(buf, (byte) 0);
    }

    // ---------- In ----------

    @Override
    public int readByte() {
        int res = buf[offs] & 0xFF;
        offs += SIZE_OF_BYTE;
        return res;
    }

    @Override
    public int readWord() {
        int val = 0;

        for (int i = 0; i < SIZE_OF_WORD; i++)
            val = (readByte() << 8 * i) | val;

        return (short) val;
    }

    @Override
    public long readDword() {
        long val = 0;

        for (int i = 0; i < SIZE_OF_DWORD; i++)
            val = ((long) readByte() << 8 * i) | val;

        return val;
    }

    @Override
    public long readQword() {
        long val = 0;

        for (int i = 0; i < SIZE_OF_QWORD; i++)
            val = ((long) readByte() << 8 * i) | val;

        return val;
    }

    @Override
    public long getPos() {
        return offs;
    }

    // ---------- Out ----------

    @Override
    public int available() {
        return limit - offs;
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return 0;
    }

    @Override
    public void writeByte(int x) {
        buf[offs] = (byte) x;
        offs += SIZE_OF_BYTE;
    }

    @Override
    public void writeWord(int x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        offs += SIZE_OF_WORD;
    }

    @Override
    public void writeDword(long x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        buf[offs + 2] = (byte) ((x & 0xFF0000) >> 8 * 2);
        buf[offs + 3] = (byte) ((x & 0xFF000000L) >> 8 * 3);
        offs += SIZE_OF_DWORD;
    }

    @Override
    public void copyMemory(RandomAccess rao) throws IOException {
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
    public RandomAccess slice(int offs, int len) {
        return new ByteArrayRandomAccess(buf, offs, Math.min(limit, len));
    }

    @Override
    public int read(byte[] buf, int offs, int len) {
        int available = Math.min(available(), len);
        System.arraycopy(this.buf, this.offs, buf, offs, available);
        this.offs += available;
        return available;
    }

    @Override
    public int readPrevByte() {
        offs -= SIZE_OF_BYTE;
        return buf[offs] & 0xFF;
    }

    // ---------- Objects ----------

    @Override
    public String toString() {
        return String.format("limit: %s, offs: %s", limit, offs);
    }

}
