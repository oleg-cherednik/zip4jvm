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
import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.security.spec.KeySpec;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
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
    private static final int ITERATION_COUNT = 1000;

    private final WinZipCipher cipher;
    private final Mac mac;

    // ---------- Encrypt ----------

    @Override
    public byte encrypt(byte b) {
        return Quietly.doRuntime(() -> {
            byte bb = cipher.update(b);
            mac.update(bb);
            return bb;
        });
    }

    // ---------- Decrypt ----------

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        assert len > 0;

        Quietly.doRuntime(() -> {
            mac.update(buf, offs, len);
            cipher.update(buf, offs, len);
        });

        return len;
    }

    // ----------

    public int getBlockSize() {
        return cipher.getBlockSize();
    }

    public byte[] getMac() {
        return mac.doFinal();
    }

    public static byte[] createKey(char[] password, byte[] salt, AesStrength strength) {
        return Quietly.doRuntime(() -> {
            int keyLength = strength.getSize() * 2 + 16;
            KeySpec keySpec = new PBEKeySpec(password, salt, ITERATION_COUNT, keyLength);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(keySpec).getEncoded();
        });
    }

    public static Mac createMac(SecretKeySpec secretKeySpec) {
        return Quietly.doRuntime(() -> {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKeySpec);
            return mac;
        });
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
