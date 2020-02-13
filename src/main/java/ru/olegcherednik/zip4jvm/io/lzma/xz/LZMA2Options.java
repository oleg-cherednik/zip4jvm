package ru.olegcherednik.zip4jvm.io.lzma.xz;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.io.lzma.xz.exceptions.UnsupportedOptionsException;
import ru.olegcherednik.zip4jvm.io.lzma.xz.lz.LZEncoder;
import ru.olegcherednik.zip4jvm.io.lzma.xz.lzma.LzmaEncoder;

/**
 * LZMA2 compression options.
 * <p>
 * While this allows setting the LZMA2 compression options in detail,
 * often you only need <code>LZMA2Options()</code> or
 * <code>LZMA2Options(int)</code>.
 */
@Getter
public class LZMA2Options {

    /**
     * Minimum valid compression preset level is 0.
     */
    public static final int PRESET_MIN = 0;

    /**
     * Maximum valid compression preset level is 9.
     */
    public static final int PRESET_MAX = 9;

    /**
     * Default compression preset level is 6.
     */
    public static final int PRESET_DEFAULT = 6;

    /**
     * Minimum dictionary size is 4 KiB.
     */
    public static final int DICT_SIZE_MIN = 4096;

    /**
     * Maximum dictionary size for compression is 768 MiB.
     * <p>
     * The decompressor supports bigger dictionaries, up to almost 2 GiB.
     * With HC4 the encoder would support dictionaries bigger than 768 MiB.
     * The 768 MiB limit comes from the current implementation of BT4 where
     * we would otherwise hit the limits of signed ints in array indexing.
     */
    public static final int DICT_SIZE_MAX = 768 << 20;

    /**
     * The default dictionary size is 8 MiB.
     */
    public static final int DICT_SIZE_DEFAULT = 8 << 20;

    /**
     * Maximum value for lc + lp is 4.
     */
    public static final int LC_LP_MAX = 4;

    /**
     * The default number of literal context bits is 3.
     */
    public static final int LC_DEFAULT = 3;

    /**
     * The default number of literal position bits is 0.
     */
    public static final int LP_DEFAULT = 0;

    /**
     * Maximum value for pb is 4.
     */
    public static final int PB_MAX = 4;

    /**
     * The default number of position bits is 2.
     */
    public static final int PB_DEFAULT = 2;

    /**
     * Compression mode: uncompressed.
     * The data is wrapped into a LZMA2 stream without compression.
     */
    public static final int MODE_UNCOMPRESSED = 0;

    /**
     * Compression mode: fast.
     * This is usually combined with a hash chain match finder.
     */
    public static final int MODE_FAST = LzmaEncoder.MODE_FAST;

    /**
     * Compression mode: normal.
     * This is usually combined with a binary tree match finder.
     */
    public static final int MODE_NORMAL = LzmaEncoder.MODE_NORMAL;

    /**
     * Minimum value for <code>niceLen</code> is 8.
     */
    public static final int NICE_LEN_MIN = 8;

    /**
     * Maximum value for <code>niceLen</code> is 273.
     */
    public static final int NICE_LEN_MAX = 273;

    /**
     * Match finder: Hash Chain 2-3-4
     */
    public static final int MF_HC4 = LZEncoder.MF_HC4;

    /**
     * Match finder: Binary tree 2-3-4
     */
    public static final int MF_BT4 = LZEncoder.MF_BT4;

    private static final int[] presetToDictSize = {
            1 << 18, 1 << 20, 1 << 21, 1 << 22, 1 << 22,
            1 << 23, 1 << 23, 1 << 24, 1 << 25, 1 << 26 };

    private static final int[] presetToDepthLimit = { 4, 8, 24, 48 };

    private final int dictionarySize;
    private int lc;
    private int lp;
    private int pb;
    private int mode;
    private int niceLength;
    private int matchFinder;
    private int depthLimit;

    public LZMA2Options() throws UnsupportedOptionsException {
        this(PRESET_DEFAULT);
    }

    /**
     * Creates new LZMA2 options and sets them to the given preset.
     *
     * @throws UnsupportedOptionsException <code>preset</code> is not supported
     */
    public LZMA2Options(int compressionLevel) throws UnsupportedOptionsException {
        if (compressionLevel < 0 || compressionLevel > 9)
            throw new UnsupportedOptionsException("Unsupported preset: " + compressionLevel);

        lc = LC_DEFAULT;
        lp = LP_DEFAULT;
        pb = PB_DEFAULT;
        dictionarySize = presetToDictSize[compressionLevel];

        if (compressionLevel <= 3) {
            mode = MODE_FAST;
            matchFinder = MF_HC4;
            niceLength = compressionLevel <= 1 ? 128 : NICE_LEN_MAX;
            depthLimit = presetToDepthLimit[compressionLevel];
        } else {
            mode = MODE_NORMAL;
            matchFinder = MF_BT4;
            niceLength = (compressionLevel == 4) ? 16 : (compressionLevel == 5) ? 32 : 64;
            depthLimit = 0;
        }
    }

}
