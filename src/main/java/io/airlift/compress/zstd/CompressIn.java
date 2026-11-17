package io.airlift.compress.zstd;

/**
 * @author Oleg Cherednik
 * @since 17.11.2026
 */
public interface CompressIn {

    int available();

    int getSize();

    int getLimit();

    int getOffs();

    int getByte();

    long getLong();

    int getInt();

    /**
     * Reads up to {@code len} bytes into {@code buf} starting at {@code offs}. Returns the number of bytes read,
     * or {@code -1} at end of input when nothing was read.
     */
    int read(byte[] buf, int offs, int len);

    @Deprecated
    void setOffs(int offs);

    @Deprecated
    void setSize(int size);

    @Deprecated
    void setLimit(int limit);

    @Deprecated
    byte[] getBuf();

}
