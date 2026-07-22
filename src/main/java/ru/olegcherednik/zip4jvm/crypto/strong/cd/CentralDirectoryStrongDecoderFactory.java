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

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.StrongCipher;
import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.model.Encryption;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 22.07.2026
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CentralDirectoryStrongDecoderFactory {

    public static final CentralDirectoryStrongDecoderFactory INSTANCE = new CentralDirectoryStrongDecoderFactory();

    // @NotNull
    public Decoder createDecoder(char[] password,
                                 long compressedSize,
                                 DecryptionHeader decryptionHeader,
                                 ByteOrder byteOrder) {
        StrongCipher cipher = createStrongCipher(password, decryptionHeader, byteOrder);
        return new CentralDirectoryStrongDecoder(cipher, compressedSize);
    }

    // @NotNull
    private static StrongCipher createStrongCipher(char[] password,
                                                   DecryptionHeader decryptionHeader,
                                                   ByteOrder byteOrder) {
        Encryption encryption = decryptionHeader.getEncryptionAlgorithm().getEncryption();
        return encryption.createStrongCipher(password, decryptionHeader, byteOrder);
    }

}
