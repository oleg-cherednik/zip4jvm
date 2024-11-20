package ru.olegcherednik.zip4jvm.io.in.data.xxx;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.Marker;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

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

    default String readString(int length, Charset charset) throws IOException {
        byte[] buf = readBytes(length);
        return buf.length == 0 ? null : new String(buf, charset);
    }

    default byte[] readBytes(int total) throws IOException {
        if (total <= 0)
            return ArrayUtils.EMPTY_BYTE_ARRAY;

        byte[] buf = new byte[total];
        int n = read(buf, 0, buf.length);

        if (n == IOUtils.EOF)
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        if (n < total)
            return Arrays.copyOfRange(buf, 0, n);
        return buf;
    }

    String readNumber(int bytes, int radix) throws IOException;

    long skip(long bytes) throws IOException;

    default int readWordSignature() throws IOException {
        return readWord();
    }

    default int readDwordSignature() throws IOException {
        return (int) readDword();
    }

}
