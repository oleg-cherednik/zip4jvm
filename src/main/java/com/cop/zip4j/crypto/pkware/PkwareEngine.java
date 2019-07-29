package com.cop.zip4j.crypto.pkware;

import lombok.NonNull;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
@SuppressWarnings({ "MethodCanBeVariableArityMethod", "NewMethodNamingConvention" })
class PkwareEngine {

    private static final int[] CRC_TABLE = createCrcTable();

    private final int[] keys;

    public PkwareEngine(@NonNull char[] password) {
        keys = createKeys(password);
    }

    public void updateKeys(byte b) {
        updateKeys(keys, b);
    }

    public byte encrypt(byte b) {
        byte cipher = (byte)(stream() ^ b & 0xFF);
        updateKeys(b);
        return cipher;
    }

    public byte decrypt() {
        int tmp = keys[2] | 2;
        return (byte)((tmp * (tmp ^ 1)) >>> 8);
    }

    private byte stream() {
        int tmp = keys[2] | 3;
        return (byte)((tmp * (tmp ^ 1)) >>> 8);
    }

    private static int[] createCrcTable() {
        int[] buf = new int[256];

        for (int i = 0; i < 256; i++) {
            int r = i;

            for (int j = 0; j < 8; j++)
                r = (r & 1) == 1 ? (r >>> 1) ^ 0xEDB88320 : (r >>> 1);

            buf[i] = r;
        }

        return buf;
    }

    private static int[] createKeys(char[] password) {
        int[] keys = { 0x12345678, 0x23456789, 0x34567890 };

        for (int i = 0; i < password.length; i++)
            updateKeys(keys, (byte)password[i]);

        return keys;
    }

    private static void updateKeys(int[] keys, byte b) {
        keys[0] = crc32(keys[0], b);
        keys[1] = (keys[1] + (keys[0] & 0xFF)) * 0x8088405 + 1;
        keys[2] = crc32(keys[2], (byte)(keys[1] >> 24));
    }

    private static int crc32(int crc, byte b) {
        return (crc >>> 8) ^ CRC_TABLE[(crc ^ b) & 0xFF];
    }

}
