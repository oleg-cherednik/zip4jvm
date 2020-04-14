package ru.olegcherednik.zip4jvm.io.bzip2;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
public class Bzip2InputStream extends InputStream {

    private final BitInputStream bin;
    private final int blockSize100k;

    /**
     * Index of the last char in the block, so the block size == last + 1.
     */
    private int last;

    /**
     * Index in zptr[] of original string after sorting.
     */
    private int origPtr;


    private int blockCrc;
    private boolean blockRandomised;

    private final CRC32 crc32 = new CRC32();

    private int nInUse;

    private State currentState = State.START_BLOCK_STATE;

    private int storedCombinedCRC;
    private int computedBlockCRC, computedCombinedCRC;

    // Variables used by setup* methods exclusively

    private int su_count;
    private int su_ch2;
    private int su_chPrev;
    private int su_i2;
    private int su_j2;
    private int su_rNToGo;
    private int su_rTPos;
    private int su_tPos;
    private char su_z;

    private Bzip2InputStream.Data data;

    public Bzip2InputStream(DataInput in, int blockSize100k) throws IOException {
        bin = new BitInputStream(in);
        this.blockSize100k = blockSize100k;
        initBlock();
    }

    @Override
    public int read() throws IOException {
        return currentState.read(this);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        if (len == 0)
            return 0;

        final int hi = offs + len;
        int destOffs = offs;
        int b;

        while (destOffs < hi && ((b = currentState.read(this)) >= 0)) {
            buf[destOffs++] = (byte)b;
        }

        return destOffs == offs ? -1 : destOffs - offs;
    }

    private static final long MAGIC_COMPRESSED = 0x314159265359L;
    private static final long MAGIC_EOS = 0x177245385090L;

    private void initBlock() throws IOException {
        long magic = bin.readBits(Byte.SIZE * 6);

        if (magic == MAGIC_EOS) {
            complete();
            return;
        }

        if (magic != MAGIC_COMPRESSED) {
            currentState = State.EOF;
            throw new IOException("Bad block header");
        }

        blockCrc = (int)bin.readBits(Byte.SIZE * 4);
        blockRandomised = bin.readBit();

        if (data == null)
            data = new Bzip2InputStream.Data(blockSize100k);

        // currBlockNo++;
        getAndMoveToFrontDecode();

        crc32.init();
        currentState = State.START_BLOCK_STATE;
    }

    private boolean complete() throws IOException {
        storedCombinedCRC = (int)bin.readBits(Byte.SIZE * 4);
        currentState = State.EOF;
        data = null;

        if (storedCombinedCRC != computedCombinedCRC)
            throw new Zip4jvmException("BZIP2 CRC incorrect");

        return true;
    }

    private void endBlock() throws IOException {
        this.computedBlockCRC = this.crc32.getFinalCRC();

        // A bad CRC is considered a fatal error.
        if (this.blockCrc != this.computedBlockCRC) {
            // make next blocks readable without error
            // (repair feature, not yet documented, not tested)
            this.computedCombinedCRC = (this.storedCombinedCRC << 1)
                    | (this.storedCombinedCRC >>> 31);
            this.computedCombinedCRC ^= this.blockCrc;

            throw new IOException("BZip2 CRC error");
        }

        this.computedCombinedCRC = (this.computedCombinedCRC << 1)
                | (this.computedCombinedCRC >>> 31);
        this.computedCombinedCRC ^= this.computedBlockCRC;
    }

    private static void checkBounds(int val, int limitExclusive, String name)
            throws IOException {
        if (val < 0)
            throw new IOException("Corrupted input, " + name + " value negative");
        if (val >= limitExclusive)
            throw new IOException("Corrupted input, " + name + " value too big");
    }

