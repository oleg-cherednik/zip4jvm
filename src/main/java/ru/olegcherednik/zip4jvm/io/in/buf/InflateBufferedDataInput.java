package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.CommonBaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * @author Oleg Cherednik
 * @since 18.12.2022
 */
public class InflateBufferedDataInput extends CommonBaseDataInput {

    private final Inflater inflater = new Inflater(true);
    private final byte[] buf;
    private int offs;

    public InflateBufferedDataInput(DataInput in,
                                    int compressedSize,
                                    int uncompressedSize) throws IOException, DataFormatException {
        super(in);

        if (in.size() > Integer.MAX_VALUE)
            throw new Zip4jvmException("Should not be used for big buffer");

        this.buf = new byte[uncompressedSize];

        byte[] bufBuf = new byte[compressedSize];
        int len = in.read(bufBuf, 0, compressedSize);
        inflater.setInput(bufBuf, 0, len);

        int a = inflater.inflate(buf, 0, this.buf.length);

//        while (!inflater.finished()) {
//            int b = inflater.inflate(bufBuf, a, len);
//
//            if(inflater.needsInput())
//                inflater.setInput(new byte[1]);
//
//            a += b;
//        }
//
//        this.buf = new byte[a];
//        System.arraycopy(bufBuf, 0, this.buf, 0, a);

        int b = 0;
        b++;
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
