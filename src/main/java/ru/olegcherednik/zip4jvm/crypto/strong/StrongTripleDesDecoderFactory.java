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

import ru.olegcherednik.zip4jvm.crypto.DecoderFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.cipher.StrongCipher;
import ru.olegcherednik.zip4jvm.crypto.strong.cipher.factory.StrongTripleDesCipherFactory;
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
 * @since 21.07.2026
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class StrongTripleDesDecoderFactory implements DecoderFactory {

    private static final String DECRYPTION_HEADER = "StrongTripleDesDecoderFactory.DecryptionHeader";

    public static final StrongTripleDesDecoderFactory INSTANCE = new StrongTripleDesDecoderFactory();

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

    private static StrongDecoder createDecoder(char[] password, long compressedSize, DataInput in) {
        in.mark(DECRYPTION_HEADER);
        DecryptionHeader decryptionHeader = Quietly.doRuntime(() -> new DecryptionHeaderReader().read(in));
        StrongCipher cipher =
                StrongTripleDesCipherFactory.INSTANCE.createCipher(password, decryptionHeader, in.getByteOrder());
        long dataCompressedSize = compressedSize - (int) in.getMarkSize(DECRYPTION_HEADER);
        return new StrongDecoder(cipher, dataCompressedSize);
    }

}