    private void recvDecodingTables() throws IOException {
        nInUse = data.readHuffmanUsedBitmaps(bin);
        int alphaSize = nInUse + 2;

        /* Now the selectorsUsed */
        int huffmanGroups = (int)bin.readBits(3);
        int selectorsUsed = (int)bin.readBits(15);

        if (selectorsUsed < 0)
            throw new IOException("Corrupted input, nSelectors value negative");

        checkBounds(alphaSize, Constants.MAX_ALPHA_SIZE + 1, "alphaSize");
        checkBounds(huffmanGroups, Constants.N_GROUPS + 1, "huffmanGroups");

        // Don't fail on nSelectors overflowing boundaries but discard the values in overflow
        // See https://gnu.wildebeest.org/blog/mjw/2019/08/02/bzip2-and-the-cve-that-wasnt/
        // and https://sourceware.org/ml/bzip2-devel/2019-q3/msg00007.html

        for (int i = 0, j = 0; i < selectorsUsed; i++, j = 0) {
            while (bin.readBit())
                j++;

            if (i < Constants.MAX_SELECTORS)
                data.selectorList[i] = (byte)j;
        }

        /* Undo the MTF values for the selectorsUsed. */
        for (int i = huffmanGroups - 1; i >= 0; i--)
            data.recvDecodingTables_pos[i] = (byte)i;

        for (int i = 0, max = Math.min(selectorsUsed, Constants.MAX_SELECTORS); i < max; i++) {
            int j = data.selectorList[i] & 0xFF;
            checkBounds(j, Constants.N_GROUPS, "selectorMtf");

            byte tmp = data.recvDecodingTables_pos[j];

            while (j > 0) {
                // nearly all times j is zero, 4 in most other cases
                data.recvDecodingTables_pos[j] = data.recvDecodingTables_pos[j - 1];
                j--;
            }

            data.recvDecodingTables_pos[0] = tmp;
            data.selector[i] = tmp;
        }

        data.codingTables(bin, huffmanGroups, alphaSize);
        createHuffmanDecodingTables(huffmanGroups, alphaSize);
    }

    /**
     * Called by recvDecodingTables() exclusively.
     */
    private void createHuffmanDecodingTables(int huffmanGroups, int alphaSize) throws IOException {
        for (int i = 0, min = 32, max = 0; i < huffmanGroups; i++) {
            for (int j = alphaSize - 1; j >= 0; j--) {
                min = Math.min(min, data.temp_charArray2d[i][j]);
                max = Math.max(max, data.temp_charArray2d[i][j]);
            }

            hbCreateDecodeTables(i, min, max, alphaSize);
            data.minLens[i] = min;
        }
    }

    /**
     * Called by createHuffmanDecodingTables() exclusively.
     */
    private void hbCreateDecodeTables(int group, int min, int max, int alphaSize) throws IOException {
        for (int i = min, k = 0; i <= max; i++)
            for (int j = 0; j < alphaSize; j++)
                if (data.temp_charArray2d[group][j] == i)
                    data.perm[group][k++] = j;

        for (int i = Constants.MAX_CODE_LEN - 1; i > 0; i--) {
            data.base[group][i] = 0;
            data.limit[group][i] = 0;
        }

        for (int i = 0; i < alphaSize; i++) {
            int l = data.temp_charArray2d[group][i];
            checkBounds(l, Constants.MAX_ALPHA_SIZE, "length");
            data.base[group][l + 1]++;
        }

        for (int i = 1, b = data.base[group][0]; i < Constants.MAX_CODE_LEN; i++) {
            b += data.base[group][i];
            data.base[group][i] = b;
        }

        for (int i = min, vec = 0, b = data.base[group][i]; i <= max; i++) {
            int nb = data.base[group][i + 1];
            vec += nb - b;
            b = nb;
            data.limit[group][i] = vec - 1;
            vec <<= 1;
        }

        for (int i = min + 1; i <= max; i++)
            data.base[group][i] = ((data.limit[group][i - 1] + 1) << 1) - data.base[group][i];
    }

