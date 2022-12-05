package ru.olegcherednik.zip4jvm.crypto;

/**
 * @author Oleg Cherednik
 * @since 05.12.2022
 */
public interface Decrypt {

    void decrypt(byte[] buf, int offs, int len);
}
