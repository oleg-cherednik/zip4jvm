package ru.olegcherednik.zip4jvm.io.out.data.xxx;

import ru.olegcherednik.zip4jvm.io.ByteOrder;

/**
 * @author Oleg Cherednik
 * @since 02.11.2024
 */
public final class DataConverter {

    private static final int OFFS_BYTE = 0;
    private static final int OFFS_WORD = 1;
    private static final int OFFS_DWORD = 3;
    private static final int OFFS_QWORD = 7;

    private final ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

    public void convertByte(int val, byte[] buf, int offs, int len) {
        byteOrder.fromLong(val, buf, offs, len);
    }

    public void convertWord(int val, byte[] buf, int offs, int len) {
        byteOrder.fromLong(val, buf, offs, len);
    }

    public void convertDword(long val, byte[] buf, int offs, int len) {
        byteOrder.fromLong(val, buf, offs, len);
    }

    public void convertQword(long val, byte[] buf, int offs, int len) {
        byteOrder.fromLong(val, buf, offs, len);
    }

}
