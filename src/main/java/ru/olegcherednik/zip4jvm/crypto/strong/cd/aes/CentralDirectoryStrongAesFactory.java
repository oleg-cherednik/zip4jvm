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
package ru.olegcherednik.zip4jvm.crypto.strong.cd.aes;

import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.aes.StrongAesCipher;
import ru.olegcherednik.zip4jvm.exception.IncorrectCentralDirectoryPasswordException;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.io.ByteOrder;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 25.03.2025
 */
@RequiredArgsConstructor
public final class CentralDirectoryStrongAesFactory {

    private final char[] password;
    private final AesStrength strength;

    public CentralDirectoryStrongAesDecoder createDecoder(long compressedSize,
                                                          DecryptionHeader decryptionHeader,
                                                          ByteOrder byteOrder) {
        StrongAesCipher cipher = createCipher(decryptionHeader);
        byte[] passwordValidationData = cipher.update(decryptionHeader.getPasswordValidationData());
        validatePasswordChecksum(passwordValidationData, byteOrder);
        return new CentralDirectoryStrongAesDecoder(cipher, compressedSize);
    }

    private StrongAesCipher createCipher(DecryptionHeader decryptionHeader) {
        try {
            return StrongAesCipher.getInstance(decryptionHeader, password, strength);
        } catch (IncorrectPasswordException e) {
            throw new IncorrectCentralDirectoryPasswordException();
        }
    }

    private static void validatePasswordChecksum(byte[] passwordValidationData, ByteOrder byteOrder) {
        long actual = DecryptionHeader.getActualCrc32(passwordValidationData);
        long expected = DecryptionHeader.getExpectedCrc32(passwordValidationData, byteOrder);

        if (expected != actual)
            throw new IncorrectCentralDirectoryPasswordException();
    }

}
