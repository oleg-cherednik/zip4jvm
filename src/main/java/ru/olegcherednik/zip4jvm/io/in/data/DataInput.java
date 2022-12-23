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
public interface DataInput extends RandomAccess, Mark, Closeable, ReadBuffer {

    int byteSize();

    int wordSize();

    int dwordSize();

    int qwordSize();

    long getAbsoluteOffs();

    // TODO looks like should be available
    long size();

    int readByte();

    int readWord();

    long readDword();

    long readQword();

    byte[] readBytes(int total);

    String readString(int length, Charset charset);

    String readNumber(int bytes, int radix);

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
