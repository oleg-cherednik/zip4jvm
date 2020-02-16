package ru.olegcherednik.zip4jvm.crypto.strong;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;

/**
 * @author Oleg Cherednik
 * @since 10.10.2019
 */
@RequiredArgsConstructor
public enum EncryptionAlgorithm {
    DES(0x6601, EncryptionMethod.DES),
    RC2_PRE_52(0x6602, EncryptionMethod.RC2_PRE_52),
    TRIPLE_DES_168(0x6603, EncryptionMethod.TRIPLE_DES_168),
    TRIPLE_DES_192(0x6609, EncryptionMethod.TRIPLE_DES_192),
    AES_128(0x660E, EncryptionMethod.AES_128),
    AES_192(0x660F, EncryptionMethod.AES_192),
    AES_256(0x6610, EncryptionMethod.AES_256),
    RC2(0x6702, EncryptionMethod.RC2),
    RC4(0x6801, EncryptionMethod.RC4),
    BLOWFISH(0x6720, EncryptionMethod.BLOWFISH),
    TWOFISH(0x6721, EncryptionMethod.TWOFISH),
    UNKNOWN(0xFFFF, EncryptionMethod.UNKNOWN);

    private final int code;
    @Getter
    private final EncryptionMethod encryptionMethod;

    public static EncryptionAlgorithm parseCode(int code) {
        for (EncryptionAlgorithm encryptionAlgorithm : values())
            if (encryptionAlgorithm.code == code)
                return encryptionAlgorithm;
        throw new EnumConstantNotPresentException(EncryptionAlgorithm.class, "code: " + code);
    }
}
