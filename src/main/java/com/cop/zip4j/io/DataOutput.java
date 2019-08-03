package com.cop.zip4j.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
public interface DataOutput extends Closeable {

    void seek(long pos) throws IOException;

    long getFilePointer() throws IOException;

    default long getOffs() {
        try {
            return getFilePointer();
        } catch(IOException e) {
            return -1;
        }
    }

    void writeDword(int val) throws IOException;

    void writeDword(long val) throws IOException;

    void writeWord(int val) throws IOException;

    void writeQword(long val) throws IOException;

    void writeBytes(byte... buf) throws IOException;

    void write(byte[] buf, int offs, int len) throws IOException;

    int getCounter();

    default void write(int b) throws IOException {
        write(new byte[] { (byte)b }, 0, 1);
    }
}
