package com.cop.zip4j.io;

import lombok.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 02.08.2019
 */
public class LittleEndianWriteFile implements DataOutput {

    private final RandomAccessFile out;

    public LittleEndianWriteFile(@NonNull Path path) throws FileNotFoundException {
        out = new RandomAccessFile(path.toFile(), "rw");
    }

    @Override
    public void writeWord(int val) throws IOException {
        out.writeByte((byte)val);
        out.writeByte((byte)(val >> 8));
    }

    @Override
    public void writeDword(int val) throws IOException {
        writeDword((long)val);
    }

    @Override
    public void writeDword(long val) throws IOException {
        out.writeByte((byte)val);
        out.writeByte((byte)(val >> 8));
        out.writeByte((byte)(val >> 16));
        out.writeByte((byte)(val >> 24));
    }

    @Override
    public void writeQword(long val) throws IOException {
        out.writeByte((byte)val);
        out.writeByte((byte)(val >> 8));
        out.writeByte((byte)(val >> 16));
        out.writeByte((byte)(val >> 24));
        out.writeByte((byte)(val >> 32));
        out.writeByte((byte)(val >> 40));
        out.writeByte((byte)(val >> 48));
        out.writeByte((byte)(val >> 56));
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        out.write(buf, offs, len);
    }

    @Override
    public void seek(long pos) throws IOException {
        out.seek(pos);
    }

    @Override
    public long getFilePointer() throws IOException {
        return out.getFilePointer();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public String toString() {
        return "offs: " + getOffs();
    }

}
