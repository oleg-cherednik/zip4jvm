package com.cop.zip4j.crypto;

import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.LocalFileHeader;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public interface Decoder {

    Decoder NULL = new NullDecoder();

    void decrypt(@NonNull byte[] buf, int offs, int len);

    long getCompressedSize(@NonNull LocalFileHeader localFileHeader);

    default void close(@NonNull DataInput in) throws IOException {
    }

}
