package ru.olegcherednik.zip4jvm.io.in.data.xxx;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.Marker;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 18.11.2024
 */
public interface XxxDataInput extends Marker, ReadBuffer, Closeable {

    ByteOrder getByteOrder();

    long getAbsOffs();

    int readByte() throws IOException;

    int readWord() throws IOException;

    long readDword() throws IOException;

    long readQword() throws IOException;

    String readString(int length, Charset charset) throws IOException;

    byte[] readBytes(int total) throws IOException;

    String readNumber(int bytes, int radix) throws IOException;

    long skip(long bytes) throws IOException;

    default int readWordSignature() throws IOException {
        return readWord();
    }

    default int readDwordSignature() throws IOException {
        return (int) readDword();
    }

}
