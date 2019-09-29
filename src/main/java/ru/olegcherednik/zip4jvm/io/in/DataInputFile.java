package ru.olegcherednik.zip4jvm.io.in;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 28.09.2019
 */
public interface DataInputFile extends Closeable {

    long getOffs();

    void skip(int bytes) throws IOException;

    long length() throws IOException;

    void seek(long pos) throws IOException;

    long convert(byte[] buf, int offs, int len);

    String readString(byte[] buf, Charset charset);

    int read(byte[] buf, int offs, int len) throws IOException;

    int readSignature() throws IOException;

}
