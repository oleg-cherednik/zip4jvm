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

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import ru.olegcherednik.zip4jvm.crypto.aes.AesEngine;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DecryptionHeaderReader;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 09.12.2022
 */
@RequiredArgsConstructor
public final class DecryptionHeaderDecoder {

    private static final int SHA1_NUM_DIGEST_WORDS = 5;
    private static final int SHA1_DIGEST_SIZE = SHA1_NUM_DIGEST_WORDS * 4;

    private final char[] password;

    public Cipher readAndCreateCipher(DataInput in) throws Exception {
        DecryptionHeader decryptionHeader = new DecryptionHeaderReader().read(in);
        AesStrength strength = AesEngine.getStrength(decryptionHeader.getEncryptionAlgorithm().getEncryptionMethod());
        Cipher cipher = createCipher(decryptionHeader, strength);
        byte[] passwordValidationData = cipher.update(decryptionHeader.getPasswordValidationData());

        long actual = DecryptionHeader.getActualCrc32(passwordValidationData);
        long expected = DecryptionHeader.getExpectedCrc32(passwordValidationData, in.getEndianness());

        if (expected != actual)
            throw new IncorrectPasswordException();

        return cipher;
    }

    private Cipher createCipher(DecryptionHeader decryptionHeader, AesStrength strength) throws Exception {
        IvParameterSpec iv = new IvParameterSpec(decryptionHeader.getIv());
        byte[] randomData = decryptRandomData(decryptionHeader, strength, iv);
        byte[] fileKey = getFileKey(decryptionHeader, randomData);
        Key key = strength.createSecretKeyForCipher(fileKey);

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        return cipher;
    }

    private byte[] decryptRandomData(DecryptionHeader decryptionHeader,
                                     AesStrength strength,
                                     IvParameterSpec iv) throws Exception {
        byte[] masterKey = getMasterKey();
        Key key = strength.createSecretKeyForCipher(masterKey);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(decryptionHeader.getEncryptedRandomData());
    }

    private byte[] getMasterKey() {
        byte[] data = toByteArray(password);
        byte[] sha1 = DigestUtils.sha1(data);
        return deriveKey(sha1);
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static byte[] toByteArray(char[] arr) {
        byte[] res = new byte[arr.length];

        for (int i = 0; i < arr.length; i++)
            res[i] = (byte)((int)arr[i] & 0xFF);

        return res;
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static byte[] getFileKey(DecryptionHeader decryptionHeader, byte[] randomData) {
        MessageDigest md = DigestUtils.getSha1Digest();
        md.update(decryptionHeader.getIv());
        md.update(randomData);
        return deriveKey(md.digest());
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static byte[] deriveKey(byte[] digest) {
        byte[] buf = new byte[SHA1_DIGEST_SIZE * 2];
        deriveKey(digest, (byte)0x36, buf, 0);
        deriveKey(digest, (byte)0x5C, buf, SHA1_DIGEST_SIZE);
        return Arrays.copyOfRange(buf, 0, 32);
    }

    private static void deriveKey(byte[] digest, byte b, byte[] dest, int offs) {
        byte[] buf = new byte[64];
        Arrays.fill(buf, b);

        for (int i = 0; i < SHA1_DIGEST_SIZE; i++)
            buf[i] ^= digest[i];

        byte[] sha1 = DigestUtils.sha1(buf);
        System.arraycopy(sha1, 0, dest, offs, sha1.length);
    }
}
