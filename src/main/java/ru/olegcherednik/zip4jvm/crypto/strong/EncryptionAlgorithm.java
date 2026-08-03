/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import ru.olegcherednik.zip4jvm.model.Encryption;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The list of encryption available for strong mode.
 *
 * @author Oleg Cherednik
 * @since 10.10.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum EncryptionAlgorithm {

    DES(0x6601, Encryption.DES),
    RC2_PRE_52(0x6602, Encryption.RC2_PRE_52),
    TRIPLE_DES_168(0x6603, Encryption.TRIPLE_DES_168),
    TRIPLE_DES_112(0x6609, Encryption.TRIPLE_DES_112),
    AES_128(0x660E, Encryption.AES_STRONG_128),
    AES_192(0x660F, Encryption.AES_STRONG_192),
    AES_256(0x6610, Encryption.AES_STRONG_256),
    RC2(0x6702, Encryption.RC2),
    RC4(0x6801, Encryption.RC4),
    BLOW_FISH(0x6720, Encryption.BLOW_FISH),
    TWO_FISH(0x6721, Encryption.TWO_FISH),
    UNKNOWN(0xFFFF, Encryption.UNKNOWN);

    private final int code;
    private final Encryption encryption;

    public String getTitle() {
        return encryption.getTitle();
    }

    public static EncryptionAlgorithm parseCode(int code) {
        for (EncryptionAlgorithm encryptionAlgorithm : values())
            if (encryptionAlgorithm.code == code)
                return encryptionAlgorithm;

        return UNKNOWN;
    }

    public static EncryptionAlgorithm parseEncryption(Encryption encryption) {
        for (EncryptionAlgorithm encryptionAlgorithm : values())
            if (encryptionAlgorithm != UNKNOWN && encryptionAlgorithm.encryption == encryption)
                return encryptionAlgorithm;

        return UNKNOWN;
    }

}
