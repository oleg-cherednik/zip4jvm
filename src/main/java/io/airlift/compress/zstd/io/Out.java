package io.airlift.compress.zstd.io;

import io.airlift.compress.zstd.ByteArrayWithOffs;

import java.io.Closeable;

/**
 * @author Oleg Cherednik
 * @since 17.11.2026
 */
public interface Out extends Closeable {

    int available();

    int getOffs();

    void putByte(byte x);

    void putShort(short x);

    void copyMemory(ByteArrayWithOffs in);

}
