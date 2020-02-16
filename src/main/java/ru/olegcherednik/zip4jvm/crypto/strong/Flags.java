package ru.olegcherednik.zip4jvm.crypto.strong;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum Flags {
    PASSWORD_KEY(0x1),
    CERTIFICATE_KEY(0x2),
    COMBO_KEY(0x3),
    DOUBLE_SEED_KEY(0x7),
    DOUBLE_DATA_KEY(0xF),
    MASTER_KEY_3DES(0x4000);

    private final int code;

    public static Flags parseCode(int code) {
        for (Flags flags : values())
            if (flags.code == code)
                return flags;
        throw new EnumConstantNotPresentException(Flags.class, "code: " + code);
    }
}
