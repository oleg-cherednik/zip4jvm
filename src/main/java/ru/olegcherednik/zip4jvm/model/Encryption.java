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
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.aes.WinZipAesDecoder;
import ru.olegcherednik.zip4jvm.crypto.aes.WinZipAesEncoder;
import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareDecoder;
import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareEncoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.StrongCipher;
import ru.olegcherednik.zip4jvm.crypto.strong.StrongCipherFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.aes.StrongAesFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.cd.cipher.StrongAesCipherFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.cd.cipher.StrongTripleDesCipherFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.tripledes.StrongTripleDesFactory;
import ru.olegcherednik.zip4jvm.exception.EncryptionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.settings.EncryptionEnum;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum Encryption {

    OFF(EncryptionEnum.OFF, "off"),
    PKWARE(EncryptionEnum.PKWARE, PkwareEncoder::create, PkwareDecoder::create, "PKWARE"),
    AES_128(EncryptionEnum.AES_128, WinZipAesEncoder::aes128, WinZipAesDecoder::aes128, "AES-128"),
    AES_192(EncryptionEnum.AES_192, WinZipAesEncoder::aes192, WinZipAesDecoder::aes192, "AES-192"),
    AES_256(EncryptionEnum.AES_256, WinZipAesEncoder::aes256, WinZipAesDecoder::aes256, "AES-256"),
    AES_STRONG_128(StrongAesFactory::aes128, StrongAesCipherFactory.INSTANCE, "AES-128"),
    AES_STRONG_192(StrongAesFactory::aes192, StrongAesCipherFactory.INSTANCE, "AES-192"),
    AES_STRONG_256(StrongAesFactory::aes256, StrongAesCipherFactory.INSTANCE, "AES-256"),
    DES("DES"),
    RC2_PRE_52("RC2 (< 5.2)"),
    TRIPLE_DES_168(StrongTripleDesFactory::tripleDes168, StrongTripleDesCipherFactory.INSTANCE, "3DES-168"),
    TRIPLE_DES_192(StrongTripleDesFactory::tripleDes192, StrongTripleDesCipherFactory.INSTANCE, "3DES-192"),
    RC2("RC2"),
    RC4("RC4"),
    BLOW_FISH("BlowFish"),
    TWO_FISH("TwoFish"),
    UNKNOWN("<unknown>");

    private final EncryptionEnum enc;
    private final Function<ZipEntry, Encoder> encoderFactory;
    private final BiFunction<ZipEntry, DataInput, Decoder> decoderFactory;
    private final StrongCipherFactory strongCipherFactory;
    @Getter
    private final String title;

    Encryption(String title) {
        this(null, null, null, null, title);
    }

    Encryption(EncryptionEnum enc, String title) {
        this(enc, zipEntry -> Encoder.NULL, (zipEntry, in) -> Decoder.NULL, null, title);
    }

    Encryption(EncryptionEnum enc,
               Function<ZipEntry, Encoder> encoderFactory,
               BiFunction<ZipEntry, DataInput, Decoder> decoderFactory,
               String title) {
        this(enc, encoderFactory, decoderFactory, null, title);
    }

    Encryption(BiFunction<ZipEntry, DataInput, Decoder> decoderFactory,
               StrongCipherFactory strongCipherFactory,
               String title) {
        this(null, null, decoderFactory, strongCipherFactory, title);
    }

    // @NotNull
    public static Encryption of(EncryptionEnum enc) {
        for (Encryption encryption : values())
            if (encryption != UNKNOWN && encryption.enc == enc)
                return encryption;

        return UNKNOWN;
    }

    // @NotNull
    public static Encryption of(AesStrength strength) {
        if (strength == AesStrength.S128)
            return AES_128;
        if (strength == AesStrength.S192)
            return AES_192;
        if (strength == AesStrength.S256)
            return AES_256;
        return OFF;
    }

    // @NotNull
    public Encoder createEncoder(ZipEntry zipEntry) {
        return Optional.ofNullable(encoderFactory)
                       .orElseThrow(() -> new EncryptionNotSupportedException(this))
                       .apply(zipEntry);
    }

    // @NotNull
    public Decoder createDecoder(ZipEntry zipEntry, DataInput in) {
        return Optional.ofNullable(decoderFactory)
                       .orElseThrow(() -> new EncryptionNotSupportedException(this))
                       .apply(zipEntry, in);
    }

    // @NotNull
    public StrongCipher createStrongCipher(char[] password, DecryptionHeader decryptionHeader, ByteOrder byteOrder) {
        return Optional.ofNullable(strongCipherFactory)
                       .orElseThrow(() -> new EncryptionNotSupportedException(this))
                       .createCipher(password, decryptionHeader, byteOrder);
    }

    public boolean isAes() {
        return this == AES_128 || this == AES_192 || this == AES_256
                || this == AES_STRONG_128 || this == AES_STRONG_192 || this == AES_STRONG_256;
    }

    public boolean isStrong() {
        return this == AES_STRONG_128 || this == AES_STRONG_192 || this == AES_STRONG_256
                || this == TRIPLE_DES_168 || this == TRIPLE_DES_192;
    }

    public static Encryption get(ExtraField extraField, GeneralPurposeFlag generalPurposeFlag) {
        if (!generalPurposeFlag.isEncrypted() || !(extraField instanceof PkwareExtraField))
            return OFF;

        PkwareExtraField pkwareExtraField = (PkwareExtraField) extraField;

        if (pkwareExtraField.getAesRecord() != AesExtraFieldRecord.NULL)
            return of(pkwareExtraField.getAesRecord().getStrength());
        if (generalPurposeFlag.isStrongEncryption())
            return pkwareExtraField.getStrongEncryptionHeaderRecord().getEncryptionAlgorithm().getEncryption();

        return PKWARE;
    }

}
