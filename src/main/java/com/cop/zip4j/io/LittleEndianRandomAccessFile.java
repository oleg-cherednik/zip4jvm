package com.cop.zip4j.io;

import com.cop.zip4j.utils.CreateStringFunc;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
public final class LittleEndianRandomAccessFile implements AutoCloseable {

    @Getter
    @NonNull
    private final RandomAccessFile in;
    @Getter
    private long offs;

    public LittleEndianRandomAccessFile(@NonNull Path path) throws FileNotFoundException {
        in = new RandomAccessFile(path.toFile(), "r");
    }

    public int readWord() throws IOException {
        offs += 2;
        int b0 = in.read();
        int b1 = in.read();
        return (b1 << 8) + b0;
    }

    public int readDword() throws IOException {
        return (int)readDwordLong();
    }

    public long readDwordLong() throws IOException {
        offs += 4;
        long b0 = in.read();
        long b1 = in.read();
        long b2 = in.read();
        long b3 = in.read();
        return b3 << 24 | b2 << 16 | b1 << 8 | b0;
    }

    public long readQword() throws IOException {
        offs += 8;
        long b0 = in.read();
        long b1 = in.read();
        long b2 = in.read();
        long b3 = in.read();
        long b4 = in.read();
        long b5 = in.read();
        long b6 = in.read();
        long b7 = in.read();
        return b7 << 56 | b6 << 48 | b5 << 40 | b4 << 32 | b3 << 24 | b2 << 16 | b1 << 8 | b0;
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
        offs = pos;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public String toString() {
        return "offs: " + offs;
    }

    public long getFilePointer() throws IOException {
        return in.getFilePointer();
    }

}
