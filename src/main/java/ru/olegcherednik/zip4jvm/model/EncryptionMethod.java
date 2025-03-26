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
import ru.olegcherednik.zip4jvm.crypto.aes.winzip.WinZipAesDecoder;
import ru.olegcherednik.zip4jvm.crypto.aes.winzip.WinZipAesEncoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.aes.winzip.WinZipAesFactory;
import ru.olegcherednik.zip4jvm.crypto.aes.strong.StrongAesDecoder;
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

import java.util.Optional;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum EncryptionMethod {

    OFF(zipEntry -> Encoder.NULL, (zipEntry, in) -> Decoder.NULL, ZipEntry::getChecksum, "off"),
    PKWARE(PkwareEncoder::create, PkwareDecoder::create, ZipEntry::getChecksum, "pkware"),
    AES_128(WinZipAesEncoder::aes128, WinZipAesDecoder::aes128, WinZipAesFactory::getChecksum, "aes-128"),
    AES_192(WinZipAesEncoder::aes192, WinZipAesDecoder::aes192, WinZipAesFactory::getChecksum, "aes-192"),
    AES_256(WinZipAesEncoder::aes256, WinZipAesDecoder::aes256, WinZipAesFactory::getChecksum, "aes-256"),
    AES_STRONG_128(null, StrongAesDecoder::aes128, WinZipAesFactory::getChecksum, "strong aes-128"),
    AES_STRONG_192(null, StrongAesDecoder::aes192, WinZipAesFactory::getChecksum, "strong aes-192"),
    AES_STRONG_256(null, StrongAesDecoder::aes256, WinZipAesFactory::getChecksum, "strong aes-256"),
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

    public static EncryptionMethod of(AesStrength strength) {
        if (strength == AesStrength.S128)
            return AES_128;
        if (strength == AesStrength.S192)
            return AES_192;
        if (strength == AesStrength.S256)
            return AES_256;
        return OFF;
    }

    public Encoder createEncoder(ZipEntry zipEntry) {
        return Optional.ofNullable(encoderFactory)
                       .orElseThrow(() -> new EncryptionNotSupportedException(this))
                       .apply(zipEntry);
    }

    public Decoder createDecoder(ZipEntry zipEntry, DataInput in) {
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
            return of(pkwareExtraField.getAesRecord().getStrength());
        if (generalPurposeFlag.isStrongEncryption())
            return pkwareExtraField.getStrongEncryptionHeaderRecord().getEncryptionAlgorithm().getEncryptionMethod();

        return PKWARE;
    }

    private interface DecoderFactory {

        Decoder create(ZipEntry zipEntry, DataInput in);

    }

}
