package com.cop.zip4j.io;

import com.cop.zip4j.engine.UnzipEngine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

/**
 * It's created for separate ZipEntry. Therefore CRC calculation could be incapsulated in this file instead of UnzipEngine
 */
@RequiredArgsConstructor
public class ZipInputStream extends InputStream {

    private final InputStream in;
    @NonNull
    private final UnzipEngine engine;

    @Override
    public int read() throws IOException {
        int b = in.read();

        if (b != -1)
            engine.updateCRC(b);

        return b;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        return super.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        in.close();
        engine.checkCRC();
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
