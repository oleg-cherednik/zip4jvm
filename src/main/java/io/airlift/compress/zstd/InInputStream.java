package io.airlift.compress.zstd;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 27.10.2026
 */
public class InInputStream extends FilterInputStream implements In {

    public InInputStream(InputStream in) {
        super(in);
    }

    // ---------- In ----------

    @Override
    public int readByte() {
        return 0;
    }

    @Override
    public int readWord() {
        return 0;
    }

    @Override
    public long readDword() {
        return 0;
    }

    @Override
    public long readQword() {
        return 0;
    }

    @Override
    public long getAbsOffs() {
        return 0;
    }

    @Override
    public int available() {
        return 0;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return 0;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return 0;
    }

}