    private void getAndMoveToFrontDecode() throws IOException {
        origPtr = (int)bin.readBits(Byte.SIZE * 3);
        recvDecodingTables();

        final int limitLast = blockSize100k * 100000;

        /*
         * Setting up the unzftab entries here is not strictly necessary, but it
         * does save having to do it later in a separate pass, and so saves a
         * block's worth of cache misses.
         */
        for (int i = 256; --i >= 0; ) {
            data.getAndMoveToFrontDecode_yy[i] = (char)i;
            data.unzftab[i] = 0;
        }

        int groupNo = 0;
        int groupPos = Constants.G_SIZE - 1;
        final int eob = nInUse + 1;
        int nextSym = getAndMoveToFrontDecode0();
        int lastShadow = -1;
        int zt = data.selector[groupNo] & 0xff;
        checkBounds(zt, Constants.N_GROUPS, "zt");
        int[] base_zt = data.base[zt];
        int[] limit_zt = data.limit[zt];
        int[] perm_zt = data.perm[zt];
        int minLens_zt = data.minLens[zt];

        while (nextSym != eob) {
            if ((nextSym == Constants.RUNA) || (nextSym == Constants.RUNB)) {
                int s = -1;

                for (int n = 1; true; n <<= 1) {
                    if (nextSym == Constants.RUNA) {
                        s += n;
                    } else if (nextSym == Constants.RUNB) {
                        s += n << 1;
                    } else {
                        break;
                    }

                    if (groupPos == 0) {
                        groupPos = Constants.G_SIZE - 1;
                        checkBounds(++groupNo, Constants.MAX_SELECTORS, "groupNo");
                        zt = data.selector[groupNo] & 0xff;
                        checkBounds(zt, Constants.N_GROUPS, "zt");
                        base_zt = data.base[zt];
                        limit_zt = data.limit[zt];
                        perm_zt = data.perm[zt];
                        minLens_zt = data.minLens[zt];
                    } else {
                        groupPos--;
                    }

                    int zn = minLens_zt;
                    checkBounds(zn, Constants.MAX_ALPHA_SIZE, "zn");
                    int zvec = (int)bin.readBits(zn);
                    while (zvec > limit_zt[zn]) {
                        checkBounds(++zn, Constants.MAX_ALPHA_SIZE, "zn");
                        zvec = (zvec << 1) | (int)bin.readBits(1);
                    }
                    final int tmp = zvec - base_zt[zn];
                    checkBounds(tmp, Constants.MAX_ALPHA_SIZE, "zvec");
                    nextSym = perm_zt[tmp];
                }

                final int yy0 = data.getAndMoveToFrontDecode_yy[0];
                checkBounds(yy0, 256, "yy");
                final byte ch = data.seqToUnseq[yy0];
                data.unzftab[ch & 0xff] += s + 1;

                final int from = ++lastShadow;
                lastShadow += s;
                Arrays.fill(data.ll8, from, lastShadow + 1, ch);

                if (lastShadow >= limitLast) {
                    throw new IOException("Block overrun while expanding RLE in MTF, "
                            + lastShadow + " exceeds " + limitLast);
                }
            } else {
                if (++lastShadow >= limitLast) {
                    throw new IOException("Block overrun in MTF, "
                            + lastShadow + " exceeds " + limitLast);
                }
                checkBounds(nextSym, 256 + 1, "nextSym");

                final char tmp = data.getAndMoveToFrontDecode_yy[nextSym - 1];
                checkBounds(tmp, 256, "yy");
                data.unzftab[data.seqToUnseq[tmp] & 0xff]++;
                data.ll8[lastShadow] = data.seqToUnseq[tmp];

                /*
                 * This loop is hammered during decompression, hence avoid
                 * native method call overhead of System.arraycopy for very
                 * small ranges to copy.
                 */
                if (nextSym <= 16) {
                    for (int j = nextSym - 1; j > 0; )
                        data.getAndMoveToFrontDecode_yy[j] = data.getAndMoveToFrontDecode_yy[--j];
                } else
                    System.arraycopy(data.getAndMoveToFrontDecode_yy, 0, data.getAndMoveToFrontDecode_yy, 1, nextSym - 1);

                data.getAndMoveToFrontDecode_yy[0] = tmp;

                if (groupPos == 0) {
                    groupPos = Constants.G_SIZE - 1;
                    checkBounds(++groupNo, Constants.MAX_SELECTORS, "groupNo");
                    zt = data.selector[groupNo] & 0xff;
                    checkBounds(zt, Constants.N_GROUPS, "zt");
                    base_zt = data.base[zt];
                    limit_zt = data.limit[zt];
                    perm_zt = data.perm[zt];
                    minLens_zt = data.minLens[zt];
                } else {
                    groupPos--;
                }

                int zn = minLens_zt;
                checkBounds(zn, Constants.MAX_ALPHA_SIZE, "zn");
                int zvec = (int)bin.readBits(zn);
                while (zvec > limit_zt[zn]) {
                    checkBounds(++zn, Constants.MAX_ALPHA_SIZE, "zn");
                    zvec = (zvec << 1) | (int)bin.readBits(1);
                }
                final int idx = zvec - base_zt[zn];
                checkBounds(idx, Constants.MAX_ALPHA_SIZE, "zvec");
                nextSym = perm_zt[idx];
            }
        }

        last = lastShadow;
    }

