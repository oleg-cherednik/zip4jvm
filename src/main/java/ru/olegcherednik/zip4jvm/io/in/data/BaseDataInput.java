package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.io.ByteOrder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 15.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseDataInput extends DataInput {

    protected final DataInput in;

    // ---------- DataInput ----------

    @Override
    public long getAbsoluteOffs() {
        return in.getAbsoluteOffs();
    }

    @Override
    public void seek(long absoluteOffs) {
        in.seek(absoluteOffs);
    }

    @Override
    public ByteOrder getByteOrder() {
        return in.getByteOrder();
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return in.read(buf, offs, len);
    }

    @Override
    public long size() {
        return in.size();
    }

    @Override
    public int readByte() throws IOException {
        return in.readByte();
    }

    @Override
    public int readWord() {
        return in.readWord();
    }

    @Override
    public long readDword() {
        return in.readDword();
    }

    @Override
    public long readQword() {
        return in.readQword();
    }

    @Override
    public byte[] readBytes(int total) {
        return in.readBytes(total);
    }

    @Override
    public String readString(int length, Charset charset) {
        return in.readString(length, charset);
    }

    @Override
    public String readNumber(int bytes, int radix) {
        return in.readNumber(bytes, radix);
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

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        in.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return in.toString();
    }

}
