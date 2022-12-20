package ru.olegcherednik.zip4jvm.io.in.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseDataInputNew implements DataInputNew {

    private static final int OFFS_BYTE = 0;
    private static final int OFFS_WORD = 1;
    private static final int OFFS_DWORD = 3;
    private static final int OFFS_QWORD = 7;

    protected static final ThreadLocal<byte[]> THREAD_LOCAL_BUF = ThreadLocal.withInitial(() -> new byte[15]);

    @Override
    public int byteSize() {
        return 1;
    }

    @Override
    public int wordSize() {
        return 2;
    }

    @Override
    public int dwordSize() {
        return 4;
    }

    @Override
    public int qwordSize() {
        return 8;
    }

    @Override
    public int readByte() throws IOException {
        return (int)readAndToLong(OFFS_BYTE, byteSize());
    }

    @Override
    public int readWord() throws IOException {
        return (int)readAndToLong(OFFS_WORD, wordSize());
    }

    @Override
    public long readDword() throws IOException {
        return readAndToLong(OFFS_DWORD, dwordSize());
    }

    @Override
    public long readQword() throws IOException {
        return readAndToLong(OFFS_QWORD, qwordSize());
    }

    private long readAndToLong(int offs, int len) throws IOException {
        byte[] buf = THREAD_LOCAL_BUF.get();
        read(buf, offs, len);
        return toLong(buf, offs, len);
    }

//    @Override
//    public String readNumber(int bytes, int radix) throws IOException {
//        if (bytes <= 0)
//            return null;
//
//        byte[] buf = readBytes(bytes);
//
//        String hexStr = IntStream.rangeClosed(1, bytes)
//                                 .map(i -> buf[buf.length - i] & 0xFF)
//                                 .mapToObj(Integer::toHexString)
//                                 .collect(Collectors.joining());
//
//        return new BigInteger(hexStr, radix).toString();
//    }

//    private long readAndToLong(int offs, int len) throws IOException {
//        byte[] buf = THREAD_LOCAL_BUF.get();
//        read(buf, offs, len);
//        return toLong(buf, offs, len);
//    }

    @Override
    public String readString(int length, Charset charset) throws IOException {
        byte[] buf = readBytes(length);
        return buf.length == 0 ? null : new String(buf, charset);
    }

    @Override
    public byte[] readBytes(int total) throws IOException {
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

//    @Override
//    public void mark(String id) {
//        map.put(id, getAbsoluteOffs());
//    }

//    @Override
//    public long getMark(String id) {
//        if (map.containsKey(id))
//            return map.get(id);
//        throw new Zip4jvmException("Cannot find mark: " + id);
//    }

//    @Override
//    public long getMarkSize(String id) {
//        return getAbsoluteOffs() - getMark(id);
//    }

//    @Override
//    public void seek(String id) throws IOException {
//        seek(getMark(id));
//    }
}
