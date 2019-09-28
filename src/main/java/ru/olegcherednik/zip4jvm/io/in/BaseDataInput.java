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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
abstract class BaseDataInput implements DataInput {

    private static final int OFFS_BYTE = 0;
    private static final int OFFS_WORD = 1;
    private static final int OFFS_DWORD = 3;
    private static final int OFFS_QWORD = 7;

    private static final ThreadLocal<byte[]> THREAD_LOCAL_BUF = ThreadLocal.withInitial(() -> new byte[15]);

    protected DataInputFile delegate;

    @Override
    public long getOffs() {
        return delegate.getOffs();
    }

    @Override
    public int readByte() throws IOException {
        return (int)readAndConvert(OFFS_BYTE, 1);
    }

    @Override
    public int readWord() throws IOException {
        return (int)readAndConvert(OFFS_WORD, 2);
    }

    @Override
    public long readDword() throws IOException {
        return readAndConvert(OFFS_DWORD, 4);
    }

    @Override
    public long readQword() throws IOException {
        return readAndConvert(OFFS_QWORD, 8);
    }

    private long readAndConvert(int offs, int len) throws IOException {
        byte[] buf = THREAD_LOCAL_BUF.get();
        read(buf, offs, len);
        return delegate.convert(buf, offs, len);
    }

    @Override
    public String readString(int length, Charset charset) throws IOException {
        byte[] buf = readBytes(length);
        return delegate.readString(buf, charset);
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
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public final String toString() {
        return delegate.toString();
    }

}
