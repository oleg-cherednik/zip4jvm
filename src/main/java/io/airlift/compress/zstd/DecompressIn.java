package io.airlift.compress.zstd;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 27.10.2026
 */
public interface DecompressIn extends Closeable {

    int readByte();

    int readWord();

    long readDword();

    long readQword();

    long getPos();

    int available() throws IOException;

    int read(byte[] buf) throws IOException;

    int read(byte[] buf, int offs, int len) throws IOException;

}
