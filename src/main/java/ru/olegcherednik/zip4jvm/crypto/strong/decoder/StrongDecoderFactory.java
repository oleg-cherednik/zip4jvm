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
package ru.olegcherednik.zip4jvm.crypto.strong.decoder;

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.cipher.StrongCipher;
import ru.olegcherednik.zip4jvm.crypto.strong.cipher.StrongCipherFactory;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.crypto.strong.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 23.07.2026
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StrongDecoderFactory {

    public static final StrongDecoderFactory INSTANCE = new StrongDecoderFactory();

    private static final String DECRYPTION_HEADER = "StrongDecoderFactory.DecryptionHeader";

    public StrongDecoder createDecoder(StrongCipherFactory cipherFactory, ZipEntry zipEntry, DataInput in) {
        try {
            char[] password = zipEntry.getPassword();
            long compressedSize = zipEntry.getCompressedSize();

            in.mark(DECRYPTION_HEADER);
            StrongCipher cipher = createCipher(cipherFactory, password, in);
            long dataCompressedSize = compressedSize - (int) in.getMarkSize(DECRYPTION_HEADER);
            return new StrongDecoder(cipher, dataCompressedSize);
        } catch (IncorrectPasswordException e) {
            throw new IncorrectZipEntryPasswordException(zipEntry.getFileName());
        }
    }

    // ----------

    private static StrongCipher createCipher(StrongCipherFactory cipherFactory, char[] password, DataInput in) {
        DecryptionHeader decryptionHeader = Quietly.doRuntime(() -> new DecryptionHeaderReader().read(in));
        return cipherFactory.createCipher(password, decryptionHeader, in.getByteOrder());
    }

}
