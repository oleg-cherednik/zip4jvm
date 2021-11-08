package ru.olegcherednik.zip4jvm.io.zstd;

import lombok.Getter;
import lombok.Setter;

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
    @Setter
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

    public long getInt() {
        long value = UnsafeUtil.getInt(buf, offs) & 0xFFFF_FFFFL;
        offs += SIZE_OF_INT;
        return value;
    }

    public int get3Bytes() {
        int a = getShort();
        int b = getByte();
        return b << 16 | a;
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
