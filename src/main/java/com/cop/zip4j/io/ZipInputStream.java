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
    public int read(byte[] buf, int offs, int len) throws IOException {
        int readLen = in.read(buf, offs, len);

        if (readLen > 0 && engine != null)
            engine.updateCRC(buf, offs, readLen);

        return readLen;
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
