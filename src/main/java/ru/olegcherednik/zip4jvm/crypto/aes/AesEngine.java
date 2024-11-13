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
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class AesEngine implements Engine {

    public static final int MAC_SIZE = 10;
    public static final int PASSWORD_CHECKSUM_SIZE = 2;
    private static final int BLOCK_SIZE = 16;
    private static final int ITERATION_COUNT = 1000;

    private final Cipher cipher;
    private final Mac mac;
    private final byte[] iv = new byte[BLOCK_SIZE];
    private final byte[] counter = new byte[BLOCK_SIZE];

    private int nonce = BLOCK_SIZE;

    // ---------- Encrypt ----------

    @Override
    public byte encrypt(byte b) {
        return Quietly.doQuietly(() -> {
            byte bb = cipherUpdate(b);
            mac.update(bb);
            return bb;
        });
    }

    // ---------- Decrypt ----------

    @Override
    public byte decrypt(byte b) {
        return Quietly.doQuietly(() -> {
            mac.update(b);
            return cipherUpdate(b);
        });
    }

    // ----------

    /*
     * Sun implementation (com.sun.crypto.provider.CounterMode) of 'AES/ECB/NoPadding' is not compatible with WinZip
     * specification. Have to implement custom one.
     */
    private void cipherUpdate(byte[] buf, int offs, int len) throws ShortBufferException {
        for (int i = 0; i < len; i++)
            buf[offs + i] = cipherUpdate(buf[offs + i]);
    }

    private byte cipherUpdate(byte b) throws ShortBufferException {
        if (nonce == iv.length) {
            ivUpdate();
            cipher.update(iv, 0, iv.length, counter);
            nonce = 0;
        }

        return (byte) (b ^ counter[nonce++]);
    }

    private void ivUpdate() {
        for (int i = 0; i < iv.length; i++) {
            iv[i]++;

            if (iv[i] != 0)
                break;
        }
    }

    private void updateMac(byte[] buf, int offs, int len) {
        for (int i = 0; i < len; i++)
            mac.update(buf[offs + i]);
    }

    public int getBlockSize() {
        return cipher.getBlockSize();
    }

    public byte[] getMac() {
        return mac.doFinal();
    }

    public static byte[] createKey(char[] password, byte[] salt, AesStrength strength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        int keyLength = strength.getSize() * 2 + 16;
        KeySpec keySpec = new PBEKeySpec(password, salt, ITERATION_COUNT, keyLength);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(keySpec).getEncoded();
    }

    public static Cipher createCipher(SecretKeySpec secretKeySpec)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        // use custom AES implementation, so no worry for DECRYPT_MODE
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher;
    }

    public static Mac createMac(SecretKeySpec secretKeySpec) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(secretKeySpec);
        return mac;
    }

    public static AesStrength getStrength(EncryptionMethod encryptionMethod) {
        if (encryptionMethod == EncryptionMethod.AES_128 || encryptionMethod == EncryptionMethod.AES_STRONG_128)
            return AesStrength.S128;
        if (encryptionMethod == EncryptionMethod.AES_192 || encryptionMethod == EncryptionMethod.AES_STRONG_192)
            return AesStrength.S192;
        if (encryptionMethod == EncryptionMethod.AES_256 || encryptionMethod == EncryptionMethod.AES_STRONG_256)
            return AesStrength.S256;
        return AesStrength.NULL;
    }

    public static EncryptionMethod getEncryption(AesStrength strength) {
        if (strength == AesStrength.S128)
            return EncryptionMethod.AES_128;
        if (strength == AesStrength.S192)
            return EncryptionMethod.AES_192;
        if (strength == AesStrength.S256)
            return EncryptionMethod.AES_256;
        return EncryptionMethod.OFF;
    }

    public static long getDataCompressedSize(long compressedSize, AesStrength strength) {
        return compressedSize - strength.getSaltSize() - PASSWORD_CHECKSUM_SIZE - MAC_SIZE;
    }

    public static long getChecksum(ZipEntry zipEntry) {
        return zipEntry.getAesVersion() == AesVersion.AE_2 ? 0 : zipEntry.getChecksum();
    }

}
