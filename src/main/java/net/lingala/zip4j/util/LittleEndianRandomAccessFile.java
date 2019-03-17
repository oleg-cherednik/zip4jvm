package net.lingala.zip4j.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 21.02.2019
 */
@RequiredArgsConstructor
public final class LittleEndianRandomAccessFile implements Closeable {

    @Getter
    private final RandomAccessFile raf;
    @Getter
    private int offs;

    public LittleEndianRandomAccessFile(@NonNull Path path) throws FileNotFoundException {
        raf = new RandomAccessFile(path.toFile(), "r");
    }

    public void resetOffs() {
        offs = 0;
    }

    public short readShort() throws IOException {
        offs += 2;
        return convertShort(raf.readShort());
    }

    public int readInt() throws IOException {
        offs += 4;
        return (int)convertInt(raf.readInt());
    }

    public String readString(int length) throws IOException {
        if (length <= 0)
            return null;

        offs += length;
        byte[] buf = new byte[length];
        raf.readFully(buf);
        return new CreateStringFunc().apply(buf);
    }

    public String readString(int length, @NonNull Charset charset) throws IOException {
        if (length <= 0)
            return null;

        offs += length;
        byte[] buf = new byte[length];
        raf.readFully(buf);
        return new String(buf, charset);
    }

    public long readIntAsLong() throws IOException {
        offs += 4;
        return convertInt(raf.readInt());
    }

    public long readLong() throws IOException {
        offs += 8;
        return convertLong(raf.readLong());
    }

    public byte[] readBytes(int total) throws IOException {
        if (total <= 0)
            return null;

        byte[] buf = new byte[total];

        if (raf.read(buf) != total)
            throw new IOException("Not enough bytes to read");

        return buf;
    }

    public long length() throws IOException {
        return raf.length();
    }

    public void seek(long pos) throws IOException {
        raf.seek(pos);
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }

    public long getFilePointer() throws IOException {
        return raf.getFilePointer();
    }

    private static short convertShort(short val) {
        return (short)(BitUtils.getByte(val, 0) << 8 | BitUtils.getByte(val, 1));
    }

    private static long convertInt(int val) {
        return BitUtils.getByte(val, 0) << 24 | BitUtils.getByte(val, 1) << 16 | BitUtils.getByte(val, 2) << 8 | BitUtils.getByte(val, 3);
    }

    private static long convertLong(long val) {
        return BitUtils.getByte(val, 0) << 56 | BitUtils.getByte(val, 1) << 48 | BitUtils.getByte(val, 2) << 40 | BitUtils.getByte(val, 3) << 32 |
                BitUtils.getByte(val, 4) << 24 | BitUtils.getByte(val, 5) << 16 | BitUtils.getByte(val, 6) << 8 | BitUtils.getByte(val, 7);
    }

}
