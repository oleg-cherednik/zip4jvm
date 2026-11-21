package io.airlift.compress.zstd.io;

import io.airlift.compress.zstd.Out;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 21.11.2026
 */
public interface RandomAccessHuffman extends Out {

    void setOffs(int offs);

    int getByte();

}
