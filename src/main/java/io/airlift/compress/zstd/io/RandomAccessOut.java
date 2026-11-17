package io.airlift.compress.zstd.io;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 17.11.2026
 */
public interface RandomAccessOut extends Out {

    @Deprecated
    byte[] getBuf();

    void setOffs(int offs);

    default int read(byte[] buf, int offs, int len) {
        return -1;
    }

    // ---------- Closeable ----------

    @Override
    default void close() throws IOException {
    }

}
