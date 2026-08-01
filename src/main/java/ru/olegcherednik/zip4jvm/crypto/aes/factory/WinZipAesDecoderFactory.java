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
package ru.olegcherednik.zip4jvm.crypto.aes.factory;

import ru.olegcherednik.zip4jvm.crypto.DecoderFactory;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.aes.WinZipAesCipher;
import ru.olegcherednik.zip4jvm.crypto.aes.WinZipAesDecoder;
import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import javax.crypto.Mac;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotEmpty;

/**
 * @author Oleg Cherednik
 * @since 23.07.2026
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class WinZipAesDecoderFactory extends BaseWinZipAesFactory implements DecoderFactory {

    public static final WinZipAesDecoderFactory S128 = new WinZipAesDecoderFactory(AesStrength.S128);
    public static final WinZipAesDecoderFactory S192 = new WinZipAesDecoderFactory(AesStrength.S192);
    public static final WinZipAesDecoderFactory S256 = new WinZipAesDecoderFactory(AesStrength.S256);

    private final AesStrength strength;

    // ---------- DecoderFactory ----------

    @Override
    public WinZipAesDecoder createDecoder(ZipEntry zipEntry, DataInput in) {
        return Quietly.doRuntime(() -> {
            char[] password = zipEntry.getPassword();
            requireNotEmpty(password, zipEntry.getFileName() + ".password");

            long compressedSize = zipEntry.getCompressedSize();
            String fileName = zipEntry.getFileName();

            byte[] salt = Quietly.doRuntime(() -> in.readBytes(strength.getSaltSize()));
            byte[] key = createKey(salt, password, strength);

            validatePasswordChecksum(key, strength, fileName, in);

            WinZipAesCipher cipher = WinZipAesCipher.getInstance(strength.createSecretKeyForCipher(key));
            Mac mac = createMac(key, strength);
            long dataCompressedSize = getDataCompressedSize(strength, compressedSize);

            return new WinZipAesDecoder(cipher, mac, dataCompressedSize);
        });
    }

    // ---------- static ----------

    public static void validatePasswordChecksum(byte[] key, AesStrength strength, String fileName, DataInput in) {
        byte[] actual = strength.createPasswordChecksum(key);
        byte[] expected = Quietly.doRuntime(() -> in.readBytes(PASSWORD_CHECKSUM_SIZE));

        if (!Objects.deepEquals(expected, actual))
            throw new IncorrectZipEntryPasswordException(fileName);
    }

    public static long getDataCompressedSize(AesStrength strength, long compressedSize) {
        return compressedSize - strength.getSaltSize() - PASSWORD_CHECKSUM_SIZE - MAC_SIZE;
    }

}
