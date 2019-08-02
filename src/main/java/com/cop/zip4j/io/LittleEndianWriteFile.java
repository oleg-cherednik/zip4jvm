package com.cop.zip4j.io;

import lombok.NonNull;

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

    public LittleEndianWriteFile(@NonNull Path path) throws FileNotFoundException {
        out = new RandomAccessFile(path.toFile(), "rw");
    }

    public void write(int val) throws IOException {
        out.write(val);
    }

    public void write(byte[] buf, int offs, int len) throws IOException {
        out.write(buf, offs, len);
    }

    public void seek(long pos) throws IOException {
        out.seek(pos);
    }

    public long getFilePointer() throws IOException {
        return out.getFilePointer();
    }

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
