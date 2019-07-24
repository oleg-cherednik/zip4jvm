package com.cop.zip4j.io;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.cop.zip4j.utils.CreateStringFunc;

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
        long ch0 = in.read();
        long ch1 = in.read();
        long ch2 = in.read();
        long ch3 = in.read();
        return ch3 << 24 | ch2 << 16 | ch1 << 8 | ch0;
    }

    public long readQword() throws IOException {
        offs += 8;
        long ch0 = in.read();
        long ch1 = in.read();
        long ch2 = in.read();
        long ch3 = in.read();
        long ch4 = in.read();
        long ch5 = in.read();
        long ch6 = in.read();
        long ch7 = in.read();
        return ch7 << 56 | ch6 << 48 | ch5 << 40 | ch4 << 32 | ch3 << 24 | ch2 << 16 | ch1 << 8 | ch0;
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

    public void skip(int bytes) throws IOException {
        if (bytes > 0) {
            in.skipBytes(bytes);
            offs += bytes;
        }
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

}
