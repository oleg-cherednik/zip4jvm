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

    // Retrieves offs starting from the beginning of the first disk
    long getAbsoluteOffs();

    long convertToAbsoluteOffs(int diskNo, long relativeOffs);

    // Retrieves offs starting from the beginning of the current disk
    long getDiskRelativeOffs();

    long size();

    int read(byte[] buf, int offs, int len) throws IOException;

    long toLong(byte[] buf, int offs, int len);

    void seek(int diskNo, long relativeOffs) throws IOException;

    SrcZip getSrcZip();

    SrcZip.Disk getDisk();

}
