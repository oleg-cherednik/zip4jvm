package ru.olegcherednik.zip4jvm.utils;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * @author Oleg Cherednik
 * @since 23.11.2024
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ByteUtils {

    public static int getByte(long val, int i) {
        return (int) (val >> 8 * i) & 0xFF;
    }

    // ---------- read ----------

    public static int readByte(DataInput in) throws IOException {
        return read(in);
    }

    public static int readWord(DataInput in) throws IOException {
        int val = 0;

        for (int i = 0; i < 2; i++)
            val = read(in) << 8 * i | val;

        return val & 0xFFFF;
    }

    public static long readDword(DataInput in) throws IOException {
        long val = 0;

        for (int i = 0; i < 4; i++)
            val = (long) read(in) << 8 * i | val;

        return val & 0xFFFFFFFFL;
    }

    public static long readDword(byte[] buf, int offs) {
        long val = 0;

        for (int i = 0; i < 4; i++)
            val = (long) (buf[offs + i] & 0xFF) << 8 * i | val;

        return val & 0xFFFFFFFFL;
    }

    public static long readQword(DataInput in) throws IOException {
        long val = 0;

        for (int i = 0; i < 8; i++)
            val = (long) read(in) << 8 * i | val;

        return val;
    }

    public static BigInteger readBigInteger(int size, DataInput in) throws IOException {
        byte[] buf = new byte[size];

        for (int i = buf.length - 1; i >= 0; i--)
            buf[i] = (byte) read(in);

        return new BigInteger(buf);
    }

    private static int read(DataInput in) throws IOException {
        int b = in.read();

        if (b == IOUtils.EOF)
            throw new IOException("End Of File");

        return b & 0xFF;
    }

    // ---------- write ----------

    public static void writeByte(int val, OutputStream out) throws IOException {
        out.write(val);
    }

    public static void writeWord(int val, OutputStream out) throws IOException {
        for (int i = 0; i < 2; i++)
            out.write(getByte(val, i));
    }

    public static void writeDword(long val, OutputStream out) throws IOException {
        for (int i = 0; i < 4; i++)
            out.write(getByte(val, i));
    }

    public static void writeQword(long val, OutputStream out) throws IOException {
        for (int i = 0; i < 8; i++)
            out.write(getByte(val, i));
    }

}
