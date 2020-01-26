package ru.olegcherednik.zip4jvm.io.in.file;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 28.09.2019
 */
public interface DataInputFile extends Closeable {

    long getOffs();

    long skip(long bytes) throws IOException;

    long length();

    void seek(long pos) throws IOException;

    long convert(byte[] buf, int offs, int len);

    int read(byte[] buf, int offs, int len) throws IOException;

}
