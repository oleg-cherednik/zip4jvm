package io.airlift.compress.zstd;

import lombok.Getter;
import org.apache.commons.io.IOUtils;

import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 27.10.2026
 */
public class InInputStream extends FilterInputStream implements In {

    @Getter
    private long pos;
    // pos at the last mark() to restore it on reset()
    private long markPos;

    public InInputStream(InputStream in) {
        super(new DataInputStream(in));
    }

    private DataInputStream getIn() {
        return (DataInputStream) in;
    }

    // ----------

    @Override
    public int read() throws IOException {
        int b = super.read();

        if (b != -1)
            pos++;

        return b;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = super.skip(n);
        pos += skipped;
        return skipped;
    }

    @Override
    public synchronized void mark(int readLimit) {
        super.mark(readLimit);
        markPos = pos;
    }

    @Override
    public synchronized void reset() throws IOException {
        // throws when mark is not supported, then pos is not changed
        super.reset();
        pos = markPos;
    }

    // ---------- In ----------

    @Override
    public int readByte() {
        try {
            int v = getIn().readUnsignedByte();
            pos++;
            return v;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int readWord() {
        try {
            int v = getIn().readUnsignedShort();
            pos += 2;
            return v;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long readDword() {
        try {
            long v = getIn().readInt();
            pos += 4;
            return v;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long readQword() {
        try {
            long v = getIn().readLong();
            pos += 8;
            return v;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int available() {
        try {
            return getIn().available();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        int nowRead = getIn().read(b);

        if (nowRead != -1)
            pos += nowRead;

        return nowRead;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int nowRead = getIn().read(b, off, len);

        if (nowRead != -1)
            pos += nowRead;

        return nowRead;
    }

}
