package io.airlift.compress.zstd.io;

import io.airlift.compress.zstd.In;
import io.airlift.compress.zstd.Out;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 17.11.2026
 */
public interface RandomAccess extends In, Out {

    RandomAccess slice(int offs, int len);

    void setOffs(int offs);

    int readPrevByte();

    // ---------- Closeable ----------

    @Override
    default void close() throws IOException {
    }

}
