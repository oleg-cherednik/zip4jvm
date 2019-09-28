package ru.olegcherednik.zip4jvm.io.in;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
// TODO temporary byte buffer should be per thread
@NoArgsConstructor(access = AccessLevel.PROTECTED)
abstract class BaseDataInput implements DataInput {

    protected DataInputFile delegate;

    @Override
    public long getOffs() {
        return delegate.getOffs();
    }

    @Override
    public int readWord() throws IOException {
        byte[] buf = new byte[2];
        read(buf, 0, buf.length);
        return delegate.readWord(buf);
    }

    @Override
    public long readDword() throws IOException {
        byte[] buf = new byte[4];
        read(buf, 0, buf.length);
        return delegate.readDword(buf);
    }

    @Override
    public long readQword() throws IOException {
        byte[] buf = new byte[8];
        read(buf, 0, buf.length);
        return delegate.readQword(buf);
    }

    @Override
    public String readString(int length, Charset charset) throws IOException {
        byte[] buf = readBytes(length);
        return delegate.readString(buf, charset);
    }

    @Override
    public int readByte() throws IOException {
        byte[] buf = readBytes(1);
        return buf[0];
    }

    @Override
    public byte[] readBytes(int total) throws IOException {
        byte[] buf = new byte[total];
        int n = read(buf, 0, buf.length);

        if (n == IOUtils.EOF)
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        if (n < total)
            return Arrays.copyOfRange(buf, 0, n);
        return buf;
    }

    @Override
    public void skip(int bytes) throws IOException {
        delegate.skip(bytes);
    }

    @Override
    public long length() throws IOException {
        return delegate.length();
    }

    @Override
    public void seek(long pos) throws IOException {
        delegate.seek(pos);
    }

    @Override
    public final String toString() {
        return delegate.toString();
    }

}
