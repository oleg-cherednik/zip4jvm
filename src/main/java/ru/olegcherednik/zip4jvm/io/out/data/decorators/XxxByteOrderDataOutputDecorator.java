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
public abstract class XxxByteOrderDataOutputDecorator extends DataOutput {

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
