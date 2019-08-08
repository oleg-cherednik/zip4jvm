package com.cop.zip4j.io.out;

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

    public byte[] convertWord(int val) {
        byte[] buf = new byte[2];
        buf[0] = (byte)val;
        buf[1] = (byte)(val >> 8);
        return buf;
    }

    public byte[] convertDword(long val) {
        byte[] buf = new byte[4];
        buf[0] = (byte)val;
        buf[1] = (byte)(val >> 8);
        buf[2] = (byte)(val >> 16);
        buf[3] = (byte)(val >> 24);
        return buf;
    }

    public byte[] convertQword(long val) {
        byte[] buf = new byte[8];
        buf[0] = (byte)val;
        buf[1] = (byte)(val >> 8);
        buf[2] = (byte)(val >> 16);
        buf[3] = (byte)(val >> 24);
        buf[4] = (byte)(val >> 32);
        buf[5] = (byte)(val >> 40);
        buf[6] = (byte)(val >> 48);
        buf[7] = (byte)(val >> 56);
        return buf;
    }

    @Override
    public void writeWord(int val) throws IOException {
        byte[] buf = convertWord(val);
        write(buf, 0, buf.length);
    }

    @Override
    public void writeDword(long val) throws IOException {
        byte[] buf = convertDword(val);
        write(buf, 0, buf.length);
    }

    @Override
    public void writeQword(long val) throws IOException {
        byte[] buf = convertQword(val);
        write(buf, 0, buf.length);
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
    public long getOffs() {
        try {
            return out.getFilePointer();
        } catch(IOException e) {
            return -1;
        }
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
