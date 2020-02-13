package ru.olegcherednik.zip4jvm.io.lzma.xz.lz;

/** Provides a CRC32 table using the polynomial from IEEE 802.3 */
@SuppressWarnings("NewClassNamingConvention")
final class CRC32Hash {

    private static final int[] CRC_TABLE = createTable();

    private final int[] hash2Table = new int[1024];
    private final int[] hash3Table = new int[65536];
    private final int[] hash4Table;

    private int hash2Value;
    private int hash3Value;
    private int hash4Value;

    public CRC32Hash(int dictionarySize) {
        hash4Table = new int[getHash4Size(dictionarySize)];
    }

    public void calcHashes(byte[] buf, int offs) {
        int tmp = CRC_TABLE[buf[offs] & 0xFF] ^ (buf[offs + 1] & 0xFF);
        hash2Value = tmp & hash2Table.length - 1;

        tmp ^= (buf[offs + 2] & 0xFF) << 8;
        hash3Value = tmp & (hash3Table.length - 1);

        tmp ^= CRC_TABLE[buf[offs + 3] & 0xFF] << 5;
        hash4Value = tmp & (hash4Table.length - 1);
    }

    public int getHash2Pos() {
        return hash2Table[hash2Value];
    }

    public int getHash3Pos() {
        return hash3Table[hash3Value];
    }

    public int getHash4Pos() {
        return hash4Table[hash4Value];
    }

    public void updateTables(int pos) {
        hash2Table[hash2Value] = pos;
        hash3Table[hash3Value] = pos;
        hash4Table[hash4Value] = pos;
    }

    public void normalize(int normalizationOffset) {
        LzEncoder.normalize(hash2Table, hash2Table.length, normalizationOffset);
        LzEncoder.normalize(hash3Table, hash3Table.length, normalizationOffset);
        LzEncoder.normalize(hash4Table, hash4Table.length, normalizationOffset);
    }

    private static int[] createTable() {
        int[] arr = new int[256];

        for (int i = 0; i < arr.length; ++i) {
            int r = i;

            for (int j = 0; j < 8; ++j) {
                if ((r & 1) != 0)
                    r = (r >>> 1) ^ 0xEDB88320;
                else
                    r >>>= 1;
            }

            arr[i] = r;
        }

        return arr;
    }

    @SuppressWarnings("NewMethodNamingConvention")
    private static int getHash4Size(int dictionarySize) {
        int h = dictionarySize - 1;
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
}
