package ru.olegcherednik.zip4jvm.io.lzma.xz.lz;

/** Provides a CRC32 table using the polynomial from IEEE 802.3 */
@SuppressWarnings("NewClassNamingConvention")
final class CRC32Hash {

    private static final int CRC32_POLY = 0xEDB88320;

    static final int[] crcTable = new int[256];

    static {
        for (int i = 0; i < 256; ++i) {
            int r = i;

            for (int j = 0; j < 8; ++j) {
                if ((r & 1) != 0)
                    r = (r >>> 1) ^ CRC32_POLY;
                else
                    r >>>= 1;
            }

            crcTable[i] = r;
        }
    }

    private static final int HASH_2_SIZE = 1 << 10;
    private static final int HASH_2_MASK = HASH_2_SIZE - 1;

    private static final int HASH_3_SIZE = 1 << 16;
    private static final int HASH_3_MASK = HASH_3_SIZE - 1;

    private final int hash4Mask;

    private final int[] hash2Table;
    private final int[] hash3Table;
    private final int[] hash4Table;
    private final int hash4Size;

    private int hash2Value = 0;
    private int hash3Value = 0;
    private int hash4Value = 0;

    static int getHash4Size(int dictSize) {
        int h = dictSize - 1;
        h |= h >>> 1;
        h |= h >>> 2;
        h |= h >>> 4;
        h |= h >>> 8;
        h >>>= 1;
        h |= 0xFFFF;
        if (h > (1 << 24))
            h >>>= 1;

        return h + 1;
    }

    CRC32Hash(int dictSize) {
        hash2Table = new int[HASH_2_SIZE];
        hash3Table = new int[HASH_3_SIZE];

        hash4Size = getHash4Size(dictSize);
        hash4Table = new int[hash4Size];
        hash4Mask = hash4Size - 1;
    }

    void calcHashes(byte[] buf, int off) {
        int temp = crcTable[buf[off] & 0xFF] ^ (buf[off + 1] & 0xFF);
        hash2Value = temp & HASH_2_MASK;

        temp ^= (buf[off + 2] & 0xFF) << 8;
        hash3Value = temp & HASH_3_MASK;

        temp ^= crcTable[buf[off + 3] & 0xFF] << 5;
        hash4Value = temp & hash4Mask;
    }

    int getHash2Pos() {
        return hash2Table[hash2Value];
    }

    int getHash3Pos() {
        return hash3Table[hash3Value];
    }

    int getHash4Pos() {
        return hash4Table[hash4Value];
    }

    void updateTables(int pos) {
        hash2Table[hash2Value] = pos;
        hash3Table[hash3Value] = pos;
        hash4Table[hash4Value] = pos;
    }

    void normalize(int normalizationOffset) {
        LzEncoder.normalize(hash2Table, HASH_2_SIZE, normalizationOffset);
        LzEncoder.normalize(hash3Table, HASH_3_SIZE, normalizationOffset);
        LzEncoder.normalize(hash4Table, hash4Size, normalizationOffset);
    }
}
