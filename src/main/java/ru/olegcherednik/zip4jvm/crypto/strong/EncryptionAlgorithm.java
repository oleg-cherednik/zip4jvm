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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;

import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 10.10.2019
 */
@Getter
@RequiredArgsConstructor
public enum EncryptionAlgorithm {
    DES(0x6601, EncryptionMethod.DES, null, "DES"),
    RC2_PRE_52(0x6602, EncryptionMethod.RC2_PRE_52, null, "RC2 (< 5.2)"),
    TRIPLE_DES_168(0x6603, EncryptionMethod.TRIPLE_DES_168, null, "3DES-168"),
    TRIPLE_DES_192(0x6609, EncryptionMethod.TRIPLE_DES_192, null, "3DES-192"),
    AES_128(0x660E, EncryptionMethod.AES_128, EncryptionMethod.AES_STRONG_128, "AES-128"),
    AES_192(0x660F, EncryptionMethod.AES_192, EncryptionMethod.AES_STRONG_192, "AES-192"),
    AES_256(0x6610, EncryptionMethod.AES_256, EncryptionMethod.AES_STRONG_256, "AES-256"),
    RC2(0x6702, EncryptionMethod.RC2, null, "RC2"),
    RC4(0x6801, EncryptionMethod.RC4, null, "RC4"),
    BLOWFISH(0x6720, EncryptionMethod.BLOWFISH, null, "Blowfish"),
    TWOFISH(0x6721, EncryptionMethod.TWOFISH, null, "Twofish"),
    UNKNOWN(0xFFFF, EncryptionMethod.UNKNOWN, null, "<unknown>");

    private final int code;
    private final EncryptionMethod encryptionMethod;
    private final EncryptionMethod strongEncryptionMethod;
    private final String title;

    public EncryptionMethod getStrongEncryptionMethod() {
        return Optional.ofNullable(strongEncryptionMethod).orElse(encryptionMethod);
    }

    public static EncryptionAlgorithm parseCode(int code) {
        for (EncryptionAlgorithm encryptionAlgorithm : values())
            if (encryptionAlgorithm.code == code)
                return encryptionAlgorithm;
        return UNKNOWN;
    }
}
