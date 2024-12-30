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

import ru.olegcherednik.zip4jvm.crypto.Engine;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * @author Oleg Cherednik
 * @since 21.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class AesStrongEngine implements Engine {

    @Getter
    private final EncryptionMethod encryptionMethod;
    private final Cipher cipher;

    public int getBlockSize() {
        return cipher.getBlockSize();
    }

    // ---------- Decrypt ----------

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        return Quietly.doRuntime(() -> cipher.update(buf, offs, len, buf, offs));
    }

    // ---------- Encrypt ----------

    @Override
    public byte encrypt(byte b) {
        throw new NotImplementedException("AesEcdEngine.encrypt(byte)");
    }

    // ---------- static

    @SuppressWarnings("NewMethodNamingConvention")
    public static Cipher createCipher128(DecryptionHeader decryptionHeader, char[] password, ByteOrder byteOrder) {
        return createCipher(decryptionHeader, password, AesStrength.S128, byteOrder);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static Cipher createCipher192(DecryptionHeader decryptionHeader, char[] password, ByteOrder byteOrder) {
        return createCipher(decryptionHeader, password, AesStrength.S192, byteOrder);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static Cipher createCipher256(DecryptionHeader decryptionHeader, char[] password, ByteOrder byteOrder) {
        return createCipher(decryptionHeader, password, AesStrength.S256, byteOrder);
    }

    public static Cipher createCipher(DecryptionHeader decryptionHeader,
                                      char[] password,
                                      AesStrength strength,
                                      ByteOrder byteOrder) {
        return Quietly.doRuntime(() -> {
            IvParameterSpec iv = new IvParameterSpec(decryptionHeader.getIv());
            byte[] randomData = decryptRandomData(decryptionHeader, password, strength, iv);
            byte[] fileKey = getFileKey(decryptionHeader, randomData);
            Key key = strength.createSecretKeyForCipher(fileKey);

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            byte[] passwordValidationData = cipher.update(decryptionHeader.getPasswordValidationData());

            long actual = DecryptionHeader.getActualCrc32(passwordValidationData);
            long expected = DecryptionHeader.getExpectedCrc32(passwordValidationData, byteOrder);

            if (expected != actual)
                throw new IncorrectPasswordException();

            return cipher;
        });
    }

    private static byte[] decryptRandomData(DecryptionHeader decryptionHeader,
                                            char[] password,
                                            AesStrength strength,
                                            IvParameterSpec iv) throws Exception {
        try {
            byte[] masterKey = getMasterKey(password);
            Key key = strength.createSecretKeyForCipher(masterKey);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return cipher.doFinal(decryptionHeader.getEncryptedRandomData());
        } catch (BadPaddingException e) {
            throw new IncorrectPasswordException();
        }
    }

    private static byte[] getMasterKey(char[] password) {
        byte[] data = toByteArray(password);
        byte[] sha1 = DigestUtils.sha1(data);
        return deriveKey(sha1);
    }

    private static byte[] toByteArray(char[] arr) {
        byte[] res = new byte[arr.length];

        for (int i = 0; i < arr.length; i++)
            res[i] = (byte) (arr[i] & 0xFF);

        return res;
    }

    private static byte[] getFileKey(DecryptionHeader decryptionHeader, byte[] randomData) {
        MessageDigest md = DigestUtils.getSha1Digest();
        md.update(decryptionHeader.getIv());
        md.update(randomData);
        return deriveKey(md.digest());
    }

    private static byte[] deriveKey(byte[] digest) {
        byte[] buf = new byte[digest.length * 2];
        deriveKey(digest, (byte) 0x36, buf, 0);
        deriveKey(digest, (byte) 0x5C, buf, digest.length);
        return Arrays.copyOfRange(buf, 0, 32);
    }

    private static void deriveKey(byte[] digest, byte b, byte[] dest, int offs) {
        byte[] buf = new byte[64];
        Arrays.fill(buf, b);

        for (int i = 0; i < digest.length; i++)
            buf[i] ^= digest[i];

        byte[] sha1 = DigestUtils.sha1(buf);
        System.arraycopy(sha1, 0, dest, offs, sha1.length);
    }
}
