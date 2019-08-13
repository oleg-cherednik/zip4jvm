package com.cop.zip4j.crypto;

import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.LocalFileHeader;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public interface Decoder {

    Decoder NULL = new NullDecoder();

    void decrypt(byte[] buf, int offs, int len);

    default int getLen(long bytesRead, int len, long length) {
        return len;
    }

    default long getCompressedSize(@NonNull LocalFileHeader localFileHeader) {
        return localFileHeader.getCompressedSize();
    }

    default long getOffs(@NonNull LocalFileHeader localFileHeader) {
        return localFileHeader.getOffs();
    }

    @Deprecated
    default int decryptAndRead(@NonNull byte[] buf, int offs, int len, @NonNull DataInput in) throws IOException {
        if (len == 0)
            return 0;

        len = in.read(buf, offs, len);

        if (len != IOUtils.EOF)
            decrypt(buf, offs, len);

        return len;
    }

    default int _read(byte[] buf, int offs, int len, DataInput in) throws IOException {
        return decryptAndRead(buf, offs, len, in);
    }

    default void close(DataInput in) throws IOException {
    }

}