    private int getAndMoveToFrontDecode0() throws IOException {
        final Bzip2InputStream.Data dataShadow = this.data;
        final int zt = dataShadow.selector[0] & 0xff;
        checkBounds(zt, Constants.N_GROUPS, "zt");
        final int[] limit_zt = dataShadow.limit[zt];
        int zn = dataShadow.minLens[zt];
        checkBounds(zn, Constants.MAX_ALPHA_SIZE, "zn");
        int zvec = (int)bin.readBits(zn);
        while (zvec > limit_zt[zn]) {
            checkBounds(++zn, Constants.MAX_ALPHA_SIZE, "zn");
            zvec = (zvec << 1) | (int)bin.readBits(1);
        }
        final int tmp = zvec - dataShadow.base[zt][zn];
        checkBounds(tmp, Constants.MAX_ALPHA_SIZE, "zvec");

        return dataShadow.perm[zt][tmp];
    }

    private int setupBlock() throws IOException {
        if (currentState == State.EOF || data == null)
            return IOUtils.EOF;

        final int[] cftab = this.data.cftab;
        final int ttLen = this.last + 1;
        final int[] tt = this.data.initTT(ttLen);
        final byte[] ll8 = this.data.ll8;
        cftab[0] = 0;
        System.arraycopy(this.data.unzftab, 0, cftab, 1, 256);

        for (int i = 1, c = cftab[0]; i <= 256; i++) {
            c += cftab[i];
            cftab[i] = c;
        }

        for (int i = 0, lastShadow = this.last; i <= lastShadow; i++) {
            final int tmp = cftab[ll8[i] & 0xff]++;
            checkBounds(tmp, ttLen, "tt index");
            tt[tmp] = i;
        }

        if ((this.origPtr < 0) || (this.origPtr >= tt.length)) {
            throw new IOException("Stream corrupted");
        }

        su_tPos = tt[this.origPtr];
        su_count = 0;
        su_i2 = 0;
        su_ch2 = 256; /* not a char and not EOF */

        if (blockRandomised) {
            su_rNToGo = 0;
            su_rTPos = 0;
            return setupRandPartA();
        }

        return setupNoRandPartA();
    }

    private int setupRandPartA() throws IOException {
        if (this.su_i2 <= this.last) {
            this.su_chPrev = this.su_ch2;
            int su_ch2Shadow = this.data.ll8[this.su_tPos] & 0xff;
            checkBounds(this.su_tPos, this.data.tt.length, "su_tPos");
            this.su_tPos = this.data.tt[this.su_tPos];
            if (this.su_rNToGo == 0) {
                this.su_rNToGo = RandomNumbers.get(this.su_rTPos) - 1;
                if (++this.su_rTPos == 512) {
                    this.su_rTPos = 0;
                }
            } else {
                this.su_rNToGo--;
            }
            this.su_ch2 = su_ch2Shadow ^= (this.su_rNToGo == 1) ? 1 : 0;
            this.su_i2++;
            this.currentState = State.RAND_PART_B_STATE;
            this.crc32.updateCRC(su_ch2Shadow);
            return su_ch2Shadow;
        }
        endBlock();
        initBlock();
        return setupBlock();
    }

