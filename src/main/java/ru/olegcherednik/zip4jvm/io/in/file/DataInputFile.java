package ru.olegcherednik.zip4jvm.io.in.file;

import ru.olegcherednik.zip4jvm.io.in.RandomAccess;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 28.09.2019
 */
public interface DataInputFile extends Closeable, RandomAccess {

    long getAbsOffs();

    long length();

    int read(byte[] buf, int offs, int len) throws IOException;

    long toLong(byte[] buf, int offs, int len);

    // TODO temporary
    @Deprecated
    SrcZip getSrcZip();

    // TODO temporary
    @Deprecated
    int getDisk();

}
