package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.BaseDataInputNew;
import ru.olegcherednik.zip4jvm.io.in.data.CommonBaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputNew;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * @author Oleg Cherednik
 * @since 18.12.2022
 */
public class InflateBufferedDataInput extends BaseDataInputNew {

    private final DataInputNew in;
    private final byte[] buf;
    private int offs;

    public InflateBufferedDataInput(DataInputNew in,
                                    int compressedSize,
                                    int uncompressedSize) throws IOException, DataFormatException {
        this.in = in;

//        if (in.size() > Integer.MAX_VALUE)
//            throw new Zip4jvmException("Should not be used for big buffer");

        buf = new byte[uncompressedSize];

        byte[] bufBuf = new byte[compressedSize];
        int len = in.read(bufBuf, 0, compressedSize);

        Inflater inflater = new Inflater(true);
        inflater.setInput(bufBuf, 0, len);
        inflater.inflate(buf, 0, buf.length);
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
