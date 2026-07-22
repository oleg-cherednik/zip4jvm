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
package ru.olegcherednik.zip4jvm.crypto.strong.tripledes;

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.StrongCipherUtils;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.crypto.strong.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 21.07.2026
 */
@RequiredArgsConstructor
public final class StrongTripleDesFactory {

    private static final String DECRYPTION_HEADER = "StrongTripleDesFactory.DecryptionHeader";

    private final char[] password;
    private final TripleDesStrength strength;

    public StrongTripleDesDecoder createDecoder(long compressedSize, DataInput in) {
        in.mark(DECRYPTION_HEADER);
        DecryptionHeader decryptionHeader = Quietly.doRuntime(() -> new DecryptionHeaderReader().read(in));
        StrongTripleDesCipher cipher = StrongCipherUtils.validatePassword(
                () -> StrongTripleDesCipher.getInstance(decryptionHeader, password, strength),
                decryptionHeader, in.getByteOrder());
        long dataCompressedSize = compressedSize - (int) in.getMarkSize(DECRYPTION_HEADER);
        return new StrongTripleDesDecoder(cipher, dataCompressedSize);
    }

}
