package io.airlift.compress.zstd;

/**
 * @author Oleg Cherednik
 * @since 27.10.2026
 */
public interface In {

    int readByte();

    int readWord();

    long readDword();

    long readQword();

    long getAbsOffs();

    long available();

    int read(byte[] buf, int offs, int len);

    byte[] readBytes(int total);

}
