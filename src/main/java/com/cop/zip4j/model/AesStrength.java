package com.cop.zip4j.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum AesStrength {
    NONE(0, 0, 0, 0),
    KEY_STRENGTH_128(1, 8, 16, 16),
    KEY_STRENGTH_192(2, 12, 24, 24),
    KEY_STRENGTH_256(3, 16, 32, 32);

    private final int rawCode;
    private final int saltLength;
    private final int macLength;
    private final int keyLength;

    public static AesStrength parseValue(int code) {
        for (AesStrength aesKeyStrength : values()) {
            if (aesKeyStrength.getRawCode() == code) {
                return aesKeyStrength;
            }
        }

        throw new EnumConstantNotPresentException(AesStrength.class, "value=" + code);
    }

}
