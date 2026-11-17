package io.airlift.compress.zstd.io;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.dep.InByteArrayWithOffs;

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

    void putInt(int x);

    void copyMemory(ByteArrayWithOffs in);

    default void copyMemory(RandomAccessOut rao) {
    }

    void copyMemory(InByteArrayWithOffs in, int size);

}
