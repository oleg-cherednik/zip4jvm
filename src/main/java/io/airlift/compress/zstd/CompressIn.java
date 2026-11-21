package io.airlift.compress.zstd;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 17.11.2026
 */
public interface CompressIn extends Closeable {

    int available();

    /**
     * Reads up to {@code len} bytes into {@code buf} starting at {@code offs}.
     * Returns the number of bytes read, or {@code -1} at end of input when
     * nothing was read.
     */
    int read(byte[] buf, int offs, int len) throws IOException;

    // ---------- Closeable ----------

    @Override
    default void close() throws IOException {
    }

}
