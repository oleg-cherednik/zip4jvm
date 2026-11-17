package io.airlift.compress.zstd.io;

import java.io.Closeable;

/**
 * @author Oleg Cherednik
 * @since 17.11.2026
 */
public interface Out extends Closeable {

    int available();

    void putByte(byte x);

    void putShort(short x);

}
