package ru.olegcherednik.zip4jvm.io.zstd;

import lombok.Getter;

import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_BYTE;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_INT;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_LONG;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_SHORT;

/**
 * @author Oleg Cherednik
 * @since 08.11.2021
 */
@Getter
public final class Buffer {

    private final byte[] buf;
    private int offs;

    public Buffer(int size) {
        buf = new byte[size];
    }

    public Buffer(byte[] buf) {
        this.buf = buf;
    }

    public void skip(int size) {
        offs += size;
    }

    public void seek(int offs) {
        this.offs = offs;
    }

    public int get3Bytes() {
        int one = getByte();
        int two = getByte();
        int three = getByte();
        return three << 16 | two << 8 | one;
    }

    public long get5Bytes() {
        int one = getByte();
        int two = getByte();
        int three = getByte();
        int four = getByte();
        long five = getByte();
        return five << 32 | four << 24 | three << 16 | two << 8 | one;
    }

    public long getInt() {
        long value = UnsafeUtil.getInt(buf, offs) & 0xFFFF_FFFFL;
        offs += SIZE_OF_INT;
        return value;
    }

    public long getLong() {
        long value = UnsafeUtil.getLong(buf, offs);
        offs += SIZE_OF_LONG;
        return value;
    }

    public int getByte() {
        int value = UnsafeUtil.getByte(buf, offs) & 0xFF;
        offs += SIZE_OF_BYTE;
        return value;
    }

    public int getByteNoMove() {
        int value = getByte();
        offs -= SIZE_OF_BYTE;
        return value;
    }

    public int getShort() {
        int value = UnsafeUtil.getShort(buf, offs) & 0xFFFF;
        offs += SIZE_OF_SHORT;
        return value;
    }

    public int putInt(int value) {
        UnsafeUtil.putInt(buf, offs, value);
        offs += SIZE_OF_INT;
        return SIZE_OF_INT;
    }

    public int putByte(byte value) {
        UnsafeUtil.putByte(buf, offs, value);
        offs += SIZE_OF_BYTE;
        return SIZE_OF_BYTE;
    }

    public int putShort(short value) {
        UnsafeUtil.putShort(buf, offs, value);
        offs += SIZE_OF_SHORT;
        return SIZE_OF_SHORT;
    }

    @Override
    public String toString() {
        return "offs: " + offs;
    }

}
