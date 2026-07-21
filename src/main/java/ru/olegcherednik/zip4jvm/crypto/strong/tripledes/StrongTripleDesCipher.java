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
package ru.olegcherednik.zip4jvm.crypto.strong.tripledes;

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.model.charset.Charsets;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * PKWARE Strong Encryption (SES) cipher for the {@code 3DES} family. The key
 * derivation is identical to {@link ru.olegcherednik.zip4jvm.crypto.strong.aes.StrongAesCipher};
 * only the cipher primitive ({@code DESede}) and the key size differ.
 *
 * @author Oleg Cherednik
 * @since 21.07.2026
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StrongTripleDesCipher {

    private static final String ALGORITHM = "DESede";

    private final Cipher cipher;

    private static final int BLOCK_SIZE = 8;

    public static StrongTripleDesCipher getInstance(DecryptionHeader decryptionHeader, char[] password, int keySize) {
        return Quietly.doRuntime(() -> {
            // DESede uses an 8-byte block, but the SES DecryptionHeader stores a 16-byte IV
            IvParameterSpec iv = new IvParameterSpec(decryptionHeader.getIv(), 0, BLOCK_SIZE);
            byte[] randomData = decryptRandomData(decryptionHeader, password, keySize, iv);
            byte[] fileKey = getFileKey(decryptionHeader, randomData, keySize);
            Key key = new SecretKeySpec(fileKey, 0, keySize, ALGORITHM);

            Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            return new StrongTripleDesCipher(cipher);
        });
    }

    public int update(byte[] buf, int offs, int len) {
        return Quietly.doRuntime(() -> cipher.update(buf, offs, len, buf, offs));
    }

    public byte[] update(byte[] buf) {
        return cipher.update(buf);
    }

    public int getBlockSize() {
        return cipher.getBlockSize();
    }

    // ----------

    private static byte[] decryptRandomData(DecryptionHeader decryptionHeader,
                                            char[] password,
                                            int keySize,
                                            IvParameterSpec iv) throws Exception {
        try {
            byte[] masterKey = getMasterKey(password, keySize);
            Key key = new SecretKeySpec(masterKey, 0, keySize, ALGORITHM);

            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return cipher.doFinal(decryptionHeader.getEncryptedRandomData());
        } catch (BadPaddingException e) {
            throw new IncorrectPasswordException();
        }
    }

    private static byte[] getMasterKey(char[] password, int keySize) {
        byte[] data = toByteArray(password);
        byte[] sha1 = DigestUtils.sha1(data);
        return deriveKey(sha1, keySize);
    }

    private static byte[] toByteArray(char[] arr) {
        ByteBuffer buf = Quietly.doRuntime(() -> Charsets.UTF_8.newEncoder().encode(CharBuffer.wrap(arr)));
        byte[] res = new byte[buf.remaining()];
        buf.get(res);
        return res;
    }

    private static byte[] getFileKey(DecryptionHeader decryptionHeader, byte[] randomData, int keySize) {
        MessageDigest md = DigestUtils.getSha1Digest();
        md.update(decryptionHeader.getIv());
        md.update(randomData);
        return deriveKey(md.digest(), keySize);
    }

    private static byte[] deriveKey(byte[] digest, int keySize) {
        byte[] buf = new byte[digest.length * 2];
        deriveKey(digest, (byte) 0x36, buf, 0);
        deriveKey(digest, (byte) 0x5C, buf, digest.length);
        return Arrays.copyOfRange(buf, 0, keySize);
    }

    private static void deriveKey(byte[] digest, byte b, byte[] dst, int offs) {
        byte[] buf = new byte[64];
        Arrays.fill(buf, b);

        for (int i = 0; i < digest.length; i++)
            buf[i] ^= digest[i];

        byte[] sha1 = DigestUtils.sha1(buf);
        System.arraycopy(sha1, 0, dst, offs, sha1.length);
    }

}
