package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.io.in.RandomAccess;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
public interface DataInput extends Closeable, RandomAccess {

    default int dwordSignatureSize() {
        return dwordSize();
    }

    int byteSize();

    int wordSize();

    int dwordSize();

    int qwordSize();

    /** Retrieves offs starting from the beginning of the first disk */
    long getAbsoluteOffs();

    long convertToAbsoluteOffs(int diskNo, long relativeOffs);

    /** Retrieves offs starting from the beginning of the current disk */
    long getDiskRelativeOffs();

    SrcZip getSrcZip();

    SrcZip.Disk getDisk();

    default int readWordSignature() throws IOException {
        return readWord();
    }

    default int readDwordSignature() throws IOException {
        return (int)readDword();
    }

    int readWord() throws IOException;

    long readDword() throws IOException;

    long readQword() throws IOException;

    String readNumber(int bytes, int radix) throws IOException;

    String readString(int length, Charset charset) throws IOException;

    int readByte() throws IOException;

    byte[] readBytes(int total) throws IOException;

    long size() throws IOException;

    default void backward(int bytes) throws IOException {
        seek(getAbsoluteOffs() - bytes);
    }

    int read(byte[] buf, int offs, int len) throws IOException;

    /* this is technical method; create {@literal long} from {@literal byte[]} */
    @Deprecated
    long toLong(byte[] buf, int offs, int len);

    void mark(String id);

    long getMark(String id);

    void seek(int diskNo, long relativeOffs) throws IOException;

    void seek(String id) throws IOException;

}
