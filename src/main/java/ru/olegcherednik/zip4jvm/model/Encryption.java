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
import ru.olegcherednik.zip4jvm.crypto.DecoderFactory;
import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.crypto.EncoderFactory;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.aes.WinZipAesDecoderFactory;
import ru.olegcherednik.zip4jvm.crypto.aes.WinZipAesEncoder;
import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareDecoderFactory;
import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareEncoder;
import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareEncoderFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.aes.StrongAesCipherFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.aes.StrongAesDecoderFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.cipher.StrongCipher;
import ru.olegcherednik.zip4jvm.crypto.strong.cipher.StrongCipherFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.desede.StrongTripleDesCipherFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.desede.StrongTripleDesDecoderFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.desede.StrongTripleDesEncoder;
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
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum Encryption {

    OFF(EncryptionEnum.OFF, "off"),
    PKWARE(EncryptionEnum.PKWARE, PkwareEncoderFactory.INSTANCE, PkwareDecoderFactory.INSTANCE, "PKWARE"),
    AES_128(EncryptionEnum.AES_128, WinZipAesEncoder::aes128, WinZipAesDecoderFactory.INSTANCE, "AES-128"),
    AES_192(EncryptionEnum.AES_192, WinZipAesEncoder::aes192, WinZipAesDecoderFactory.INSTANCE, "AES-192"),
    AES_256(EncryptionEnum.AES_256, WinZipAesEncoder::aes256, WinZipAesDecoderFactory.INSTANCE, "AES-256"),
    AES_STRONG_128(StrongAesDecoderFactory.INSTANCE, StrongAesCipherFactory.INSTANCE, "AES-128"),
    AES_STRONG_192(StrongAesDecoderFactory.INSTANCE, StrongAesCipherFactory.INSTANCE, "AES-192"),
    AES_STRONG_256(StrongAesDecoderFactory.INSTANCE, StrongAesCipherFactory.INSTANCE, "AES-256"),
    DES("DES"),
    RC2_PRE_52("RC2 (< 5.2)"),
    TRIPLE_DES_112("3DES-112"),
    TRIPLE_DES_168(EncryptionEnum.TRIPLE_DES_168, StrongTripleDesEncoder::tripleDes168,
                   StrongTripleDesDecoderFactory.INSTANCE, StrongTripleDesCipherFactory.INSTANCE, "3DES-168"),
    RC2("RC2"),
    RC4("RC4"),
    BLOW_FISH("BlowFish"),
    TWO_FISH("TwoFish"),
    UNKNOWN("<unknown>");

    private final EncryptionEnum enc;
    private final EncoderFactory encoderFactory;
    private final DecoderFactory decoderFactory;
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
               EncoderFactory encoderFactory,
               DecoderFactory decoderFactory,
               String title) {
        this(enc, encoderFactory, decoderFactory, null, title);
    }

    Encryption(DecoderFactory decoderFactory,
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
                       .createDecoder(zipEntry, in);
    }

    // @NotNull
    public StrongCipher createStrongCipher(char[] password, DecryptionHeader decryptionHeader, ByteOrder byteOrder) {
        return Optional.ofNullable(strongCipherFactory)
                       .orElseThrow(() -> new EncryptionNotSupportedException(this))
                       .createStrongCipher(password, decryptionHeader, byteOrder);
    }

    public boolean isAes() {
        return this == AES_128 || this == AES_192 || this == AES_256
                || this == AES_STRONG_128 || this == AES_STRONG_192 || this == AES_STRONG_256;
    }

    public boolean isStrong() {
        return this == AES_STRONG_128 || this == AES_STRONG_192 || this == AES_STRONG_256
                || this == TRIPLE_DES_112 || this == TRIPLE_DES_168;
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
