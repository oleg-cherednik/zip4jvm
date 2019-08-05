package com.cop.zip4j.io.in;

import com.cop.zip4j.utils.CreateStringFunc;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;

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
public class LittleEndianReadFile implements DataInput {

    @Getter
    @NonNull
    private final RandomAccessFile in;

    public LittleEndianReadFile(@NonNull Path path) throws FileNotFoundException {
        in = new RandomAccessFile(path.toFile(), "r");
    }

    @Override
    public int readWord() throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        return (b1 << 8) + b0;
    }

    @Override
    public long readDwordLong() throws IOException {
        long b0 = in.read();
        long b1 = in.read();
        long b2 = in.read();
        long b3 = in.read();
        return b3 << 24 | b2 << 16 | b1 << 8 | b0;
    }

    @Override
    public long readQword() throws IOException {
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

    @Override
    public String readString(int length) throws IOException {
        if (length <= 0)
            return null;

        byte[] buf = new byte[length];
        in.readFully(buf);
        return new CreateStringFunc().apply(buf);
    }

    @Override
    public int readByte() throws IOException {
        return in.read();
    }

    @Override
    public byte[] readBytes(int total) throws IOException {
        if (total <= 0)
            return null;

        byte[] buf = new byte[total];
        total = in.read(buf);

        return total == buf.length ? buf : ArrayUtils.subarray(buf, 0, total);
    }

    @Override
    public void skip(int bytes) throws IOException {
        if (bytes > 0) {
            in.skipBytes(bytes);
        }
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
            return -1;
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
