package io.airlift.compress;

import io.airlift.compress.zstd.Constants;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static io.airlift.compress.zstd.UnsafeUtil.UNSAFE;
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
    private long outputAddress = ARRAY_BYTE_BASE_OFFSET;
    @Getter
    private long outputLimit;

    public Foo(int maxLength) {
        compressed = new byte[maxLength];
        buf = ByteBuffer.wrap(compressed);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        outputLimit = outputAddress + compressed.length;
    }

    public long getOffs() {
        return buf.position() + ARRAY_BYTE_BASE_OFFSET;
    }

    public void setOffs(long offs) {
        buf.position((int) (offs - ARRAY_BYTE_BASE_OFFSET));
    }

    public int getMaxLength() {
        return compressed.length;
    }

    public int putInt(int value) {
        buf.putInt(value);
        return Constants.SIZE_OF_INT;
    }

    public int putLong(long value) {
        buf.putLong(value);
        return Constants.SIZE_OF_LONG;
    }

    public int putByte(byte value) {
        buf.put(value);
        return Constants.SIZE_OF_BYTE;
    }

    public int putShort(short value) {
        buf.putShort(value);
        return Constants.SIZE_OF_SHORT;
    }

    public long copyMemory(Object srcBase, long srcOffset, long bytes) {
//        buf.put((byte[]) srcBase, (int)srcOffset, (int)bytes);
        UNSAFE.copyMemory(srcBase, srcOffset, compressed, getOffs(), bytes);
        setOffs(getOffs() + bytes);
        return bytes;
    }

    public void copyMemory(byte[] src, int bytes) {
        System.arraycopy(src, 0, compressed, buf.position(), bytes);
    }

    @Override
    public String toString() {
        return "offs: " + getOffs();
    }
}
