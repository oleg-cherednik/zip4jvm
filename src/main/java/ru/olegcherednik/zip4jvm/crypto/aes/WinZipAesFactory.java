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

import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.RequiredArgsConstructor;

import java.security.spec.KeySpec;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
@RequiredArgsConstructor
public final class WinZipAesFactory {

    public static final int MAC_SIZE = 10;
    public static final int PASSWORD_CHECKSUM_SIZE = 2;
    private static final int ITERATION_COUNT = 1000;

    private final char[] password;
    private final AesStrength strength;

    public AesEncoder createEncoder() {
        byte[] salt = strength.generateSalt();
        byte[] key = createKey(salt);

        byte[] passwordChecksum = strength.createPasswordChecksum(key);
        WinZipCipher cipher = createCipher(key);
        Mac mac = createMac(key);

        return new AesEncoder(salt, passwordChecksum, cipher, mac);
    }

    public AesDecoder createDecoder(long compressedSize, String fileName, DataInput in) {
        byte[] salt = Quietly.doRuntime(() -> in.readBytes(strength.getSaltSize()));
        byte[] key = createKey(salt);

        validatePasswordChecksum(key, fileName, in);

        WinZipCipher cipher = createCipher(key);
        Mac mac = createMac(key);
        long dataCompressedSize = getDataCompressedSize(compressedSize);

        return new AesDecoder(cipher, mac, dataCompressedSize);
    }

    private byte[] createKey(byte[] salt) {
        return Quietly.doRuntime(() -> {
            int keyLength = strength.getSize() * 2 + 16;
            KeySpec keySpec = new PBEKeySpec(password, salt, ITERATION_COUNT, keyLength);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(keySpec).getEncoded();
        });
    }

    private WinZipCipher createCipher(byte[] key) {
        return WinZipCipher.getInstance(strength.createSecretKeyForCipher(key));
    }

    private Mac createMac(byte[] key) {
        return Quietly.doRuntime(() -> {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(strength.createSecretKeyForMac(key));
            return mac;
        });
    }

    private long getDataCompressedSize(long compressedSize) {
        return compressedSize - strength.getSaltSize() - PASSWORD_CHECKSUM_SIZE - MAC_SIZE;
    }

    private void validatePasswordChecksum(byte[] key, String fileName, DataInput in) {
        byte[] actual = strength.createPasswordChecksum(key);
        byte[] expected = Quietly.doRuntime(() -> in.readBytes(PASSWORD_CHECKSUM_SIZE));

        if (!Objects.deepEquals(expected, actual))
            throw new IncorrectZipEntryPasswordException(fileName);
    }

    public static long getDataCompressedSize(long compressedSize, AesStrength strength) {
        return compressedSize - strength.getSaltSize() - PASSWORD_CHECKSUM_SIZE - MAC_SIZE;
    }

    public static long getChecksum(ZipEntry zipEntry) {
        return zipEntry.getAesVersion() == AesVersion.AE_2 ? 0 : zipEntry.getChecksum();
    }

}
