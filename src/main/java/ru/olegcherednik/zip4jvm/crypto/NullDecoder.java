package ru.olegcherednik.zip4jvm.crypto;

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
    public long getDataCompressedSize(long compressedSize) {
        return compressedSize;
    }

    @Override
    public String toString() {
        return "<null>";
    }
}
