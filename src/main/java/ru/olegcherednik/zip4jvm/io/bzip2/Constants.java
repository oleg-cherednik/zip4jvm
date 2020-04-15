package ru.olegcherednik.zip4jvm.io.bzip2;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Constants {

    public static final int BASEBLOCKSIZE = 100000;
    public static final int MAX_ALPHA_SIZE = 258;
    public static final int MAX_CODE_LEN = 23;
    public static final int RUNA = 0;
    public static final int RUNB = 1;
    public static final int N_GROUPS = 6;
    public static final int G_SIZE = 50;
    public static final int N_ITERS = 4;
    public static final int MAX_SELECTORS = 2 + (900000 / G_SIZE);
    public static final int NUM_OVERSHOOT_BYTES = 20;

}
