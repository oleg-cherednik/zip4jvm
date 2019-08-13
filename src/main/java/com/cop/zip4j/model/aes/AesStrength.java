package com.cop.zip4j.model.aes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.crypto.spec.SecretKeySpec;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
@Getter
@SuppressWarnings("MethodCanBeVariableArityMethod")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum AesStrength {
    NONE(0, 0, 0, 0, 0),
    KEY_STRENGTH_128(1, 128, 8, 16, 16),
    KEY_STRENGTH_192(2, 192, 12, 24, 24),
    KEY_STRENGTH_256(3, 256, 16, 32, 32);

    private final int code;
    private final int size;
    private final int saltLength;
    private final int macLength;
    private final int keyLength;

    public SecretKeySpec createSecretKeyForCipher(byte[] key) {
        return new SecretKeySpec(key, 0, keyLength, "AES");
    }

    public SecretKeySpec createSecretKeyForMac(byte[] key) {
        return new SecretKeySpec(key, keyLength, macLength, "HmacSHA1");
    }

    public byte[] createPasswordChecksum(byte[] key) {
        final int offs = keyLength + macLength;
        return new byte[] { key[offs], key[offs + 1] };
    }

    public static AesStrength parseValue(int code) {
        for (AesStrength aesKeyStrength : values())
            if (aesKeyStrength.getCode() == code)
                return aesKeyStrength;

        throw new EnumConstantNotPresentException(AesStrength.class, "code=" + code);
    }

}
