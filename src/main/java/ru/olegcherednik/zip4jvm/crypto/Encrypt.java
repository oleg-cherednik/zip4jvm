package ru.olegcherednik.zip4jvm.crypto;

/**
 * @author Oleg Cherednik
 * @since 05.12.2022
 */
public interface Encrypt {

    void encrypt(byte[] buf, int offs, int len);
}