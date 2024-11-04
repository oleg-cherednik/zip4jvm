package ru.olegcherednik.zip4jvm.io.out.data.decorators;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DataOutputDecorator extends DataOutput {

    protected final DataOutput delegate;

    // ---------- DataOutput ----------

    @Override
    public ByteOrder getByteOrder() {
        return delegate.getByteOrder();
    }

    @Override
    public long getDiskOffs() {
        return delegate.getDiskOffs();
    }

    @Override
    public void writeByte(int val) throws IOException {
        delegate.writeByte(val);
    }

    @Override
    public void writeWord(int val) throws IOException {
        delegate.writeWord(val);
    }

    @Override
    public void writeDword(long val) throws IOException {
        delegate.writeDword(val);
    }

    @Override
    public void writeQword(long val) throws IOException {
        delegate.writeQword(val);
    }

    // ---------- OutputStream ----------

    @Override
    public void write(int b) throws IOException {
        delegate.write(b);
    }

    // ---------- Flushable ----------

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    // ---------- Closeable ----------

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    // ---------- Marker ----------

    @Override
    public void mark(String id) {
        delegate.mark(id);
    }

    @Override
    public long getMark(String id) {
        return delegate.getMark(id);
    }

    @Override
    public long getWrittenBytesAmount(String id) {
        return delegate.getWrittenBytesAmount(id);
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return delegate.toString();
    }

}
