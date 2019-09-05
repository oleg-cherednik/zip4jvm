package ru.olegcherednik.zip4jvm.crypto;

import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import lombok.NonNull;

/**
 * @author Oleg Cherednik
 * @since 11.08.2019
 */
final class NullDecoder implements Decoder {

    @Override
    public void decrypt(@NonNull byte[] buf, int offs, int len) {
    }

    @Override
    public long getCompressedSize(@NonNull ZipEntry entry) {
        return entry.getCompressedSize();
    }

    @Override
    public String toString() {
        return "<null>";
    }
}