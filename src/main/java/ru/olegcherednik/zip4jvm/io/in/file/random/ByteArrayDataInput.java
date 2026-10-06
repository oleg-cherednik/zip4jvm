package ru.olegcherednik.zip4jvm.io.in.file.random;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.in.BaseDataInput;

import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 05.10.2026
 */
public class ByteArrayDataInput extends BaseDataInput {

    private final byte[] buf;
    private long offs;

    public ByteArrayDataInput(byte[] buf) {
        this.buf = Arrays.copyOf(buf, buf.length);
    }

    @Override
    public ByteOrder getByteOrder() {
        return ByteOrder.LITTLE_ENDIAN;
    }

    @Override
    public long getAbsOffs() {
        return offs;
    }

    @Override
    public long skip(long bytes) {
        long b = Math.min(bytes, buf.length - offs);
        offs += b;
        return b;
    }

    @Override
    public void mark(String id) {

    }

    @Override
    public long getMark(String id) {
        return 0;
    }

    @Override
    public long getMarkSize(String id) {
        return 0;
    }

    @Override
    public int read(byte[] buf, int offs, int len) {
        int maxLen = (int) Math.min(len, this.buf.length - this.offs);

        for (int i = 0; i < maxLen; i++) {
            buf[offs + i] = this.buf[(int) (this.offs + i)];
        }

        this.offs += maxLen;
        return maxLen;
    }
}
