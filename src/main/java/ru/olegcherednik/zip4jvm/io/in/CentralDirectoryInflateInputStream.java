package ru.olegcherednik.zip4jvm.io.in;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.Decoder;

import java.io.IOException;
import java.util.zip.Inflater;

/**
 * @author Oleg Cherednik
 * @since 10.10.2019
 */
@RequiredArgsConstructor
public class CentralDirectoryInflateInputStream implements DataInputFile {

    private final Decoder decoder;
    private final byte[] buf = new byte[1024 * 4];
    private final Inflater inflater = new Inflater(true);

    @Override
    public long getOffs() {
        return 0;
    }

    @Override
    public void skip(int bytes) throws IOException {
        int a = 0;
        a++;
    }

    @Override
    public long length() throws IOException {
        return 0;
    }

    @Override
    public void seek(long pos) throws IOException {
        int a = 0;
        a++;
    }

    @Override
    public long convert(byte[] buf, int offs, int len) {
        return 0;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return 0;
    }

    @Override
    public int readSignature() throws IOException {
        return 0;
    }

    @Override
    public void close() throws IOException {
        int a = 0;
        a++;
    }
}
