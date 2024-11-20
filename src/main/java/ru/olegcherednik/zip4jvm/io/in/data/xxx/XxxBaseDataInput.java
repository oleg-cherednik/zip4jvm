package ru.olegcherednik.zip4jvm.io.in.data.xxx;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.utils.ThreadLocalBuffer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 19.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class XxxBaseDataInput implements XxxDataInput {

    protected final XxxDataInput in;

    @Override
    public ByteOrder getByteOrder() {
        return in.getByteOrder();
    }

    @Override
    public long getAbsOffs() {
        return in.getAbsOffs();
    }

    @Override
    public int readByte() throws IOException {
        return in.readByte();
    }

    @Override
    public int readWord() throws IOException {
        return in.readWord();
    }

    @Override
    public long readDword() throws IOException {
        return in.readDword();
    }

    @Override
    public long readQword() throws IOException {
        return in.readQword();
    }

    @Override
    public String readNumber(int bytes, int radix) throws IOException {
        return in.readNumber(bytes, radix);
    }

    @Override
    public long skip(long bytes) throws IOException {
        return in.skip(bytes);
    }

    @Override
    public void mark(String id) {
        in.mark(id);
    }

    @Override
    public long getMark(String id) {
        return in.getMark(id);
    }

    @Override
    public long getMarkSize(String id) {
        return in.getMarkSize(id);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return in.read(buf, offs, len);
    }

    @Override
    public final int read() throws IOException {
        byte[] buf = ThreadLocalBuffer.getOne();
        read(buf, 0, buf.length);
        return buf[0] & 0xFF;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public String toString() {
        return in.toString();
    }

}
