package ru.olegcherednik.zip4jvm.io.in.file;

import ru.olegcherednik.zip4jvm.io.Endianness;

/**
 * @author Oleg Cherednik
 * @since 28.09.2024
 */
public final class LittleEndian implements Endianness {

    public static final LittleEndian INSTANCE = new LittleEndian();

    @Override
    public long getLong(byte[] buf, int offs, int len) {
        long res = 0;

        for (int i = offs + len - 1; i >= offs; i--)
            res = res << 8 | buf[i] & 0xFF;

        return res;
    }

}
