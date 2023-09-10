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
package ru.olegcherednik.zip4jvm.io.readers.cd;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.strong.AesDecryptionHeaderDecoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.exception.EncryptionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.Endianness;

import javax.crypto.Cipher;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 30.08.2023
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum CentralDirectoryEncryptionMethod {
    AES_128(EncryptionAlgorithm.AES_128, AesDecryptionHeaderDecoder::createCipher, AesCentralDirectoryDecoder::create),
    AES_192(EncryptionAlgorithm.AES_192, AesDecryptionHeaderDecoder::createCipher, AesCentralDirectoryDecoder::create),
    AES_256(EncryptionAlgorithm.AES_256, AesDecryptionHeaderDecoder::createCipher, AesCentralDirectoryDecoder::create),
    TRIPLE_DES_168(EncryptionAlgorithm.TRIPLE_DES_168, null, null),
    TRIPLE_DES_192(EncryptionAlgorithm.TRIPLE_DES_192, null, null),
    UNKNOWN(EncryptionAlgorithm.UNKNOWN, null, null);

    private final EncryptionAlgorithm encryptionAlgorithm;
    private final CreateCipher createCipher;
    private final CreateCipher1 createCipher1;

    public final Cipher createCipher(char[] password, Endianness endianness, DecryptionHeader decryptionHeader) {
        return Optional.ofNullable(createCipher).orElseThrow(() -> new EncryptionNotSupportedException(this))
                       .apply(password, endianness, decryptionHeader);
    }

    public final CentralDirectoryDecoder createDecoder(char[] password, Endianness endianness, DecryptionHeader decryptionHeader) {
        return Optional.ofNullable(createCipher1).orElseThrow(() -> new EncryptionNotSupportedException(this))
                       .apply(password, endianness, decryptionHeader);
    }

    public static CentralDirectoryEncryptionMethod get(EncryptionAlgorithm encryptionAlgorithm) {
        for (CentralDirectoryEncryptionMethod cdEncryptionMethod : values())
            if (cdEncryptionMethod.encryptionAlgorithm == encryptionAlgorithm)
                return cdEncryptionMethod;

        return UNKNOWN;
    }

    private interface CreateCipher {

        Cipher apply(char[] password, Endianness endianness, DecryptionHeader decryptionHeader);
    }

    private interface CreateCipher1 {

        CentralDirectoryDecoder apply(char[] password, Endianness endianness, DecryptionHeader decryptionHeader);
    }
}
