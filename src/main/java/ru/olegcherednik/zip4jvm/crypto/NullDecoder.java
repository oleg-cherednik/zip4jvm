package ru.olegcherednik.zip4jvm.crypto;

import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

/**
 * @author Oleg Cherednik
 * @since 11.08.2019
 */
final class NullDecoder implements Decoder {

    @Override
    public void decrypt(byte[] buf, int offs, int len) {
        /* nothing to decrypt */
    }

    @Override
    public long getCompressedSize(ZipEntry zipEntry) {
        return zipEntry.getCompressedSize();
    }

    @Override
    public String toString() {
        return "<null>";
    }
}
