package io.airlift.compress.zstd.dep;

/**
 * @author Oleg Cherednik
 * @since 26.10.2026
 */
public interface OutStream {

    int available();

    int getOffs();

    void copyMemory(byte[] buf, int offs, int bytes);

}
