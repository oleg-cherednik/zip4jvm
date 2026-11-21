package io.airlift.compress.zstd;

import io.airlift.compress.zstd.io.ByteArrayWithOffs;
import io.airlift.compress.zstd.io.ByteArrayRandomAccessIn;
import io.airlift.compress.zstd.io.RandomAccessOut;

import java.io.Closeable;

/**
 * @author Oleg Cherednik
 * @since 17.11.2026
 */
public interface Out extends Closeable {

    int available();

//    int getLimit();

    int getOffs();

    void writeByte(int x);

    void writeWord(int x);

    void writeDword(long x);

    void copyMemory(ByteArrayWithOffs in);

    void copyMemory(RandomAccessOut rao);

    void copyMemory(ByteArrayRandomAccessIn in, int size);

}
