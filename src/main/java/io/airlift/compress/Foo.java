package io.airlift.compress;

import io.airlift.compress.zstd.Constants;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET;

/**
 * @author Oleg Cherednik
 * @since 16.07.2026
 */
public class Foo {

    @Getter
    private byte[] compressed;
    private ByteBuffer buf;
    @Getter
    private int offs = ARRAY_BYTE_BASE_OFFSET;
    @Getter
    private long outputAddress = ARRAY_BYTE_BASE_OFFSET;
    @Getter
    private long outputLimit;

    public Foo(int maxLength) {
        compressed = new byte[maxLength];
        buf = ByteBuffer.wrap(compressed);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        outputLimit = outputAddress + compressed.length;
    }

    public void incOffs(int inc) {
        for(int i = 0; i < inc; i++)
            putByte((byte)0);
    }

    public int getMaxLength() {
        return compressed.length;
    }

    public int putInt(int value) {
        buf.putInt(value);
        offs += Constants.SIZE_OF_INT;
        return Constants.SIZE_OF_INT;
    }

    public int putByte(byte value) {
        buf.put(value);
        offs += Constants.SIZE_OF_BYTE;
        return Constants.SIZE_OF_BYTE;
    }

    public int putShort(short value) {
        buf.putShort(value);
        offs += Constants.SIZE_OF_SHORT;
        return Constants.SIZE_OF_SHORT;
    }

    @Override
    public String toString() {
        return "offs: " + offs;
    }
}
