package com.cop.zip4j.crypto;

import com.cop.zip4j.io.out.DataOutput;
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
        public void writeHeader(@NonNull DataOutput out) throws IOException {
        }

        @Override
        public int writeDraft(byte[] buf, int offs, int len, DataOutput out) {
            return len;
        }

        @Override
        public String toString() {
            return "<null>";
        }
    };

    void encrypt(@NonNull byte[] buf, int offs, int len);

    void writeHeader(@NonNull DataOutput out) throws IOException;

    @Deprecated
    default int writeDraft(byte[] buf, int offs, int len, DataOutput out) throws IOException {
        return len;
    }

    default void close(DataOutput out) throws IOException {
    }

}
