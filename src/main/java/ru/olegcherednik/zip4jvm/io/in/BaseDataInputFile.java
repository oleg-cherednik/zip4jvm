package ru.olegcherednik.zip4jvm.io.in;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 22.01.2020
 */
public abstract class BaseDataInputFile implements DataInputFile {

    protected RandomAccessFile in;

    protected BaseDataInputFile(Path file) throws IOException {
        openFile(file);
    }

    @Override
    public long convert(byte[] buf, int offs, int len) {
        long res = 0;

        for (int i = offs + len - 1; i >= offs; i--)
            res = res << 8 | buf[i] & 0xFF;

        return res;
    }

    @Override
    public final void close() throws IOException {
        if (in != null)
            in.close();
    }

    @Override
    public String toString() {
        return "offs: " + getBaseOffs() + " (0x" + Long.toHexString(getBaseOffs()) + ')';
    }

    protected final void openFile(Path file) throws IOException {
        close();
        in = new RandomAccessFile(file.toFile(), "r");
    }

}
