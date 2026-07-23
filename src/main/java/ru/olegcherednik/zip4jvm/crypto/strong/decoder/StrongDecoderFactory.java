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

import ru.olegcherednik.zip4jvm.crypto.DecoderFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.aes.StrongAesCipherFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.cipher.StrongCipher;
import ru.olegcherednik.zip4jvm.crypto.strong.cipher.StrongCipherFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.desede.StrongTripleDesCipherFactory;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.crypto.strong.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 23.07.2026
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StrongDecoderFactory implements DecoderFactory {

    public static final StrongDecoderFactory AES =
            new StrongDecoderFactory(StrongAesCipherFactory.INSTANCE);
    public static final StrongDecoderFactory TRIPLE_DES =
            new StrongDecoderFactory(StrongTripleDesCipherFactory.INSTANCE);

    private static final String DECRYPTION_HEADER = "StrongDecoderFactory.DecryptionHeader";

    private final StrongCipherFactory cipherFactory;

    // ---------- DecoderFactory ----------

    @Override
    public StrongDecoder createDecoder(ZipEntry zipEntry, DataInput in) {
        try {
            char[] password = zipEntry.getPassword();
            long compressedSize = zipEntry.getCompressedSize();
            return createDecoder(password, compressedSize, in);
        } catch (IncorrectPasswordException e) {
            throw new IncorrectZipEntryPasswordException(zipEntry.getFileName());
        }
    }

    // ----------

    private StrongDecoder createDecoder(char[] password, long compressedSize, DataInput in) {
        in.mark(DECRYPTION_HEADER);
        DecryptionHeader decryptionHeader = Quietly.doRuntime(() -> new DecryptionHeaderReader().read(in));
        StrongCipher cipher = cipherFactory.createCipher(password, decryptionHeader, in.getByteOrder());
        long dataCompressedSize = compressedSize - (int) in.getMarkSize(DECRYPTION_HEADER);
        return new StrongDecoder(cipher, dataCompressedSize);
    }

}
