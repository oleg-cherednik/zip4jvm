package com.cop.zip4j.io.in;

import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
abstract class BaseMarkDataInput implements MarkDataInput {

    private final Map<String, Long> map = new HashMap<>();

    @NonNull
    protected final ZipModel zipModel;
    @NonNull
    protected DataInput delegate;

    protected BaseMarkDataInput(@NonNull ZipModel zipModel) throws FileNotFoundException {
        this.zipModel = zipModel;
    }

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
    public void mark(String id) {
        map.put(id, getOffs());
    }

    @Override
    public long getWrittenBytesAmount(String id) {
        return getOffs() - map.getOrDefault(id, 0L);
    }

    @Override
    public String toString() {
        return "offs: " + getOffs() + " (0x" + Long.toHexString(getOffs()) + ')';
    }
}
