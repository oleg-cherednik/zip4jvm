package com.cop.zip4j.io.in;

import com.cop.zip4j.model.ZipModel;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SingleZipInputStream implements DataInput {

    @NonNull
    private final DataInput delegate;

    @NonNull
    public static SingleZipInputStream create(@NonNull ZipModel zipModel) throws FileNotFoundException {
        return new SingleZipInputStream(new LittleEndianReadFile(zipModel.getZipFile()));
    }

    // -------------------
    @Override
    public long getOffs() {
        return delegate.getOffs();
    }

    @Override
    public int readWord() throws IOException {
        return delegate.readWord();
    }

    @Override
    public long readDwordLong() throws IOException {
        return delegate.readDwordLong();
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
    public byte readByte() throws IOException {
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
    public int read(byte[] buf, int offs, int len) throws IOException {
        return delegate.read(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
