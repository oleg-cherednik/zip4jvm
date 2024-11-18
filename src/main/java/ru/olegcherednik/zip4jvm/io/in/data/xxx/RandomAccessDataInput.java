package ru.olegcherednik.zip4jvm.io.in.data.xxx;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 18.11.2024
 */
public interface RandomAccessDataInput extends XxxDataInput, Closeable {

    void seek(int diskNo, long relativeOffs) throws IOException;

    void seek(long absOffs) throws IOException;

    void seek(String id) throws IOException;

    // TODO not sure this method belongs to random access
    long convertToAbsoluteOffs(int diskNo, long relativeOffs);

    // TODO this should be inherited from InputStream and not be here
    int read(byte[] buf, int offs, int len) throws IOException;

    long availableLong() throws IOException;

    boolean isDwordSignature(int expected) throws IOException;

    void backward(int bytes) throws IOException;

}