    private int setupNoRandPartA() throws IOException {
        if (this.su_i2 <= this.last) {
            this.su_chPrev = this.su_ch2;
            final int su_ch2Shadow = this.data.ll8[this.su_tPos] & 0xff;
            this.su_ch2 = su_ch2Shadow;
            checkBounds(this.su_tPos, this.data.tt.length, "su_tPos");
            this.su_tPos = this.data.tt[this.su_tPos];
            this.su_i2++;
            this.currentState = State.NO_RAND_PART_B_STATE;
            this.crc32.updateCRC(su_ch2Shadow);
            return su_ch2Shadow;
        }
        this.currentState = State.NO_RAND_PART_A_STATE;
        endBlock();
        initBlock();
        return setupBlock();
    }

    private int setupRandPartB() throws IOException {
        if (su_ch2 != su_chPrev) {
            currentState = State.RAND_PART_A_STATE;
            su_count = 1;
            return setupRandPartA();
        } else if (++su_count >= 4) {
            su_z = (char)(data.ll8[su_tPos] & 0xff);
            checkBounds(su_tPos, data.tt.length, "su_tPos");
            su_tPos = data.tt[su_tPos];
            if (su_rNToGo == 0) {
                su_rNToGo = RandomNumbers.get(this.su_rTPos) - 1;
                if (++su_rTPos == 512) {
                    su_rTPos = 0;
                }
            } else {
                su_rNToGo--;
            }
            su_j2 = 0;
            currentState = State.RAND_PART_C_STATE;
            if (su_rNToGo == 1) {
                su_z ^= 1;
            }
            return setupRandPartC();
        } else {
            currentState = State.RAND_PART_A_STATE;
            return setupRandPartA();
        }
    }

    private int setupRandPartC() throws IOException {
        if (su_j2 < su_z) {
            crc32.updateCRC(su_ch2);
            su_j2++;
            return su_ch2;
        }

        currentState = State.RAND_PART_A_STATE;
        su_i2++;
        su_count = 0;
        return setupRandPartA();
    }

    private int setupNoRandPartB() throws IOException {
        if (su_ch2 != su_chPrev) {
            su_count = 1;
            return setupNoRandPartA();
        }

        if (++su_count >= 4) {
            checkBounds(su_tPos, data.ll8.length, "su_tPos");
            su_z = (char)(data.ll8[su_tPos] & 0xff);
            su_tPos = data.tt[su_tPos];
            su_j2 = 0;
            return setupNoRandPartC();
        }

        return setupNoRandPartA();
    }

    private int setupNoRandPartC() throws IOException {
        if (su_j2 < su_z) {
            crc32.updateCRC(su_ch2);
            su_j2++;
            currentState = State.NO_RAND_PART_C_STATE;
            return su_ch2;
        }

        su_i2++;
        su_count = 0;
        return setupNoRandPartA();
    }

    private static final class Data {

        // (with blockSize 900k)
        final boolean[] huffmanUsedBitmaps = new boolean[256]; // 256 byte

        final byte[] seqToUnseq = new byte[256]; // 256 byte
        final byte[] selector = new byte[Constants.MAX_SELECTORS]; // 18002 byte
        final byte[] selectorList = new byte[Constants.MAX_SELECTORS]; // 18002 byte

        /**
         * Freq table collected to save a pass over the data during
         * decompression.
         */
        final int[] unzftab = new int[256]; // 1024 byte

        final int[][] limit = new int[Constants.N_GROUPS][Constants.MAX_ALPHA_SIZE]; // 6192 byte
        final int[][] base = new int[Constants.N_GROUPS][Constants.MAX_ALPHA_SIZE]; // 6192 byte
        final int[][] perm = new int[Constants.N_GROUPS][Constants.MAX_ALPHA_SIZE]; // 6192 byte
        final int[] minLens = new int[Constants.N_GROUPS]; // 24 byte

