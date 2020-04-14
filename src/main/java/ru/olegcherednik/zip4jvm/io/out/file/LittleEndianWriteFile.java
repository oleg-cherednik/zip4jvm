package ru.olegcherednik.zip4jvm.io.out.file;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 02.08.2019
 */
public class LittleEndianWriteFile implements DataOutputFile {

    private final OutputStream out;
    private long offs;

    public LittleEndianWriteFile(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        out = new BufferedOutputStream(new FileOutputStream(file.toFile()));
    }

    @Override
    public void fromLong(long val, byte[] buf, int offs, int len) {
        for (int i = 0; i < len; i++) {
            buf[offs + i] = (byte)val;
            val >>= 8;
        }
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        out.write(buf, offs, len);
        this.offs += len;
    }

    @Override
    public long getOffs() {
        return offs;
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public String toString() {
        return "offs: " + getOffs() + " (0x" + Long.toHexString(getOffs()) + ')';
    }

}
