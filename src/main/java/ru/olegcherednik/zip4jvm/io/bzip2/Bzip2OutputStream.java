
package ru.olegcherednik.zip4jvm.io.bzip2;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
public class Bzip2OutputStream extends OutputStream {

    private final DataOutput out;
    private Bzip2CompressorOutputStream bzip2;

    public Bzip2OutputStream(DataOutput out, long uncompressedSize) throws IOException {
        this.out = out;
    }

    @Override
    public void write(int b) throws IOException {
        create();
        bzip2.write(b);
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        create();
        bzip2.write(buf, off, len);
    }

    private void create() throws IOException {
        if (bzip2 != null)
            return;

        bzip2 = new Bzip2CompressorOutputStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                out.writeByte(b);
            }
        });
    }

    @Override
    public void close() throws IOException {
        if (bzip2 != null)
            bzip2.close();
    }

}
