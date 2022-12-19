package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.io.ed.Deflate64CompressorInputStream;
import ru.olegcherednik.zip4jvm.io.in.data.CommonBaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
public class EnhancedDeflateBufferedDataInput extends CommonBaseDataInput {

    private final byte[] buf;
    private int offs;

    public EnhancedDeflateBufferedDataInput(DataInput in,
                                            int compressedSize,
                                            int uncompressedSize) throws IOException {
        super(in);

        Deflate64CompressorInputStream ed = new Deflate64CompressorInputStream(in);
        buf = new byte[uncompressedSize];
        ed.read(buf, 0, uncompressedSize);
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
