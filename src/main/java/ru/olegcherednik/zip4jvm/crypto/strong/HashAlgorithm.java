package ru.olegcherednik.zip4jvm.crypto.strong;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 19.03.2020
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum HashAlgorithm {
    NONE(0x0, "none"),
    SHA1(0x8004, "SHA1"),
    UNKNOWN(0xFFFF, "<unknown>");;

    private final int code;
    private final String title;

    public static HashAlgorithm parseCode(int code) {
        for (HashAlgorithm hashAlgorithm : values())
            if (hashAlgorithm.code == code)
                return hashAlgorithm;
        return UNKNOWN;
    }
}
