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
public enum CompressionMethod {
    STORE(0),
    //    FILE_SHRUNK(1),
//    FILE_RED_COMP_FACTOR_1(2),
//    FILE_RED_COMP_FACTOR_2(3),
//    FILE_RED_COMP_FACTOR_3(4),
//    FILE_RED_COMP_FACTOR_4(5),
//    FILE_IMPLODED(6),
    DEFLATE(8),
    //    FILE_ENHANCED_DEFLATED(9),
//    PKWARE_DATA_COMP_LIB_IMPL(10),
//    BZIP2(12),
//    LZMA(14),
//    IBM_TERSE(18),
//    IBM_LZ77(19),
//    WAVPACK(97),
//    PPMD(98),
    AES(99);

    private final int code;

    public static CompressionMethod parseCode(int code) {
        for (CompressionMethod method : values())
            if (method.code == code)
                return method;
        throw new EnumConstantNotPresentException(CompressionMethod.class, "code: " + code);
    }
}
