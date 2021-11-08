package ru.olegcherednik.zip4jvm.io.zstd;

import lombok.Getter;

import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_BYTE;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_INT;
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

    public void incOffs(int size) {
        offs += size;
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

}
