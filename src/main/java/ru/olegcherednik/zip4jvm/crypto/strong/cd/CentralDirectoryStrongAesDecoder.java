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
package ru.olegcherednik.zip4jvm.crypto.strong.cd;

import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.aes.StrongAesCipher;
import ru.olegcherednik.zip4jvm.exception.EncryptionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.ByteOrder;

/**
 * @author Oleg Cherednik
 * @since 21.11.2024
 */
class CentralDirectoryStrongAesDecoder extends BaseCentralDirectoryStrongDecoder {

    public static CentralDirectoryStrongAesDecoder create(char[] password,
                                                          long compressedSize,
                                                          DecryptionHeader decryptionHeader,
                                                          ByteOrder byteOrder) {
        StrongAesCipher cipher = createStrongCipher(() -> createCipherInstance(password, decryptionHeader),
                                                    decryptionHeader, byteOrder);
        return new CentralDirectoryStrongAesDecoder(cipher, compressedSize);
    }

    private static StrongAesCipher createCipherInstance(char[] password, DecryptionHeader decryptionHeader) {
        EncryptionAlgorithm encryptionAlgorithm = decryptionHeader.getEncryptionAlgorithm();
        AesStrength strength = AesStrength.of(encryptionAlgorithm.getEncryption());

        if (strength == AesStrength.NULL)
            throw new EncryptionNotSupportedException(encryptionAlgorithm);

        return StrongAesCipher.getInstance(decryptionHeader, password, strength);
    }

    protected CentralDirectoryStrongAesDecoder(StrongAesCipher cipher, long compressedSize) {
        super(cipher, compressedSize);
    }

}
