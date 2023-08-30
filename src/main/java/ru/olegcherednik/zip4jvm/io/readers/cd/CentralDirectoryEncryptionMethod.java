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
package ru.olegcherednik.zip4jvm.io.readers.cd;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesDecoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesEncoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesEngine;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.tripledes.TripleDesDecoder;
import ru.olegcherednik.zip4jvm.exception.EncryptionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AesExtraFieldRecord;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 30.08.2023
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum CentralDirectoryEncryptionMethod {
    OFF(null, (in, password, decryptionHeaderReader, compressedSize) -> Decoder.NULL),
    AES_128(EncryptionAlgorithm.AES_128, AesCentralDirectoryDecoder::create),
    AES_192(EncryptionAlgorithm.AES_192, AesCentralDirectoryDecoder::create),
    AES_256(EncryptionAlgorithm.AES_256, AesCentralDirectoryDecoder::create),
    TRIPLE_DES_168(EncryptionAlgorithm.TRIPLE_DES_168, null),
    TRIPLE_DES_192(EncryptionAlgorithm.TRIPLE_DES_192, null),
    UNKNOWN(EncryptionAlgorithm.UNKNOWN, null);

    private final EncryptionAlgorithm encryptionAlgorithm;
    private final CreateDecoder createDecoder;

    public final Decoder createDecoder(DataInput in, char[] password, DecryptionHeaderReader decryptionHeaderReader, long compressedSize) {
        return Optional.ofNullable(createDecoder).orElseThrow(() -> new EncryptionNotSupportedException(this)).apply(in, password,
                                                                                                                     decryptionHeaderReader,
                                                                                                                     compressedSize);
    }

    public static CentralDirectoryEncryptionMethod get(EncryptionAlgorithm encryptionAlgorithm) {
        for (CentralDirectoryEncryptionMethod cdEncryptionMethod : values()) {
            if (cdEncryptionMethod == OFF && encryptionAlgorithm == null)
                return OFF;

            if (cdEncryptionMethod.encryptionAlgorithm == encryptionAlgorithm)
                return cdEncryptionMethod;
        }

        return UNKNOWN;
    }

    private interface CreateDecoder {

        Decoder apply(DataInput in, char[] password, DecryptionHeaderReader decryptionHeaderReader, long compressedSize);
    }
}
