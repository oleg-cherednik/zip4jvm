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
package ru.olegcherednik.zip4jvm.crypto.strong;

import ru.olegcherednik.zip4jvm.crypto.SecretKeySpecFactory;
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

/**
 * @author Oleg Cherednik
 * @since 21.07.2026
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StrongCipher {

    protected final Cipher cipher;

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

    protected static byte[] decryptRandomData(DecryptionHeader decryptionHeader,
                                              char[] password,
                                              SecretKeySpecFactory secretKeySpecFactory,
                                              String cipherTransformation,
                                              IvParameterSpec iv) throws Exception {
        try {
            byte[] masterKey = getMasterKey(password, secretKeySpecFactory.getKeySize());
            Key key = secretKeySpecFactory.createSecretKeyForCipher(masterKey);

            Cipher cipher = Cipher.getInstance(cipherTransformation);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return cipher.doFinal(decryptionHeader.getEncryptedRandomData());
        } catch (BadPaddingException e) {
            throw new IncorrectPasswordException();
        }
    }

    protected static byte[] getMasterKey(char[] password, int keySize) {
        byte[] data = toByteArray(password);
        byte[] sha1 = DigestUtils.sha1(data);
        return deriveKey(sha1, keySize);
    }

    protected static byte[] toByteArray(char[] arr) {
        ByteBuffer buf = Quietly.doRuntime(() -> Charsets.UTF_8.newEncoder().encode(CharBuffer.wrap(arr)));
        byte[] res = new byte[buf.remaining()];
        buf.get(res);
        return res;
    }

    protected static byte[] getFileKey(DecryptionHeader decryptionHeader, byte[] randomData, int keySize) {
        MessageDigest md = DigestUtils.getSha1Digest();
        md.update(decryptionHeader.getIv());
        md.update(randomData);
        return deriveKey(md.digest(), keySize);
    }

    protected static byte[] deriveKey(byte[] digest, int keySize) {
        byte[] buf = new byte[digest.length * 2];
        deriveKey(digest, (byte) 0x36, buf, 0);
        deriveKey(digest, (byte) 0x5C, buf, digest.length);
        return Arrays.copyOfRange(buf, 0, keySize);
    }

    protected static void deriveKey(byte[] digest, byte b, byte[] dst, int offs) {
        byte[] buf = new byte[64];
        Arrays.fill(buf, b);

        for (int i = 0; i < digest.length; i++)
            buf[i] ^= digest[i];

        byte[] sha1 = DigestUtils.sha1(buf);
        System.arraycopy(sha1, 0, dst, offs, sha1.length);
    }

}
