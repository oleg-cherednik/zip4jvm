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
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.crypto.strong.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
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

    @SuppressWarnings("NewMethodNamingConvention")
    public static AesStrongDecoder create128(ZipEntry zipEntry, DataInput in) throws IOException {
        return create(zipEntry, AesStrength.S128, in);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static AesStrongDecoder create192(ZipEntry zipEntry, DataInput in) throws IOException {
        return create(zipEntry, AesStrength.S192, in);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static AesStrongDecoder create256(ZipEntry zipEntry, DataInput in) throws IOException {
        return create(zipEntry, AesStrength.S256, in);
    }

    public static AesStrongDecoder create(ZipEntry zipEntry, AesStrength strength, DataInput in) throws IOException {
        in.mark(DECRYPTION_HEADER);
        Cipher cipher = createCipher(zipEntry, strength, in);
        int decryptionHeaderSize = (int) in.getMarkSize(DECRYPTION_HEADER);
        long compressedSize = zipEntry.getCompressedSize() - decryptionHeaderSize;
        return new AesStrongDecoder(cipher, compressedSize);
    }

    private static Cipher createCipher(ZipEntry zipEntry, AesStrength strength, DataInput in) throws IOException {
        // TODO should check that decryptionHeader has same strength
        assert strength != null;

        DecryptionHeader decryptionHeader = new DecryptionHeaderReader().read(in);
        EncryptionAlgorithm encryptionAlgorithm = decryptionHeader.getEncryptionAlgorithm();

        try {
            return encryptionAlgorithm.createCipher(decryptionHeader, zipEntry.getPassword(), in.getByteOrder());
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
