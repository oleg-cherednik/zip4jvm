package ru.olegcherednik.zip4jvm.io.in;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 21.02.2019
 */
@SuppressWarnings("SpellCheckingInspection")
public class LittleEndianReadFile implements DataInputFile {

    private final RandomAccessFile in;

    public LittleEndianReadFile(Path path) throws FileNotFoundException {
        in = new RandomAccessFile(path.toFile(), "r");
    }

    @Override
    public int readWord(byte[] buf) {
        int b0 = buf[0] & 0xFF;
        int b1 = buf[1] & 0xFF;
        return (b1 << 8) + b0;
    }

    @Override
    public long readDword(byte[] buf) {
        int b0 = buf[0] & 0xFF;
        int b1 = buf[1] & 0xFF;
        int b2 = buf[2] & 0xFF;
        long b3 = buf[3] & 0xFF;
        return b3 << 24 | b2 << 16 | b1 << 8 | b0;
    }

    @Override
    public long readQword(byte[] buf) {
        int b0 = buf[0] & 0xFF;
        int b1 = buf[1] & 0xFF;
        int b2 = buf[2] & 0xFF;
        int b3 = buf[3] & 0xFF;
        long b4 = buf[4] & 0xFF;
        long b5 = buf[5] & 0xFF;
        long b6 = buf[6] & 0xFF;
        long b7 = buf[7] & 0xFF;
        return b7 << 56 | b6 << 48 | b5 << 40 | b4 << 32 | b3 << 24 | b2 << 16 | b1 << 8 | b0;
    }

    @Override
    public String readString(byte[] buf, Charset charset) {
        return ArrayUtils.isEmpty(buf) ? null : new String(buf, charset);
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
    public int read(byte[] buf, int offs, int len) throws IOException {
        return in.read(buf, offs, len);
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
