package ru.olegcherednik.zip4jvm.crypto.strong;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum Flags {
    PASSWORD_KEY(0x1, "password"),
    CERTIFICATE_KEY(0x2, "certificate"),
    COMBO_KEY(0x3, "password or certificate"),
    DOUBLE_SEED_KEY(0x7, "double seed"),
    DOUBLE_DATA_KEY(0xF, "double data"),
    NON_OAEP(0x100, "non-OAEP"),
    MASTER_KEY_3DES(0x4000, "master 3DES");

    private final int code;
    private final String title;

    public static Flags parseCode(int code) {
        for (Flags flags : values())
            if (flags.code == code)
                return flags;
        throw new EnumConstantNotPresentException(Flags.class, "code: " + code);
    }
}
