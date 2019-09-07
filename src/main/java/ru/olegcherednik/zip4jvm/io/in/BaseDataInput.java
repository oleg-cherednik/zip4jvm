package ru.olegcherednik.zip4jvm.io.in;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
abstract class BaseDataInput implements DataInput {

    @NonNull
    protected DataInput delegate;

    @Override
    public long getOffs() {
        return delegate.getOffs();
    }

    @Override
    public int readWord() throws IOException {
        return delegate.readWord();
    }

    @Override
    public long readDword() throws IOException {
        return delegate.readDword();
    }

    @Override
    public long readQword() throws IOException {
        return delegate.readQword();
    }

    @Override
    public String readString(int length) throws IOException {
        return delegate.readString(length);
    }

    @Override
    public int readByte() throws IOException {
        return delegate.readByte();
    }

    @Override
    public byte[] readBytes(int total) throws IOException {
        return delegate.readBytes(total);
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
    public String toString() {
        return "offs: " + getOffs() + " (0x" + Long.toHexString(getOffs()) + ')';
    }
}
