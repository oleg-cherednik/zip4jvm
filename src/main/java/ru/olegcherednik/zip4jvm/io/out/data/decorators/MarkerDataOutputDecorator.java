package ru.olegcherednik.zip4jvm.io.out.data.decorators;

import ru.olegcherednik.zip4jvm.io.BaseMarker;
import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.11.2024
 */
@RequiredArgsConstructor
public class MarkerDataOutputDecorator extends DataOutput {

    private final BaseMarker marker = new BaseMarker();
    private final DataOutput delegate;

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

    @Override
    public int getDiskNo() {
        return delegate.getDiskNo();
    }

    // ---------- OutputStream ----------

    @Override
    public void write(int b) throws IOException {
        marker.incTic();
    }

    // ---------- Marker ----------

    @Override
    public final void mark(String id) {
        marker.mark(id);
    }

    @Override
    public final long getMark(String id) {
        return marker.getMark(id);
    }

    @Override
    public final long getWrittenBytesAmount(String id) {
        return marker.getWrittenBytesAmount(id);
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

}
