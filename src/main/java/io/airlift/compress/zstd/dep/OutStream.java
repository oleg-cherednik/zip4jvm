package io.airlift.compress.zstd.dep;

import io.airlift.compress.zstd.ByteArrayWithOffs;

/**
 * @author Oleg Cherednik
 * @since 26.10.2026
 */
public interface OutStream {

    int available();

    int getOffs();

    void putByte(byte x);

    void copyMemory(ByteArrayWithOffs in, int bytes);

}
