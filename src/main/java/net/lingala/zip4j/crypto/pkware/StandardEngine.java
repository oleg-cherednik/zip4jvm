/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lingala.zip4j.crypto.pkware;

import lombok.NonNull;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
@SuppressWarnings({ "MethodCanBeVariableArityMethod", "NewMethodNamingConvention" })
public class StandardEngine {

    private static final int[] CRC_TABLE = new int[256];

    private final int[] keys;

    static {
        for (int i = 0, r = i; i < 256; i++, r = i) {
            for (int j = 0; j < 8; j++)
                r = (r & 1) == 1 ? (r >>> 1) ^ 0xedb88320 : (r >>> 1);

            CRC_TABLE[i] = r;
        }
    }

    public StandardEngine(@NonNull char[] password) {
        keys = createKeys(password);
    }

    private static int[] createKeys(char[] password) {
        int[] keys = { 0x12345678, 0x23456789, 0x34567890 };

        for (int i = 0; i < password.length; i++)
            updateKeys(keys, (byte)(password[i] & 0xFF));

        return keys;
    }

    private static void updateKeys(int[] keys, byte val) {
        keys[0] = crc32(keys[0], val);
        keys[1] = (keys[1] + (keys[0] & 0xFF)) * 0x8088405 + 1;
        keys[2] = crc32(keys[2], (byte)(keys[1] >> 24));
    }

    private static int crc32(int crc, byte val) {
        return (crc >>> 8) ^ CRC_TABLE[(crc ^ val) & 0xFF];
    }

    public void updateKeys(byte charAt) {
        updateKeys(keys, charAt);
    }

    public byte decryptByte() {
        int tmp = keys[2] | 2;
        return (byte)((tmp * (tmp ^ 1)) >>> 8);
    }

    private byte stream() {
        int tmp = keys[2] | 3;
        return (byte)((tmp * (tmp ^ 1)) >>> 8);
    }

    public byte encode(byte plain) {
        byte cipher = (byte)(stream() ^ plain & 0xFF);
        updateKeys(plain);
        return cipher;
    }

}
