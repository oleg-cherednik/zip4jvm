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
package ru.olegcherednik.zip4jvm.model;

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesDecoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesEncoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesEngine;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrongDecoder;
import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareDecoder;
import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareEncoder;
import ru.olegcherednik.zip4jvm.exception.EncryptionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AesExtraFieldRecord;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum EncryptionMethod {

    OFF(zipEntry -> Encoder.NULL, (zipEntry, in) -> Decoder.NULL, ZipEntry::getChecksum, null),
    PKWARE(PkwareEncoder::create, PkwareDecoder::create, ZipEntry::getChecksum, "pkware"),
    AES_128(AesEncoder::create, AesDecoder::create128, AesEngine::getChecksum, "aes-128"),
    AES_192(AesEncoder::create, AesDecoder::create192, AesEngine::getChecksum, "aes-192"),
    AES_256(AesEncoder::create, AesDecoder::create256, AesEngine::getChecksum, "aes-256"),
    AES_STRONG_128(null, AesStrongDecoder::create, AesEngine::getChecksum, "strong aes-128"),
    AES_STRONG_192(null, AesStrongDecoder::create, AesEngine::getChecksum, "strong aes-192"),
    AES_STRONG_256(null, AesStrongDecoder::create, AesEngine::getChecksum, "strong aes-256"),
    DES(null, null, ZipEntry::getChecksum, null),
    RC2_PRE_52(null, null, ZipEntry::getChecksum, null),
    TRIPLE_DES_168(null, null, ZipEntry::getChecksum, null),
    TRIPLE_DES_192(null, null, ZipEntry::getChecksum, null),
    RC2(null, null, ZipEntry::getChecksum, null),
    RC4(null, null, ZipEntry::getChecksum, null),
    BLOW_FISH(null, null, ZipEntry::getChecksum, null),
    TWO_FISH(null, null, ZipEntry::getChecksum, null),
    UNKNOWN(null, null, ZipEntry::getChecksum, null);

    private final Function<ZipEntry, Encoder> encoderFactory;
    private final DecoderFactory decoderFactory;
    private final Function<ZipEntry, Long> checksum;
    @Getter
    private final String title;

    public Encoder createEncoder(ZipEntry zipEntry) {
        return Optional.ofNullable(encoderFactory)
                       .orElseThrow(() -> new EncryptionNotSupportedException(this))
                       .apply(zipEntry);
    }

    public Decoder createDecoder(ZipEntry zipEntry, DataInput in) throws IOException {
        return Optional.ofNullable(decoderFactory)
                       .orElseThrow(() -> new EncryptionNotSupportedException(this))
                       .create(zipEntry, in);
    }

    public long getChecksum(ZipEntry zipEntry) {
        return checksum.apply(zipEntry);
    }

    public boolean isAes() {
        return this == AES_128 || this == AES_192 || this == AES_256;
    }

    public boolean isStrong() {
        return this == AES_STRONG_128 || this == AES_STRONG_192 || this == AES_STRONG_256;
    }

    public static EncryptionMethod get(ExtraField extraField, GeneralPurposeFlag generalPurposeFlag) {
        if (!generalPurposeFlag.isEncrypted() || !(extraField instanceof PkwareExtraField))
            return OFF;

        PkwareExtraField pkwareExtraField = (PkwareExtraField) extraField;

        if (pkwareExtraField.getAesRecord() != AesExtraFieldRecord.NULL)
            return AesEngine.getEncryption(pkwareExtraField.getAesRecord().getStrength());
        if (generalPurposeFlag.isStrongEncryption())
            return pkwareExtraField.getStrongEncryptionHeaderRecord().getEncryptionAlgorithm().getEncryptionMethod();

        return PKWARE;
    }

    private interface DecoderFactory {

        Decoder create(ZipEntry zipEntry, DataInput in) throws IOException;

    }

}
