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
import ru.olegcherednik.zip4jvm.crypto.aes.strong.cd.CentralDirectoryStrongAesDecoder;
import ru.olegcherednik.zip4jvm.exception.EncryptionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 10.10.2019
 */
@Getter
@RequiredArgsConstructor
public enum EncryptionAlgorithm {

    DES(0x6601, EncryptionMethod.DES, null),
    RC2_PRE_52(0x6602, EncryptionMethod.RC2_PRE_52, null),
    TRIPLE_DES_168(0x6603, EncryptionMethod.TRIPLE_DES_168, null),
    TRIPLE_DES_192(0x6609, EncryptionMethod.TRIPLE_DES_192, null),
    AES_128(0x660E, EncryptionMethod.AES_STRONG_128, CentralDirectoryStrongAesDecoder::aes128),
    AES_192(0x660F, EncryptionMethod.AES_STRONG_192, CentralDirectoryStrongAesDecoder::aes192),
    AES_256(0x6610, EncryptionMethod.AES_STRONG_256, CentralDirectoryStrongAesDecoder::aes256),
    RC2(0x6702, EncryptionMethod.RC2, null),
    RC4(0x6801, EncryptionMethod.RC4, null),
    BLOW_FISH(0x6720, EncryptionMethod.BLOW_FISH, null),
    TWO_FISH(0x6721, EncryptionMethod.TWO_FISH, null),
    UNKNOWN(0xFFFF, EncryptionMethod.UNKNOWN, null);

    private final int code;
    private final EncryptionMethod encryptionMethod;
    @Getter(AccessLevel.NONE)
    private final DecoderFactory centralDirectoryDecoderFactory;

    public Decoder createCentralDirectoryDecoder(char[] password,
                                                 long compressedSize,
                                                 DecryptionHeader decryptionHeader,
                                                 ByteOrder byteOrder) {
        return Optional.ofNullable(centralDirectoryDecoderFactory)
                       .orElseThrow(() -> new EncryptionNotSupportedException(this))
                       .create(password, compressedSize, decryptionHeader, byteOrder);
    }

    public String getTitle() {
        return encryptionMethod.getTitle();
    }

    public static EncryptionAlgorithm parseCode(int code) {
        for (EncryptionAlgorithm encryptionAlgorithm : values())
            if (encryptionAlgorithm.code == code)
                return encryptionAlgorithm;

        return UNKNOWN;
    }

    private interface DecoderFactory {

        Decoder create(char[] password, long compressedSize, DecryptionHeader decryptionHeader, ByteOrder byteOrder);

    }

}
