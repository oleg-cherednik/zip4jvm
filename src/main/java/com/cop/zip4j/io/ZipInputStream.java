package com.cop.zip4j.io;

import com.cop.zip4j.engine.UnzipEngine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class ZipInputStream extends InputStream {

    private final InputStream in;
    @NonNull
    private final UnzipEngine engine;

    @Override
    public int read() throws IOException {
        int readByte = in.read();

        if (readByte != -1)
            engine.updateCRC(readByte);

        return readByte;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readLen = in.read(b, off, len);

        if (readLen > 0 && engine != null)
            engine.updateCRC(b, off, readLen);

        return readLen;
    }

    /**
     * Closes the input stream and releases any resources.
     * This method also checks for the CRC of the extracted file.
     * If CRC check has to be skipped use close(boolean skipCRCCheck) method
     *
     * @throws IOException
     */
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
