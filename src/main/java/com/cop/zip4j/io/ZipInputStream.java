package com.cop.zip4j.io;

import com.cop.zip4j.engine.UnzipEngine;
import com.cop.zip4j.exception.Zip4jException;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class ZipInputStream extends InputStream {

    private final InputStream in;
    private final UnzipEngine unzipEngine;

    @Override
    public int read() throws IOException {
        int readByte = in.read();

        if (readByte != -1)
            unzipEngine.updateCRC(readByte);

        return readByte;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readLen = in.read(b, off, len);

        if (readLen > 0 && unzipEngine != null)
            unzipEngine.updateCRC(b, off, readLen);

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
        close(false);
    }

    /**
     * Closes the input stream and releases any resources.
     * If skipCRCCheck flag in set to true, this method skips CRC Check
     * of the extracted file
     *
     * @throws IOException
     */
    public void close(boolean skipCRCCheck) throws IOException {
        try {
            in.close();
            if (!skipCRCCheck && unzipEngine != null) {
                unzipEngine.checkCRC();
            }
        } catch(Zip4jException e) {
            throw new IOException(e.getMessage());
        }
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
