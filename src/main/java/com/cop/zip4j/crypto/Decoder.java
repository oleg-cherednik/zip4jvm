package com.cop.zip4j.crypto;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public interface Decoder {

    void decrypt(byte[] buf, int offs, int len);

    default void checkCRC() {
    }

    default int getLen(long bytesRead, int len, long length) {
        return len;
    }

}
