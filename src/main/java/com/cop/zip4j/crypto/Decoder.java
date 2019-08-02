package com.cop.zip4j.crypto;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public interface Decoder {

    Decoder NULL = new Decoder() {
        @Override
        public void decrypt(byte[] buf, int offs, int len) {
        }

        @Override
        public String toString() {
            return "<null>";
        }
    };

    void decrypt(byte[] buf, int offs, int len);

    default void checkCRC() {
    }

    default int getLen(long bytesRead, int len, long length) {
        return len;
    }

}
