package com.cop.zip4j.io.out;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 08.08.2019
 */
public interface DataOutputFile extends Closeable {

    void seek(long pos) throws IOException;

    void write(byte[] buf, int offs, int len) throws IOException;

    long getOffs();

    byte[] convertWord(int val);

    byte[] convertDword(long val);

    byte[] convertQword(long val);

}
