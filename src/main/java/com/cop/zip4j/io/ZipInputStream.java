package com.cop.zip4j.io;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.model.CentralDirectory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

@RequiredArgsConstructor
public class ZipInputStream extends InputStream {

    @NonNull
    private final InputStream in;
    @NonNull
    private final CentralDirectory.FileHeader fileHeader;
    @NonNull
    private final Decoder decoder;

    private final Checksum crc32 = new CRC32();

    @Override
    public int read() throws IOException {
        int b = in.read();

        if (b != -1)
            crc32.update(b);

        return b;
    }

    @Override
    public void close() throws IOException {
        in.close();
        decoder.checkChecksum(fileHeader, crc32.getValue());
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

}
