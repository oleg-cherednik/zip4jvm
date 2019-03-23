package net.lingala.zip4j.util;

import lombok.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 01.03.2019
 */
public final class LittleEndianBuffer implements Closeable {
    private final List<String> bytes = new ArrayList<>();
    private final byte[] intByte = new byte[4];
    private final byte[] shortByte = new byte[2];
    private final byte[] longByte = new byte[8];

    public long flushInto(OutputStream out) throws IOException {
        byte[] buf = byteArrayListToByteArray();
        out.write(buf);
        return buf.length;
    }

    // 2 bytes (16 bit)
    public LittleEndianBuffer writeWord(short val) {
        return writeShort(val);
    }

    // 4 bytes (32 bit)
    public LittleEndianBuffer writeDword(int val) {
        return writeInt(val);
    }

    public LittleEndianBuffer writeDword(long val) {
        return writeLongAsInt(val);
    }

    // 8 bytes (64 bit)
    public LittleEndianBuffer writeQword(long val) {
        return writeLong(val);
    }

    public LittleEndianBuffer writeInt(int val) {
        Raw.writeIntLittleEndian(intByte, 0, val);
        copyByteArrayToArrayList(intByte);
        return this;
    }

    public LittleEndianBuffer writeShort(short val) {
        Raw.writeShortLittleEndian(shortByte, 0, val);
        copyByteArrayToArrayList(shortByte);
        return this;
    }

    public LittleEndianBuffer writeLong(long val) {
        Raw.writeLongLittleEndian(longByte, 0, val);
        copyByteArrayToArrayList(longByte);
        return this;
    }

    public LittleEndianBuffer writeLongAsInt(long val) {
        Raw.writeLongLittleEndian(longByte, 0, val);
        System.arraycopy(longByte, 0, intByte, 0, 4);
        copyByteArrayToArrayList(intByte);
        return this;
    }

    public LittleEndianBuffer writeBytes(byte... buf) {
        copyByteArrayToArrayList(buf);
        return this;
    }

    public void copyByteArrayToArrayList(@NonNull byte[] buf) {
        for (int i = 0; i < buf.length; i++)
            bytes.add(Byte.toString(buf[i]));
    }

    public byte[] byteArrayListToByteArray() {
        byte[] buf = new byte[bytes.size()];

        for (int i = 0; i < bytes.size(); i++)
            buf[i] = Byte.parseByte(bytes.get(i));

        return buf;
    }

    // ---------- Closeable ----------

    @Override
    public void close() throws IOException {

    }

}
