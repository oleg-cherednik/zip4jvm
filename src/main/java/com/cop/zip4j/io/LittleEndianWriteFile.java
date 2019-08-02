package com.cop.zip4j.io;

import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang.ArrayUtils;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 02.08.2019
 */
public class LittleEndianWriteFile implements Closeable {

    private final RandomAccessFile out;
    @Getter
    private long offs;

    public LittleEndianWriteFile(@NonNull Path path) throws FileNotFoundException {
        out = new RandomAccessFile(path.toFile(), "rw");
    }

    public void writeWord(int val) throws IOException {
        out.writeByte((byte)val);
        out.writeByte((byte)(val >> 8));
        offs += 2;
    }

    public void writeDword(int val) throws IOException {
        writeDword((long)val);
    }

    public void writeDword(long val) throws IOException {
        out.writeByte((byte)val);
        out.writeByte((byte)(val >> 8));
        out.writeByte((byte)(val >> 16));
        out.writeByte((byte)(val >> 24));
        offs += 4;
    }

    public void writeQword(long val) throws IOException {
        out.writeByte((byte)val);
        out.writeByte((byte)(val >> 8));
        out.writeByte((byte)(val >> 16));
        out.writeByte((byte)(val >> 24));
        out.writeByte((byte)(val >> 32));
        out.writeByte((byte)(val >> 40));
        out.writeByte((byte)(val >> 48));
        out.writeByte((byte)(val >> 56));
        offs += 8;
    }

    public void writeBytes(byte... buf) throws IOException {
        if (ArrayUtils.isEmpty(buf))
            return;
        out.write(buf);
        offs += buf.length;
    }

    public void write(byte[] buf, int offs, int len) throws IOException {
        out.write(buf, offs, len);
        this.offs += len;
    }

    public void seek(long pos) throws IOException {
        out.seek(pos);
    }

    public long getFilePointer() throws IOException {
        return out.getFilePointer();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public String toString() {
        return "offs: " + offs;
    }

    public void incOffs(long inc) {
        offs += inc;
    }
}
