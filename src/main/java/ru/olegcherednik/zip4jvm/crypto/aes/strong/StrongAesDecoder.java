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
package ru.olegcherednik.zip4jvm.crypto.aes.strong;

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 04.12.2022
 */
@RequiredArgsConstructor
public final class StrongAesDecoder implements Decoder {

    private final StrongAesCipher cipher;
    @Getter
    private final long compressedSize;

    private long decryptedBytes;

    @SuppressWarnings("NewMethodNamingConvention")
    public static StrongAesDecoder aes128(ZipEntry zipEntry, DataInput in) {
        return create(zipEntry, AesStrength.S128, in);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static StrongAesDecoder aes192(ZipEntry zipEntry, DataInput in) {
        return create(zipEntry, AesStrength.S192, in);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static StrongAesDecoder aes256(ZipEntry zipEntry, DataInput in) {
        return create(zipEntry, AesStrength.S256, in);
    }

    private static StrongAesDecoder create(ZipEntry zipEntry, AesStrength strength, DataInput in) {
        char[] password = zipEntry.getPassword();
        long compressedSize = zipEntry.getCompressedSize();
        String fileName = zipEntry.getFileName();
        return new StrongAesFactory(password, strength).createDecoder(compressedSize, fileName, in);
    }

    // ---------- Decoder ----------

    @Override
    public int getBlockSize() {
        return cipher.getBlockSize();
    }

    // ---------- Decrypt ----------

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        assert len > 0;

        if (decryptedBytes >= compressedSize)
            return 0;

        decryptedBytes += len;
        int resLen = cipher.update(buf, offs, len);
        return decryptedBytes < compressedSize ? resLen : unpad(buf, offs, resLen);
    }

    // ----------

    private static int unpad(byte[] buf, int offs, int len) {
        int n = buf[offs + len - 1];

        for (int i = offs + len - n; i < offs + len; i++)
            if (buf[i] != n)
                return len;

        return len - n;
    }

}
