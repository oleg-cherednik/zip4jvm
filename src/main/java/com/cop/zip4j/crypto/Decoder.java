package com.cop.zip4j.crypto;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public interface Decoder {

    void decrypt(byte[] buf, int offs, int len);

}
