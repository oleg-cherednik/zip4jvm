package ru.olegcherednik.zip4jvm.io.out.data.decorators;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.out.data.ByteOrderConverter;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.11.2024
 */
public abstract class ByteOrderDataOutput extends DataOutput {

    protected final DataOutput delegate;
    private final ByteOrderConverter byteOrderConverter;

    protected ByteOrderDataOutput(DataOutput delegate) {
        this.delegate = delegate;
        byteOrderConverter = new ByteOrderConverter(delegate.getByteOrder());
    }

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
        byteOrderConverter.writeByte(val, this);
    }

    @Override
    public void writeWord(int val) throws IOException {
        byteOrderConverter.writeWord(val, this);
    }

    @Override
    public void writeDword(long val) throws IOException {
        byteOrderConverter.writeDword(val, this);
    }

    @Override
    public void writeQword(long val) throws IOException {
        byteOrderConverter.writeQword(val, this);
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
