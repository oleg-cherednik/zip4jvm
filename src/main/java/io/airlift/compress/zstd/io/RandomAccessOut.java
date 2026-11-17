package io.airlift.compress.zstd.io;

/**
 * @author Oleg Cherednik
 * @since 17.11.2026
 */
public interface RandomAccessOut extends Out {

    byte[] getBuf();

    void setOffs(int offs);

    default int read(byte[] buf, int offs, int len) {
        return -1;
    }

}
