package com.cop.zip4j.crypto;

/**
 * @author Oleg Cherednik
 * @since 11.08.2019
 */
final class NullDecoder implements Decoder {

    @Override
    public void decrypt(byte[] buf, int offs, int len) {
    }

    @Override
    public String toString() {
        return "<null>";
    }

}
