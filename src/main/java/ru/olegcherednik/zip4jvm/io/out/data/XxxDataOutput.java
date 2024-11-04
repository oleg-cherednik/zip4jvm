package ru.olegcherednik.zip4jvm.io.out.data;

import ru.olegcherednik.zip4jvm.io.ByteOrder;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.11.2024
 */
public abstract class XxxDataOutput extends DataOutput {

    protected final DataOutput out;
    private final ByteOrderConverter byteOrderConverter;

    protected XxxDataOutput(DataOutput out) {
        this.out = out;
        byteOrderConverter = new ByteOrderConverter(out.getByteOrder());
    }

    // ---------- DataOutput ----------

    @Override
    public ByteOrder getByteOrder() {
        return out.getByteOrder();
    }

    @Override
    public long getDiskOffs() {
        return out.getDiskOffs();
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
        out.write(b);
    }

    // ---------- Flushable ----------

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    // ---------- Closeable ----------

    @Override
    public void close() throws IOException {
        out.close();
    }

    // ---------- Marker ----------

    @Override
    public void mark(String id) {
        out.mark(id);
    }

    @Override
    public long getMark(String id) {
        return out.getMark(id);
    }

    @Override
    public long getWrittenBytesAmount(String id) {
        return out.getWrittenBytesAmount(id);
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return out.toString();
    }

}
