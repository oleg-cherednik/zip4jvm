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

import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.spec.KeySpec;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseWinZipAesFactory {

    public static final int MAC_SIZE = 10;
    public static final int PASSWORD_CHECKSUM_SIZE = 2;

    private static final int ITERATION_COUNT = 1000;

    public byte[] createKey(byte[] salt, char[] password, AesStrength strength) {
        return Quietly.doRuntime(() -> {
            int keyLength = strength.getSize() * 2 + 16;
            KeySpec keySpec = new PBEKeySpec(password, salt, ITERATION_COUNT, keyLength);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(keySpec).getEncoded();
        });
    }

    public static Mac createMac(byte[] key, AesStrength strength) {
        return Quietly.doRuntime(() -> {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(strength.createSecretKeyForMac(key));
            return mac;
        });
    }

}
