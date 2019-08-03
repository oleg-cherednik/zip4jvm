package com.cop.zip4j.io;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
@RequiredArgsConstructor
public final class MarkDataOutputDecorator implements MarkDataOutput {

    private final DataOutput delegate;
    private final Map<String, Long> map = new HashMap<>();

    @Override
    public void mark(String id) {
        map.put(id, getOffs());
    }

    @Override
    public long getWrittenBytesAmount(String id) {
        return getOffs() - map.getOrDefault(id, 0L);
    }

    @Override
    public void seek(long pos) throws IOException {
        delegate.seek(pos);
    }

    @Override
    public long getFilePointer() throws IOException {
        return delegate.getFilePointer();
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
    public void write(byte[] buf, int offs, int len) throws IOException {
        delegate.write(buf, offs, len);
    }

    @Override
    public int getCounter() {
        return delegate.getCounter();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

}
