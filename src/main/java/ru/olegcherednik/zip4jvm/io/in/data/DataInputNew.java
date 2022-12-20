package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.io.in.RandomAccess;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
public interface DataInputNew extends RandomAccess {

    int byteSize();

    int wordSize();

    int dwordSize();

    int qwordSize();


    long getAbsoluteOffs();

    int readByte() throws IOException;

    int readWord() throws IOException;

    long readDword() throws IOException;

    long readQword() throws IOException;

    byte[] readBytes(int total) throws IOException;

    String readString(int length, Charset charset) throws IOException;

    int read(byte[] buf, int offs, int len) throws IOException;

    default int dwordSignatureSize() {
        return dwordSize();
    }

    default int readWordSignature() throws IOException {
        return readWord();
    }

    default int readDwordSignature() throws IOException {
        return (int)readDword();
    }


    default void backward(int bytes) throws IOException {
        seek(getAbsoluteOffs() - bytes);
    }

    /* this is technical method; create {@literal long} from {@literal byte[]} */
    @Deprecated
    long toLong(byte[] buf, int offs, int len);

}