        final int[] cftab = new int[257]; // 1028 byte
        final char[] getAndMoveToFrontDecode_yy = new char[256]; // 512 byte
        final char[][] temp_charArray2d = new char[Constants.N_GROUPS][Constants.MAX_ALPHA_SIZE]; // 3096
        // byte
        final byte[] recvDecodingTables_pos = new byte[Constants.N_GROUPS]; // 6 byte
        // ---------------
        // 60798 byte

        int[] tt; // 3600000 byte
        private final byte[] ll8; // 900000 byte

        public Data(int blockSize100k) {
            ll8 = new byte[blockSize100k * Constants.BASEBLOCKSIZE];
        }

        /**
         * Initializes the {@link #tt} array.
         * <p>
         * This method is called when the required length of the array is known.
         * I don't initialize it at construction time to avoid unneccessary
         * memory allocation when compressing small files.
         */
        int[] initTT(final int length) {
            int[] ttShadow = this.tt;

            // tt.length should always be >= length, but theoretically
            // it can happen, if the compressor mixed small and large
            // blocks. Normally only the last block will be smaller
            // than others.
            if ((ttShadow == null) || (ttShadow.length < length)) {
                this.tt = ttShadow = new int[length];
            }

            return ttShadow;
        }

        public int readHuffmanUsedBitmaps(BitInputStream in) throws IOException {
            int huffmanUsedMap = (int)in.readBits(16);

            Arrays.fill(huffmanUsedBitmaps, false);

            for (int i = 0; i < 16; i++) {
                if ((huffmanUsedMap & (1 << i)) == 0)
                    continue;

                int i16 = i << 4;

                for (int j = 0; j < 16; j++)
                    if (in.readBit())
                        huffmanUsedBitmaps[i16 + j] = true;
            }

            return makeMaps();
        }

        private int makeMaps() {
            int res = 0;

            for (int i = 0; i < huffmanUsedBitmaps.length; i++)
                if (huffmanUsedBitmaps[i])
                    seqToUnseq[res++] = (byte)i;

            return res;
        }

        public void codingTables(BitInputStream bin, int huffmanGroups, int alphaSize) throws IOException {
            for (int i = 0; i < huffmanGroups; i++) {
                int curr = (int)bin.readBits(5);

                for (int j = 0; j < alphaSize; j++) {
                    while (bin.readBit())
                        curr += bin.readBit() ? -1 : 1;

                    temp_charArray2d[i][j] = (char)curr;
                }
            }
        }

    }

    /**
     * Checks if the signature matches what is expected for a bzip2 file.
     *
     * @param signature the bytes to check
     * @param length    the number of bytes to check
     * @return true, if this stream is a bzip2 compressed stream, false otherwise
     * @since 1.1
     */
    public static boolean matches(final byte[] signature, final int length) {
        return length >= 3 && signature[0] == 'B' &&
                signature[1] == 'Z' && signature[2] == 'h';
    }

    private enum State {
        EOF {
            @Override
            public int read(Bzip2InputStream in) throws IOException {
                return IOUtils.EOF;
            }
        },
        START_BLOCK_STATE {
            @Override
            public int read(Bzip2InputStream in) throws IOException {
                return in.setupBlock();
            }
        },
        RAND_PART_A_STATE,
        RAND_PART_B_STATE {
            @Override
            public int read(Bzip2InputStream in) throws IOException {
                return in.setupRandPartB();
            }
        },
        RAND_PART_C_STATE {
            @Override
            public int read(Bzip2InputStream in) throws IOException {
                return in.setupRandPartC();
            }
        },
        NO_RAND_PART_A_STATE,
        NO_RAND_PART_B_STATE {
            @Override
            public int read(Bzip2InputStream in) throws IOException {
                return in.setupNoRandPartB();
            }
        },
        NO_RAND_PART_C_STATE {
            @Override
            public int read(Bzip2InputStream in) throws IOException {
                return in.setupNoRandPartC();
            }
        };

        public int read(Bzip2InputStream in) throws IOException {
            throw new IllegalStateException();
        }
    }
}
