package io.airlift.compress.zstd;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 27.10.2026
 */
public interface In {

    int readByte();

    int readWord();

    long readDword();

    long readQword();

    long getPos();

    int available() throws IOException;

    int read(byte[] b) throws IOException;

    int read(byte[] b, int off, int len) throws IOException;

}
