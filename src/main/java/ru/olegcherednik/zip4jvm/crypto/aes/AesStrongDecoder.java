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
package ru.olegcherednik.zip4jvm.crypto.aes;

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.strong.AesCentralDirectoryCipherCreator;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.crypto.strong.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.crypto.Cipher;

/**
 * @author Oleg Cherednik
 * @since 04.12.2022
 */
@RequiredArgsConstructor
public final class AesStrongDecoder implements Decoder {

    private static final String DECRYPTION_HEADER = "AesStrongDecoder.DecryptionHeader";

    private final Cipher cipher;
    @Getter
    private final long compressedSize;

    private long decryptedBytes;

    public static AesStrongDecoder create(DataInput in, ZipEntry zipEntry) {
        in.mark(DECRYPTION_HEADER);
        Cipher cipher = createCipher(in, zipEntry);
        int decryptionHeaderSize = (int) in.getMarkSize(DECRYPTION_HEADER);
        long compressedSize = zipEntry.getCompressedSize() - decryptionHeaderSize;
        return new AesStrongDecoder(cipher, compressedSize);
    }

    private static Cipher createCipher(DataInput in, ZipEntry zipEntry) {
        try {
            DecryptionHeader decryptionHeader = new DecryptionHeaderReader().read(in);
            return new AesCentralDirectoryCipherCreator(zipEntry.getPassword())
                    .createCipher(in.getEndianness(), decryptionHeader);
        } catch (IncorrectPasswordException e) {
            throw new IncorrectZipEntryPasswordException(zipEntry.getFileName());
        }
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

        return Quietly.doQuietly(() -> {
            if (decryptedBytes >= compressedSize)
                return 0;

            decryptedBytes += len;
            int resLen = cipher.update(buf, offs, len, buf, offs);
            return decryptedBytes < compressedSize ? resLen : unpad(buf, offs, resLen);
        });
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
