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
package ru.olegcherednik.zip4jvm.crypto.strong.aes;

import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;
import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.crypto.strong.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 25.03.2025
 */
@RequiredArgsConstructor
public final class StrongAesFactory {

    private static final String DECRYPTION_HEADER = "StrongAesFactory.DecryptionHeader";

    private final char[] password;
    private final AesStrength strength;

    public StrongAesDecoder createDecoder(long compressedSize, String fileName, DataInput in) {
        in.mark(DECRYPTION_HEADER);
        DecryptionHeader decryptionHeader = Quietly.doRuntime(() -> new DecryptionHeaderReader().read(in));
        StrongAesCipher cipher = createCipher(decryptionHeader, fileName);

        validatePasswordChecksum(cipher, decryptionHeader, fileName, in.getByteOrder());

        long dataCompressedSize = compressedSize - (int) in.getMarkSize(DECRYPTION_HEADER);
        return new StrongAesDecoder(cipher, dataCompressedSize);
    }

    private StrongAesCipher createCipher(DecryptionHeader decryptionHeader, String fileName) {
        try {
            return StrongAesCipher.getInstance(decryptionHeader, password, strength);
        } catch (IncorrectPasswordException e) {
            throw new IncorrectZipEntryPasswordException(fileName);
        }
    }

    private static void validatePasswordChecksum(StrongAesCipher cipher,
                                                 DecryptionHeader decryptionHeader,
                                                 String fileName,
                                                 ByteOrder byteOrder) {
        byte[] passwordValidationData = cipher.update(decryptionHeader.getPasswordValidationData());
        long actual = DecryptionHeader.getActualCrc32(passwordValidationData);
        long expected = DecryptionHeader.getExpectedCrc32(passwordValidationData, byteOrder);

        if (expected != actual)
            throw new IncorrectZipEntryPasswordException(fileName);
    }

}
