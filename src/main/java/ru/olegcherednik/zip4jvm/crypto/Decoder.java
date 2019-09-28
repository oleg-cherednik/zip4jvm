package ru.olegcherednik.zip4jvm.crypto;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public interface Decoder {

    Decoder NULL = new NullDecoder();

    void decrypt(byte[] buf, int offs, int len);

    long getCompressedSize(ZipEntry zipEntry);

    default void close(@NonNull DataInput in) throws IOException {
        /* nothing to close */
    }

}
