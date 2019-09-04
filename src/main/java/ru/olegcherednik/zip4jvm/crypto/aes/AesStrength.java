package ru.olegcherednik.zip4jvm.crypto.aes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
@Getter
@SuppressWarnings("MethodCanBeVariableArityMethod")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum AesStrength {
    NULL(0, 0),
    S128(1, 128),
    S192(2, 192),
    S256(3, 256);

    private final int code;
    private final int size;

    public final int saltLength() {
        return size / 16;
    }

    private int macLength() {
        return size / 8;
    }

    private int keyLength() {
        return size / 8;
    }

    public SecretKeySpec createSecretKeyForCipher(byte[] key) {
        return new SecretKeySpec(key, 0, keyLength(), "AES");
    }

    public SecretKeySpec createSecretKeyForMac(byte[] key) {
        return new SecretKeySpec(key, keyLength(), macLength(), "HmacSHA1");
    }

    public byte[] createPasswordChecksum(byte[] key) {
        final int offs = keyLength() + macLength();
        return new byte[] { key[offs], key[offs + 1] };
    }

    public byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] buf = new byte[saltLength()];
        random.nextBytes(buf);
        return buf;
    }

    public static AesStrength parseValue(int code) {
        for (AesStrength aesKeyStrength : values())
            if (aesKeyStrength.getCode() == code)
                return aesKeyStrength;

        throw new EnumConstantNotPresentException(AesStrength.class, "code=" + code);
    }

}
