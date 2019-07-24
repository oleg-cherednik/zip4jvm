package com.cop.zip4j.crypto;

import com.cop.zip4j.exception.ZipException;
import com.cop.zip4j.io.SplitOutputStream;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
@SuppressWarnings("MethodCanBeVariableArityMethod")
public interface Encoder {

    // TODO should nobe here
    Encoder NULL = new Encoder() {
        @Override
        public void encode(byte[] buf, int offs, int len) {
        }

        @Override
        public void write(@NonNull SplitOutputStream out) throws IOException {
        }
    };

    default void encode(byte[] buf) throws ZipException {
        encode(buf, 0, buf.length);
    }

    void encode(@NonNull byte[] buf, int offs, int len);

    void write(@NonNull SplitOutputStream out) throws IOException;

}
