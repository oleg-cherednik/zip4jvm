package com.cop.zip4j.crypto;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.io.out.MarkDataOutput;
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
        public String toString() {
            return "<null>";
        }
    };

    void encrypt(@NonNull byte[] buf, int offs, int len);

    @Deprecated
    default void encrypt(@NonNull byte[] buf, int offs, int len, @NonNull MarkDataOutput out) throws  IOException {
        if (len == 0)
            return;
        encrypt(buf, offs, len);
        out.write(buf, offs, len);
    }

    default int writeDraft(byte[] buf, int offs, int len, MarkDataOutput out) throws IOException {
        return len;
    }

    void writeHeader(@NonNull DataOutput out) throws IOException;

    default void close(DataOutput out) throws IOException {
    }

}
