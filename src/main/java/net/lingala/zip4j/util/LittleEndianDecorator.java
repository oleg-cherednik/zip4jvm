package net.lingala.zip4j.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 21.02.2019
 */
@RequiredArgsConstructor
public final class LittleEndianDecorator {
    private final byte[] buf;
    @Getter
    private int offs;

    private final byte[] intByte = new byte[4];
    private final byte[] shortByte = new byte[2];
    private final byte[] longByte = new byte[8];

    public byte readByte() throws IOException {
        return (byte)buf[offs++];
    }

    public short readShort() throws IOException {
        short val = (short)Raw.readShortLittleEndian(buf, offs);
        offs += 2;
        return val;
    }

    public int readInt() throws IOException {
        int val = Raw.readIntLittleEndian(buf, offs);
        offs += 4;
        return val;
    }

    public long readLong() throws IOException {
        long val = Raw.readLongLittleEndian(buf, offs);
        offs += 8;
        return val;
    }

    public String readString(int length) throws IOException {
        if (length <= 0)
            return null;

        byte[] buf = new byte[length];
        System.arraycopy(this.buf, offs, buf, 0, length);

        offs += length;
        return new String(buf, Zip4jUtil.detectCharset(buf));
    }

    @Override
    public String toString() {
        return "size: " + offs;
    }

}
