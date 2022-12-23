package ru.olegcherednik.zip4jvm.io.in.data;

/**
 * @author Oleg Cherednik
 * @since 22.12.2022
 */
public interface ReadBuffer {

    int read(byte[] buf, int offs, int len);

}
