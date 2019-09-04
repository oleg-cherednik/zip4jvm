package ru.olegcherednik.zip4jvm.crypto;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public interface Decoder {

    Decoder NULL = new NullDecoder();

    void decrypt(@NonNull byte[] buf, int offs, int len);

    long getCompressedSize(@NonNull ZipEntry entry);

    default void close(@NonNull DataInput in) throws IOException {
    }

}
