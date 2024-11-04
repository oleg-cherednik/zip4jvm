package ru.olegcherednik.zip4jvm.io.out.data;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.out.file.OffsOutputStream;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 01.11.2024
 */
public class TempWriteFileDataOutput extends BaseDataOutput {

    private final OffsOutputStream out;

    public TempWriteFileDataOutput(Path zip) throws IOException {
        super(ByteOrder.LITTLE_ENDIAN);
        out = OffsOutputStream.create(zip);
    }

    // ---------- DataOutput ----------

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
