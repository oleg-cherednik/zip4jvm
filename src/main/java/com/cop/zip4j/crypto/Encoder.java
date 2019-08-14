package com.cop.zip4j.crypto;

import com.cop.zip4j.io.out.DataOutput;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public interface Encoder {

    Encoder NULL = new NullEncoder();

    void writeHeader(@NonNull DataOutput out) throws IOException;

    void encrypt(@NonNull byte[] buf, int offs, int len);

    default void close(@NonNull DataOutput out) throws IOException {
    }

}
