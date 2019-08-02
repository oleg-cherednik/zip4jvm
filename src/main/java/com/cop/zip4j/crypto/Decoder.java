package com.cop.zip4j.crypto;

import com.cop.zip4j.model.CentralDirectory;
import lombok.NonNull;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public interface Decoder {

    Decoder NULL = new Decoder() {
        @Override
        public void decrypt(byte[] buf, int offs, int len) {
        }

        @Override
        public String toString() {
            return "<null>";
        }
    };

    void decrypt(byte[] buf, int offs, int len);

    default void checkChecksum(@NonNull CentralDirectory.FileHeader fileHeader, long crc32) {
    }

    default int getLen(long bytesRead, int len, long length) {
        return len;
    }

}
