package net.lingala.zip4j.io;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.utils.BitUtils;
import net.lingala.zip4j.utils.CreateStringFunc;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 21.02.2019
 */
@RequiredArgsConstructor
@SuppressWarnings("SpellCheckingInspection")
public final class LittleEndianRandomAccessFile implements Closeable {

    @Getter
    @NonNull
    private final RandomAccessFile in;
    @Getter
    private int offs;

    public LittleEndianRandomAccessFile(@NonNull Path path) throws FileNotFoundException {
        in = new RandomAccessFile(path.toFile(), "r");
    }

    public int readWord() throws IOException {
        offs += 2;
        int ch0 = in.read();
        int ch1 = in.read();
        return (ch1 << 8) + ch0;
    }

    public int readDword() throws IOException {
        return (int)readDwordLong();
    }

    public long readDwordLong() throws IOException {
        offs += 4;
        int ch0 = in.read();
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        return ch3 << 24 | ch2 << 16 | ch1 << 8 | ch0;
    }

    public long readLong() throws IOException {
        offs += 8;
        return convertLong(in.readLong());
    }

    public String readString(int length) throws IOException {
        if (length <= 0)
            return null;

        offs += length;
        byte[] buf = new byte[length];
        in.readFully(buf);
        return new CreateStringFunc().apply(buf);
    }



    public byte readByte() throws IOException {
        offs++;
        return in.readByte();
    }

    public byte[] readBytes(int total) throws IOException {
        if (total <= 0)
            return null;

        offs += total;
        byte[] buf = new byte[total];

        if (in.read(buf) != total)
            throw new IOException("Not enough bytes to read");

        return buf;
    }

    public long length() throws IOException {
        return in.length();
    }

    public void seek(long pos) throws IOException {
        in.seek(pos);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    public long getFilePointer() throws IOException {
        return in.getFilePointer();
    }

    private static long convertLong(long val) {
        return BitUtils.getByte(val, 0) << 56 | BitUtils.getByte(val, 1) << 48 | BitUtils.getByte(val, 2) << 40 | BitUtils.getByte(val, 3) << 32 |
                BitUtils.getByte(val, 4) << 24 | BitUtils.getByte(val, 5) << 16 | BitUtils.getByte(val, 6) << 8 | BitUtils.getByte(val, 7);
    }

}
