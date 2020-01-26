package ru.olegcherednik.zip4jvm.io.in.file;

import ru.olegcherednik.zip4jvm.io.in.RandomAccess;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 28.09.2019
 */
public interface DataInputFile extends Closeable, RandomAccess {

    long getOffs();

    long length();

    long convert(byte[] buf, int offs, int len);

    int read(byte[] buf, int offs, int len) throws IOException;

}
