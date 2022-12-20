package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.io.bzip2.Bzip2InputStream;
import ru.olegcherednik.zip4jvm.io.in.data.BaseDataInputNew;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputNew;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
public class Bzip2BufferedDataInput extends BaseDataInputNew {

    private final DataInputNew in;
    private final byte[] buf;
    private int offs;

    public Bzip2BufferedDataInput(DataInputNew in,
                                  int compressedSize,
                                  int uncompressedSize) throws IOException {
        this.in = in;

        try (Bzip2InputStream bzip = new Bzip2InputStream(in)) {
            buf = new byte[uncompressedSize];
            bzip.read(buf, 0, uncompressedSize);
        }
    }

    @Override
    public long getAbsoluteOffs() {
        return offs;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int res = 0;

        for (int i = 0; i < len && this.offs < this.buf.length; i++, res++)
            buf[offs + i] = this.buf[this.offs++];

        return res;
    }

    @Override
    public long toLong(byte[] buf, int offs, int len) {
        return in.toLong(buf, offs, len);
    }

    @Override
    public long skip(long bytes) throws IOException {
        int res = 0;

        for (int i = 0; i < bytes && offs < buf.length; i++, res++)
            offs++;

        return res;
    }

    @Override
    public void seek(long absoluteOffs) throws IOException {
        offs = (int)Math.min(buf.length, absoluteOffs);
    }

    @Override
    public String toString() {
        return String.valueOf(offs);
    }
}
