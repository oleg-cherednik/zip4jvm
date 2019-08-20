package com.cop.zip4j.io.in;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
public interface DataInput extends Closeable {

    long getOffs();

    default int readSignature() throws IOException {
        return (int)readDword();
    }

    int readWord() throws IOException;

    long readDword() throws IOException;

    long readQword() throws IOException;

    String readString(int length) throws IOException;

    int readByte() throws IOException;

    byte[] readBytes(int total) throws IOException;

    void skip(int bytes) throws IOException;

    long length() throws IOException;

    void seek(long pos) throws IOException;

    int read(byte[] buf, int offs, int len) throws IOException;

}
