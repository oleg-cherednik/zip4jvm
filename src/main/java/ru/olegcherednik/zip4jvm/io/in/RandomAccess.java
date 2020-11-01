package ru.olegcherednik.zip4jvm.io.in;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 26.01.2020
 */
public interface RandomAccess {

    // TODO looks like when delegate is encryption (e.g. AES), then it's mandatory not skip, but read bytes to keep counters up to date
    long skip(long bytes) throws IOException;

    void seek(long absoluteOffs) throws IOException;

}
