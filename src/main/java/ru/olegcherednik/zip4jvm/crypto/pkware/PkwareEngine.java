/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.crypto.pkware;

import ru.olegcherednik.zip4jvm.crypto.Engine;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
@SuppressWarnings("NewMethodNamingConvention")
public final class PkwareEngine implements Engine {

    private static final int[] CRC_TABLE = createCrcTable();

    private final int[] keys;

    public PkwareEngine(char[] password) {
        keys = createKeys(password);
    }

    // ---------- Encrypt ----------

    @Override
    public void encrypt(byte[] buf, int offs, int len) {
        for (int i = offs; i < offs + len; i++)
            buf[i] = encrypt(buf[i]);
    }

    // ---------- Decrypt ----------

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        assert len > 0;

        for (int i = offs; i < offs + len; i++) {
            buf[i] ^= decrypt();
            updateKeys(keys, buf[i]);
        }

        return len;
    }

    // ----------

    private byte encrypt(byte b) {
        byte cipher = (byte) (stream() ^ b);
        updateKeys(keys, b);
        return cipher;
    }

    private byte decrypt() {
        int tmp = keys[2] | 2;
        return (byte) ((tmp * (tmp ^ 1)) >>> 8);
    }

    private byte stream() {
        int tmp = keys[2] | 3;
        return (byte) ((tmp * (tmp ^ 1)) >>> 8);
    }

    private static int[] createCrcTable() {
        int[] buf = new int[256];

        for (int i = 0; i < buf.length; i++) {
            int r = i;

            for (int j = 0; j < 8; j++)
                r = (r & 1) == 1 ? (r >>> 1) ^ 0xEDB88320 : (r >>> 1);

            buf[i] = r;
        }

        return buf;
    }

    /** see 6.1.5 */
    private static int[] createKeys(char[] password) {
        int[] keys = { 0x12345678, 0x23456789, 0x34567890 };

        for (char ch : password)
            updateKeys(keys, (byte) ch);

        return keys;
    }

    private static void updateKeys(int[] keys, byte b) {
        keys[0] = crc32(keys[0], b);
        keys[1] = (keys[1] + (keys[0] & 0xFF)) * 0x8088405 + 1;
        keys[2] = crc32(keys[2], (byte) (keys[1] >> 24));
    }

    private static int crc32(int crc, byte b) {
        return (crc >>> 8) ^ CRC_TABLE[(crc ^ b) & 0xFF];
    }

}
