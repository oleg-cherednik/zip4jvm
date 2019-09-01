package com.cop.zip4j.io.out;

import org.apache.commons.lang.ArrayUtils;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
public interface DataOutput extends Closeable {

    void seek(long pos) throws IOException;

    long getOffs();

    default void writeWordSignature(int sig) throws IOException {
        writeWord(sig);
    }

    default void writeDwordSignature(int sig) throws IOException {
        writeDword(sig);
    }

    void writeWord(int val) throws IOException;

    void writeDword(long val) throws IOException;

    void writeQword(long val) throws IOException;

    default void writeBytes(byte... buf) throws IOException {
        if (ArrayUtils.isNotEmpty(buf))
            write(buf, 0, buf.length);
    }

    void write(byte[] buf, int offs, int len) throws IOException;

    void mark(String id);

    long getWrittenBytesAmount(String id);

    int getDisk();

}
