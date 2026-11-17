package io.airlift.compress.zstd.io;

/**
 * @author Oleg Cherednik
 * @since 17.11.2026
 */
public interface RandomAccessOut extends Out {

    void setOffs(int offs);

}
