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

    @Deprecated
    void setOffs(int offs);

    @Deprecated
    void setSize(int size);

    @Deprecated
    void setLimit(int limit);

    byte[] getBuf();

}
