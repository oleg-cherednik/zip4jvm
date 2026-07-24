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
package ru.olegcherednik.zip4jvm.model.builders;

import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Flag;
import ru.olegcherednik.zip4jvm.crypto.strong.desede.TripleDesStrength;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.extrafield.records.StrongEncryptionHeaderExtraFieldRecord;

import lombok.RequiredArgsConstructor;

/**
 * Builds the {@code 0x0017} strong-encryption extra-field record for the central
 * directory file header.
 *
 * @author Oleg Cherednik
 * @since 24.07.2026
 */
@RequiredArgsConstructor
final class StrongEncryptionHeaderExtraFieldRecordBuilder {

    private static final int FORMAT = 2;
    // format:2 + algId:2 + bitLength:2 + flags:2 + reserved:4
    private static final int DATA_SIZE = 12;

    private final ZipEntry zipEntry;

    public StrongEncryptionHeaderExtraFieldRecord build() {
        Encryption encryption = zipEntry.getEncryption();

        if (!encryption.isStrong())
            return StrongEncryptionHeaderExtraFieldRecord.NULL;

        TripleDesStrength strength = TripleDesStrength.of(encryption);

        if (strength == TripleDesStrength.NULL)
            return StrongEncryptionHeaderExtraFieldRecord.NULL;

        return StrongEncryptionHeaderExtraFieldRecord.builder()
                                                     .dataSize(DATA_SIZE)
                                                     .format(FORMAT)
                                                     .encryptionAlgorithm(EncryptionAlgorithm.parseEncryption(encryption))
                                                     .bitLength(strength.getSize())
                                                     .flag(Flag.PASSWORD_KEY)
                                                     .unknown(new byte[4]).build();
    }

}
