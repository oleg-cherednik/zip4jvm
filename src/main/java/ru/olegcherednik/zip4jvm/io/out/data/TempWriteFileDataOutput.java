package ru.olegcherednik.zip4jvm.io.out.data;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.out.file.OffsetOutputStream;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 01.11.2024
 */
public class TempWriteFileDataOutput extends BaseDataOutput {

    private final OffsetOutputStream out;

    public TempWriteFileDataOutput(Path zip) throws IOException {
        super(ByteOrder.LITTLE_ENDIAN);
        out = OffsetOutputStream.create(zip);
    }

    // ---------- DataOutput ----------

    @Override
    public long getRelativeOffs() {
        return out.getRelativeOffs();
    }

    @Override
    protected void write(byte b) {
        Quietly.doQuietly(() -> out.write(b));
    }

    @Override
    public void flush() throws IOException {
        out.flush();
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
