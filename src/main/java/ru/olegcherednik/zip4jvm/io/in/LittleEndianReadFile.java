package ru.olegcherednik.zip4jvm.io.in;

import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 21.02.2019
 */
public class LittleEndianReadFile implements DataInputFile {

    private final RandomAccessFile in;

    public LittleEndianReadFile(Path path) throws FileNotFoundException {
        in = new RandomAccessFile(path.toFile(), "r");
    }

    @Override
    public void skip(int bytes) throws IOException {
        if (bytes > 0)
            in.skipBytes(bytes);
    }

    @Override
    public long length() throws IOException {
        return in.length();
    }

    @Override
    public void seek(long pos) throws IOException {
        in.seek(pos);
    }

    @Override
    public long convert(byte[] buf, int offs, int len) {
        long res = 0;

        for (int i = offs + len - 1; i >= offs; i--)
            res = res << 8 | buf[i] & 0xFF;

        return res;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return in.read(buf, offs, len);
    }

    @Override
    public int readSignature() throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        return b3 << 24 | b2 << 16 | b1 << 8 | b0;
    }

    @Override
    public long getOffs() {
        try {
            return in.getFilePointer();
        } catch(IOException e) {
            return IOUtils.EOF;
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public String toString() {
        return "offs: " + getOffs() + " (0x" + Long.toHexString(getOffs()) + ')';
    }

}
