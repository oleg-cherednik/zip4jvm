package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.io.Endianness;
import ru.olegcherednik.zip4jvm.io.in.RandomAccess;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Represents any source that can be treated as data input for read byte, word,
 * dword or byte array
 *
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
public interface DataInputNew extends RandomAccess, Mark, Closeable, ReadBuffer {

    int byteSize();

    int wordSize();

    int dwordSize();

    int qwordSize();

    long getAbsoluteOffs();

    // TODO looks like should be available
    long size() throws IOException;

    int readByte() throws IOException;

    int readWord() throws IOException;

    long readDword() throws IOException;

    long readQword() throws IOException;

    byte[] readBytes(int total) throws IOException;

    String readString(int length, Charset charset) throws IOException;

    String readNumber(int bytes, int radix) throws IOException;

    Endianness getEndianness();

    @Override
    default void close() throws IOException {
        /* nothing to close */
    }

    // TODO signature should be read in normal order

    default int dwordSignatureSize() {
        return dwordSize();
    }

    default int readWordSignature() throws IOException {
        return readWord();
    }

    default int readDwordSignature() throws IOException {
        return (int)readDword();
    }

    // ---------- RandomAccess ----------

    @Override
    default void backward(int bytes) {
        seek(getAbsoluteOffs() - bytes);
    }

}
