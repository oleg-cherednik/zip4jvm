package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.io.ByteOrder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 15.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class FooDataInput extends OldBaseDataInput {

    protected final DataInput in;

    @Override
    public String readNumber(int bytes, int radix) throws IOException {
        return super.readNumber(bytes, radix);
    }

    @Override
    public String readString(int length, Charset charset) throws IOException {
        byte[] buf = readBytes(length);
        return buf.length == 0 ? null : new String(buf, charset);
    }

    @Override
    public byte[] readBytes(int total) throws IOException {
        if (total <= 0)
            return ArrayUtils.EMPTY_BYTE_ARRAY;

        byte[] buf = new byte[total];
        int n = read(buf, 0, buf.length);

        if (n == IOUtils.EOF)
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        if (n < total)
            return Arrays.copyOfRange(buf, 0, n);
        return buf;
    }

    public void seek(String id) throws IOException {
        super.seek(id);
    }

    // ---------- DataInput ----------

    @Override
    public long getAbsoluteOffs() {
        return in.getAbsoluteOffs();
    }

    @Override
    public long size() {
        return in.size();
    }

    @Override
    public ByteOrder getByteOrder() {
        return in.getByteOrder();
    }

    @Override
    public void seek(long absoluteOffs) {
        in.seek(absoluteOffs);
    }

    @Override
    public int readByte() throws IOException {
        return getByteOrder().readByte(this);
    }

    @Override
    public int readWord() throws IOException {
        return getByteOrder().readWord(this);
    }

    @Override
    public long readDword() throws IOException {
        return getByteOrder().readDword(this);
    }

    @Override
    public long readQword() throws IOException {
        return getByteOrder().readQword(this);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        in.close();
    }

}
