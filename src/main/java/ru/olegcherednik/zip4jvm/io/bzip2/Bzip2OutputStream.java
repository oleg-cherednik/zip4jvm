
package ru.olegcherednik.zip4jvm.io.bzip2;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;

import java.io.IOException;
import java.io.OutputStream;

import static ru.olegcherednik.zip4jvm.io.bzip2.Bzip2CompressorOutputStream.MAX_BLOCKSIZE;

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
public class Bzip2OutputStream extends OutputStream {

    private final Bzip2CompressorOutputStream bzip;

    public Bzip2OutputStream(DataOutput out, long uncompressedSize) throws IOException {
        bzip = new Bzip2CompressorOutputStream(out, MAX_BLOCKSIZE);
    }

    @Override
    public void write(int b) throws IOException {
        bzip.write(b);
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        bzip.write(buf, off, len);
    }

    @Override
    public void close() throws IOException {
        bzip.close();
    }

}
