package ru.olegcherednik.zip4jvm.io.out;

/**
 * @author Oleg Cherednik
 * @since 02.08.2026
 */
public interface WriteBuffer {

    void write(byte[] buf, int offs, int len);

    void write(int b);

    void close();

}
