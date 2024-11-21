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

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesCentralDirectoryDecoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrongEngine;
import ru.olegcherednik.zip4jvm.exception.EncryptionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import javax.crypto.Cipher;

/**
 * @author Oleg Cherednik
 * @since 10.10.2019
 */
@Getter
@RequiredArgsConstructor
public enum EncryptionAlgorithm {

    DES(0x6601, EncryptionMethod.DES, null, null, "DES"),
    RC2_PRE_52(0x6602, EncryptionMethod.RC2_PRE_52, null, null, "RC2 (< 5.2)"),
    TRIPLE_DES_168(0x6603, EncryptionMethod.TRIPLE_DES_168, null, null, "3DES-168"),
    TRIPLE_DES_192(0x6609, EncryptionMethod.TRIPLE_DES_192, null, null, "3DES-192"),
    AES_128(0x660E,
            EncryptionMethod.AES_STRONG_128,
            AesCentralDirectoryDecoder::create128,
            AesStrongEngine::createCipher128,
            "AES-128"),
    AES_192(0x660F,
            EncryptionMethod.AES_STRONG_192,
            AesCentralDirectoryDecoder::create192,
            AesStrongEngine::createCipher192,
            "AES-192"),
    AES_256(0x6610,
            EncryptionMethod.AES_STRONG_256,
            AesCentralDirectoryDecoder::create256,
            AesStrongEngine::createCipher256,
            "AES-256"),
    RC2(0x6702, EncryptionMethod.RC2, null, null, "RC2"),
    RC4(0x6801, EncryptionMethod.RC4, null, null, "RC4"),
    BLOW_FISH(0x6720, EncryptionMethod.BLOW_FISH, null, null, "BlowFish"),
    TWO_FISH(0x6721, EncryptionMethod.TWO_FISH, null, null, "TwoFish"),
    UNKNOWN(0xFFFF, EncryptionMethod.UNKNOWN, null, null, "<unknown>");

    private final int code;
    private final EncryptionMethod encryptionMethod;
    @Getter(AccessLevel.NONE)
    private final DecoderFactory ecdDecoderFactory;
    @Getter(AccessLevel.NONE)
    private final CipherFactory cipherFactory;
    private final String title;

    public Decoder createEcdDecoder(DecryptionHeader decryptionHeader,
                                    char[] password,
                                    long compressedSize,
                                    ByteOrder byteOrder) {
        return Optional.ofNullable(ecdDecoderFactory)
                       .orElseThrow(() -> new EncryptionNotSupportedException(this))
                       .create(decryptionHeader, password, compressedSize, byteOrder);
    }

    public Cipher createCipher(DecryptionHeader decryptionHeader, char[] password, ByteOrder byteOrder) {
        return Optional.ofNullable(cipherFactory)
                       .orElseThrow(() -> new EncryptionNotSupportedException(this))
                       .create(decryptionHeader, password, byteOrder);
    }

    public static EncryptionAlgorithm parseCode(int code) {
        for (EncryptionAlgorithm encryptionAlgorithm : values())
            if (encryptionAlgorithm.code == code)
                return encryptionAlgorithm;

        return UNKNOWN;
    }

    private interface DecoderFactory {

        Decoder create(DecryptionHeader decryptionHeader, char[] password, long compressedSize, ByteOrder byteOrder);

    }

    private interface CipherFactory {

        Cipher create(DecryptionHeader decryptionHeader, char[] password, ByteOrder byteOrder);

    }

}
