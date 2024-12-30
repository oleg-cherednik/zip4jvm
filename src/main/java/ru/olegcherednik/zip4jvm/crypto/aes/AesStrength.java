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

import ru.olegcherednik.zip4jvm.model.EncryptionMethod;

import lombok.Getter;

import java.security.SecureRandom;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
@Getter
public enum AesStrength {

    NULL(0, 0),
    S128(1, 128),
    S192(2, 192),
    S256(3, 256);

    private final int code;
    private final int size;
    private final int saltSize;
    private final int macSize;
    private final int keySize;

    AesStrength(int code, int size) {
        this.code = code;
        this.size = size;
        saltSize = size / 16;
        macSize = size / 8;
        keySize = size / 8;
    }

    public SecretKeySpec createSecretKeyForCipher(byte[] key) {
        return new SecretKeySpec(key, 0, keySize, "AES");
    }

    public SecretKeySpec createSecretKeyForMac(byte[] key) {
        return new SecretKeySpec(key, keySize, macSize, "HmacSHA1");
    }

    public byte[] createPasswordChecksum(byte[] key) {
        final int offs = keySize + macSize;
        return new byte[] { key[offs], key[offs + 1] };
    }

    public byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] buf = new byte[saltSize];
        random.nextBytes(buf);
        return buf;
    }

    public static AesStrength of(int code) {
        for (AesStrength aesKeyStrength : values())
            if (aesKeyStrength.getCode() == code)
                return aesKeyStrength;

        throw new EnumConstantNotPresentException(AesStrength.class, "code=" + code);
    }

    public static AesStrength of(EncryptionMethod encryptionMethod) {
        switch (encryptionMethod) {
            case AES_128:
            case AES_STRONG_128:
                return S128;
            case AES_192:
            case AES_STRONG_192:
                return S192;
            case AES_256:
            case AES_STRONG_256:
                return S256;
            default:
                return NULL;
        }
    }

}
