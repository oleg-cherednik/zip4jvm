package io.airlift.compress.zstd.io;

import io.airlift.compress.zstd.Out;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 17.11.2026
 */
public interface RandomAccess extends Out {

    RandomAccess slice(int offs, int len);

    void setOffs(int offs);

    int getByte();

    default int read(byte[] buf, int offs, int len) {
        return -1;
    }

    // ---------- Closeable ----------

    @Override
    default void close() throws IOException {
    }

}
