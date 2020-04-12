package ru.olegcherednik.zip4jvm.io.bzip2;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
public class Bzip2InputStream extends InputStream {

    private final BZip2CompressorInputStream bzip2;

    public Bzip2InputStream(DataInput in, long uncompressedSize) throws IOException {
        bzip2 = new BZip2CompressorInputStream(new InputStream() {
            @Override
            public int read() throws IOException {
                return in.readByte();
            }
        });
    }

    @Override
    public int read() throws IOException {
        return bzip2.read();
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return bzip2.read(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        bzip2.close();
    }

}
