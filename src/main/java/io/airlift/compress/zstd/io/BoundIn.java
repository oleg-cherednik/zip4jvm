package io.airlift.compress.zstd.io;

import io.airlift.compress.zstd.In;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 18.11.2026
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BoundIn implements In {

    private final In in;
    private final long limit;

    public static In create(In in, long limit) {
        long curLimit = in.getPos() + in.available();
        return curLimit <= limit ? in : new BoundIn(in, limit);
    }

    // ---------- In ----------

    @Override
    public int readByte() {
        return in.readByte();
    }

    @Override
    public int readWord() {
        return in.readWord();
    }

    @Override
    public long readDword() {
        return in.readDword();
    }

    @Override
    public long readQword() {
        return in.readQword();
    }

    @Override
    public long getPos() {
        return in.getPos();
    }

    @Override
    public int available() {
        return (int) (limit - getPos());
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return in.read(buf);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return in.read(buf, offs, len);
    }

    // ---------- Closeable ----------

    @Override
    public void close() throws IOException {
        in.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return String.format("offs: %d, available: %d", getPos(), available());
    }
}
