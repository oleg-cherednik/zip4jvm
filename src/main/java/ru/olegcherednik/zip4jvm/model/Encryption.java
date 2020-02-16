package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 09.03.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum Encryption {
    OFF(EncryptionMethod.OFF),
    PKWARE(EncryptionMethod.PKWARE),
    AES_128(EncryptionMethod.AES_128),
    AES_192(EncryptionMethod.AES_192),
    AES_256(EncryptionMethod.AES_256);

    private final EncryptionMethod method;

}


