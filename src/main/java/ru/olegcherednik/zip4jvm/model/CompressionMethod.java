package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * see 4.4.5
 *
 * @author Oleg Cherednik
 * @since 09.03.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum CompressionMethod {
    STORE(0, "none (stored)"),
    FILE_SHRUNK(1, "shrunk"),
    FILE_RED_COMP_FACTOR_1(2, "reduced (factor 1)"),
    FILE_RED_COMP_FACTOR_2(3, "reduced (factor 2)"),
    FILE_RED_COMP_FACTOR_3(4, "reduced (factor 3)"),
    FILE_RED_COMP_FACTOR_4(5, "reduced (factor 4)"),
    FILE_IMPLODED(6, "imploded"),
    DEFLATE(8, "deflated"),
    FILE_ENHANCED_DEFLATED(9, "deflated (enhanced)"),
    BZIP2(12, "bzip2 algorithm"),
    LZMA(14, "lzma encoding"),
    JPEG(96, "jpeg compression"),
    WAVPACK(97, "wavpack compression"),
    PPMD(98, "ppmd encoding"),
    AES(99, "AES encryption");

    private final int code;
    private final String title;

    public static CompressionMethod parseCode(int code) {
        for (CompressionMethod method : values())
            if (method.code == code)
                return method;
        throw new EnumConstantNotPresentException(CompressionMethod.class, "code: " + code);
    }
}
