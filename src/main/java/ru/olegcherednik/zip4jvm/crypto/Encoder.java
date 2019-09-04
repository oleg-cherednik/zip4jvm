package ru.olegcherednik.zip4jvm.crypto;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public interface Encoder {

    Encoder NULL = new NullEncoder();

    void writeEncryptionHeader(@NonNull DataOutput out) throws IOException;

    void encrypt(@NonNull byte[] buf, int offs, int len);

    default void close(@NonNull DataOutput out) throws IOException {
    }

}
