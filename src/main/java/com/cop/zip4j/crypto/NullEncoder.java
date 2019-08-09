package com.cop.zip4j.crypto;

import com.cop.zip4j.io.out.DataOutput;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 09.08.2019
 */
final class NullEncoder implements Encoder {

    @Override
    public void writeHeader(DataOutput out) throws IOException {
    }

    @Override
    public void encrypt(byte[] buf, int offs, int len) {
    }

    @Override
    public String toString() {
        return "<null>";
    }

}
