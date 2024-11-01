package ru.olegcherednik.zip4jvm.io.out.data;

import ru.olegcherednik.zip4jvm.io.out.file.OffsetOutputStream;

import lombok.AccessLevel;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 01.11.2024
 */
public class TempWriteFileDataOutput extends BaseDataOutput {

    @Getter(AccessLevel.PROTECTED)
    private final OffsetOutputStream out;

    public TempWriteFileDataOutput(Path zip) throws IOException {
        out = OffsetOutputStream.create(zip);
    }

    @Override
    public long getRelativeOffs() {
        return out.getRelativeOffs();
    }

    @Override
    protected void writeInternal(byte[] buf, int offs, int len) throws IOException {
        out.write(buf, offs, len);
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
