package ru.olegcherednik.zip4jvm.io.in;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
public interface DataInput extends Closeable {

    default int dwordSignatureSize() {
        return dwordSize();
    }

    int byteSize();

    int wordSize();

    int dwordSize();

    int qwordSize();

    long getOffs();

    default int readWordSignature() throws IOException {
        return readWord();
    }

    default int readDwordSignature() throws IOException {
        return (int)readDword();
    }

    int readWord() throws IOException;

    long readDword() throws IOException;

    long readQword() throws IOException;

    String readNumber(int bytes, int radix) throws IOException;

    String readString(int length, Charset charset) throws IOException;

    int readByte() throws IOException;

    byte[] readBytes(int total) throws IOException;

    void skip(long bytes) throws IOException;

    long length() throws IOException;

    void seek(long pos) throws IOException;

    default void backward(int bytes) throws IOException {
        seek(getOffs() - bytes);
    }

    int read(byte[] buf, int offs, int len) throws IOException;

    void mark(String id);

    long getMark(String id);

    void seek(String id) throws IOException;

    void cleanBuffer();

    byte[] getLastBytes(int bytes);

}
