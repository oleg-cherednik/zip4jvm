package com.cop.zip4j.crypto;

import com.cop.zip4j.io.DataOutputStream;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public interface Encoder {

    Encoder NULL = new Encoder() {
        @Override
        public void encrypt(byte[] buf, int offs, int len) {
        }

        @Override
        public void writeHeader(@NonNull DataOutputStream out) throws IOException {
        }

        @Override
        public String toString() {
            return "<null>";
        }
    };

    void encrypt(@NonNull byte[] buf, int offs, int len);

    void writeHeader(@NonNull DataOutputStream out) throws IOException;

    default void close(DataOutputStream out) throws IOException {
    }

}
