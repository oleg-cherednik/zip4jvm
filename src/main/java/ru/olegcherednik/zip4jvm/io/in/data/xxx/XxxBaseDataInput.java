package ru.olegcherednik.zip4jvm.io.in.data.xxx;

import ru.olegcherednik.zip4jvm.io.ByteOrder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;

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
    public String readString(int length, Charset charset) throws IOException {
        return in.readString(length, charset);
    }

    @Override
    public byte[] readBytes(int total) throws IOException {
        return in.readBytes(total);
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

}
