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

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.tripledes.StrongTripleDesCipher;
import ru.olegcherednik.zip4jvm.crypto.strong.tripledes.TripleDesStrength;
import ru.olegcherednik.zip4jvm.exception.EncryptionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.ByteOrder;

/**
 * @author Oleg Cherednik
 * @since 22.07.2026
 */
class CentralDirectoryStrongTripleDesDecoder extends BaseCentralDirectoryStrongDecoder {

    public static CentralDirectoryStrongTripleDesDecoder create(char[] password,
                                                                long compressedSize,
                                                                DecryptionHeader decryptionHeader,
                                                                ByteOrder byteOrder) {
        StrongTripleDesCipher cipher = createStrongCipher(() -> createCipherInstance(password, decryptionHeader),
                                                          decryptionHeader, byteOrder);
        return new CentralDirectoryStrongTripleDesDecoder(cipher, compressedSize);
    }

    private static StrongTripleDesCipher createCipherInstance(char[] password, DecryptionHeader decryptionHeader) {
        EncryptionAlgorithm encryptionAlgorithm = decryptionHeader.getEncryptionAlgorithm();
        TripleDesStrength strength = TripleDesStrength.of(encryptionAlgorithm.getEncryption());

        if (strength == TripleDesStrength.NULL)
            throw new EncryptionNotSupportedException(encryptionAlgorithm);

        return StrongTripleDesCipher.getInstance(decryptionHeader, password, strength);
    }

    protected CentralDirectoryStrongTripleDesDecoder(StrongTripleDesCipher cipher, long compressedSize) {
        super(cipher, compressedSize);
    }

}
