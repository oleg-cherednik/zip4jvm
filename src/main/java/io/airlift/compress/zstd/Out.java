package io.airlift.compress.zstd;

import io.airlift.compress.zstd.io.ByteArrayWithOffs;
import io.airlift.compress.zstd.io.ByteArrayRandomAccessIn;
import io.airlift.compress.zstd.io.RandomAccess;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 17.11.2026
 */
public interface Out extends Closeable {

    int available();

    // TODO нужно в одном месте (посмотреть действительно ли нужно?)
    @Deprecated
    int getLimit();

    int getOffs();

    void writeByte(int x);

    void writeWord(int x);

    void writeDword(long x);

    void copyMemory(ByteArrayWithOffs in);

    void copyMemory(RandomAccess rao) throws IOException;

    void copyMemory(ByteArrayRandomAccessIn in, int size);

}
