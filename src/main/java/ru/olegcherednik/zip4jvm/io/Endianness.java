package ru.olegcherednik.zip4jvm.io;

/**
 * @author Oleg Cherednik
 * @since 22.12.2022
 */
public interface Endianness {

    long getLong(byte[] buf, int offs, int len);
}
