package ru.olegcherednik.zip4jvm.io.out.data;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.out.file.OffsOutputStream;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 01.11.2024
 */
public class SolidDataOutput extends MarkerDataOutput {

    protected final OffsOutputStream out;
    protected final ByteOrderConverter byteOrderConverter;

    public SolidDataOutput(ByteOrder byteOrder, Path file) throws IOException {
        out = OffsOutputStream.create(file);
        byteOrderConverter = new ByteOrderConverter(byteOrder);
    }

    @Override
    public ByteOrder getByteOrder() {
        return byteOrderConverter.getByteOrder();
    }

    // ---------- DataOutput ----------

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

    @Override
    public long getDiskOffs() {
        return out.getOffs();
    }

    // ---------- Flushable ----------

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    // ---------- OutputStream ----------

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        super.write(b);
    }

    // ---------- Closeable ----------

    @Override
    public void close() throws IOException {
        out.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return out.toString();
    }

}
