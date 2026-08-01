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

import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.crypto.EncoderFactory;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.aes.WinZipAesCipher;
import ru.olegcherednik.zip4jvm.crypto.aes.WinZipAesEncoder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.crypto.Mac;

/**
 * @author Oleg Cherednik
 * @since 01.08.2026
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class WinZipAesEncoderFactory extends BaseWinZipAesFactory implements EncoderFactory {

    public static final WinZipAesEncoderFactory S128 = new WinZipAesEncoderFactory(AesStrength.S128);
    public static final WinZipAesEncoderFactory S192 = new WinZipAesEncoderFactory(AesStrength.S192);
    public static final WinZipAesEncoderFactory S256 = new WinZipAesEncoderFactory(AesStrength.S256);

    private final AesStrength strength;

    // ---------- EncoderFactory ----------

    @Override
    public Encoder createEncoder(ZipEntry zipEntry) {
        char[] password = zipEntry.getPassword();
        byte[] salt = strength.generateSalt();
        byte[] key = createKey(salt, password, strength);
        byte[] passwordChecksum = strength.createPasswordChecksum(key);

        WinZipAesCipher cipher = WinZipAesCipher.getInstance(strength.createSecretKeyForCipher(key));
        Mac mac = createMac(key, strength);

        return new WinZipAesEncoder(salt, passwordChecksum, cipher, mac);
    }

}
