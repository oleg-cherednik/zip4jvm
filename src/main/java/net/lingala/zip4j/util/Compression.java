package net.lingala.zip4j.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 02.03.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum Compression {
    STORE(0),
    //    static final int COMP_FILE_SHRUNK = 1;
//    static final int COMP_FILE_RED_COMP_FACTOR_1 = 2;
//    static final int COMP_FILE_RED_COMP_FACTOR_2 = 3;
//    static final int COMP_FILE_RED_COMP_FACTOR_3 = 4;
//    static final int COMP_FILE_RED_COMP_FACTOR_4 = 5;
//    static final int COMP_FILE_IMPLODED = 6;
    DEFLATE(8),
    //    static final int COMP_FILE_ENHANCED_DEFLATED = 9;
//    static final int COMP_PKWARE_DATA_COMP_LIB_IMPL = 10;
//    static final int COMP_BZIP2 = 12;
//    static final int COMP_LZMA = 14;
//    static final int COMP_IBM_TERSE = 18;
//    static final int COMP_IBM_LZ77 =19;
//    static final int COMP_WAVPACK = 97;
//    static final int COMP_PPMD = 98;
    AES_ENC(99);

    private final int val;

}
