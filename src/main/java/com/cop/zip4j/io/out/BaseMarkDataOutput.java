package com.cop.zip4j.io.out;

import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
abstract class BaseMarkDataOutput implements MarkDataOutput {

    private final Map<String, Long> map = new HashMap<>();

    @NonNull
    protected final ZipModel zipModel;
    @NonNull
    protected DataOutput delegate;

    protected BaseMarkDataOutput(@NonNull ZipModel zipModel) throws FileNotFoundException {
        this.zipModel = zipModel;
    }

    @Override
    public void seek(long pos) throws IOException {
        delegate.seek(pos);
    }

    @Override
    public long getOffs() {
        return delegate.getOffs();
    }

    @Override
    public void writeDword(int val) throws IOException {
        delegate.writeDword(val);
    }

    @Override
    public void writeDword(long val) throws IOException {
        delegate.writeDword(val);
    }

    @Override
    public void writeWord(int val) throws IOException {
        delegate.writeWord(val);
    }

    @Override
    public void writeQword(long val) throws IOException {
        delegate.writeQword(val);
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
        return "offs: " + getOffs();
    }

}
