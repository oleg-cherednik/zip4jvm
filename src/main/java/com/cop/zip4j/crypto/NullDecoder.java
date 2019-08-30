package com.cop.zip4j.crypto;

import com.cop.zip4j.model.entry.PathZipEntry;
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
    public long getCompressedSize(@NonNull PathZipEntry entry) {
        return entry.getCompressedSizeNew();
    }

    @Override
    public String toString() {
        return "<null>";
    }
}
