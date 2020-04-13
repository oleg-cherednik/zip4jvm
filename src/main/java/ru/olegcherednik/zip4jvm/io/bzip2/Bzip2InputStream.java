package ru.olegcherednik.zip4jvm.io.bzip2;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * An input stream that decompresses from the BZip2 format to be read as any other stream.
 *
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
public class Bzip2InputStream extends InputStream {

    private final BitInputStream bin;

    /**
     * Index of the last char in the block, so the block size == last + 1.
     */
    private int last;

    /**
     * Index in zptr[] of original string after sorting.
     */
    private int origPtr;

    /**
     * always: in the range 0 .. 9. The current block size is 100000 * this
     * number.
     */
    private int blockSize100k;

    private boolean blockRandomised;

    private final CRC32 crc32 = new CRC32();

    private int nInUse;

    private State currentState = State.START_BLOCK_STATE;

    private int storedBlockCRC, storedCombinedCRC;
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

    /**
     * All memory intensive stuff. This field is initialized by initBlock().
     */
    private Bzip2InputStream.Data data;

    public Bzip2InputStream(DataInput in, int blocSize) throws IOException {
        bin = new BitInputStream(in);
        blockSize100k = blocSize;
        initBlock();
    }

    @Override
    public int read() throws IOException {
        return currentState.read(this);
    }

    @Override
    public int read(final byte[] dest, final int offs, final int len) throws IOException {
        if (len == 0)
            return 0;

        final int hi = offs + len;
        int destOffs = offs;
        int b;

        while (destOffs < hi && ((b = currentState.read(this)) >= 0)) {
            dest[destOffs++] = (byte)b;
        }

        return (destOffs == offs) ? -1 : (destOffs - offs);
    }

    private void makeMaps() {
        final boolean[] inUse = this.data.inUse;
        final byte[] seqToUnseq = this.data.seqToUnseq;

        int nInUseShadow = 0;

        for (int i = 0; i < 256; i++) {
            if (inUse[i]) {
                seqToUnseq[nInUseShadow++] = (byte)i;
            }
        }

        this.nInUse = nInUseShadow;
    }

    private void initBlock() throws IOException {
        char magic0;
        char magic1;
        char magic2;
        char magic3;
        char magic4;
        char magic5;

        while (true) {
            // Get the block magic bytes.
            magic0 = bin.bsGetUByte();
            magic1 = bin.bsGetUByte();
            magic2 = bin.bsGetUByte();
            magic3 = bin.bsGetUByte();
            magic4 = bin.bsGetUByte();
            magic5 = bin.bsGetUByte();

            // If isn't end of stream magic, break out of the loop.
            if (magic0 != 0x17 || magic1 != 0x72 || magic2 != 0x45 || magic3 != 0x38 || magic4 != 0x50 || magic5 != 0x90) {
                break;
            }

            // End of stream was reached. Check the combined CRC and
            // advance to the next .bz2 stream if decoding concatenated
            // streams.
            if (complete()) {
                return;
            }
        }

        if (magic0 != 0x31 || // '1'
                magic1 != 0x41 || // ')'
                magic2 != 0x59 || // 'Y'
                magic3 != 0x26 || // '&'
                magic4 != 0x53 || // 'S'
                magic5 != 0x59 // 'Y'
        ) {
            currentState = State.EOF;
            throw new IOException("Bad block header");
        }
        this.storedBlockCRC = bin.bsGetInt();
        this.blockRandomised = bin.bsR(1) == 1;

        /**
         * Allocate data here instead in constructor, so we do not allocate
         * it if the input file is empty.
         */
        if (this.data == null) {
            this.data = new Bzip2InputStream.Data(this.blockSize100k);
        }

        // currBlockNo++;
        getAndMoveToFrontDecode();

        crc32.initialiseCRC();
        currentState = State.START_BLOCK_STATE;
    }

    private void endBlock() throws IOException {
        this.computedBlockCRC = this.crc32.getFinalCRC();

        // A bad CRC is considered a fatal error.
        if (this.storedBlockCRC != this.computedBlockCRC) {
            // make next blocks readable without error
            // (repair feature, not yet documented, not tested)
            this.computedCombinedCRC = (this.storedCombinedCRC << 1)
                    | (this.storedCombinedCRC >>> 31);
            this.computedCombinedCRC ^= this.storedBlockCRC;

            throw new IOException("BZip2 CRC error");
        }

        this.computedCombinedCRC = (this.computedCombinedCRC << 1)
                | (this.computedCombinedCRC >>> 31);
        this.computedCombinedCRC ^= this.computedBlockCRC;
    }

    private boolean complete() throws IOException {
        this.storedCombinedCRC = bin.bsGetInt();
        this.currentState = State.EOF;
        this.data = null;

        if (this.storedCombinedCRC != this.computedCombinedCRC) {
            throw new IOException("BZip2 CRC error");
        }

        return true;
    }

    @Override
    public void close() throws IOException {
        this.data = null;
    }

    private static void checkBounds(final int checkVal, final int limitExclusive, String name)
            throws IOException {
        if (checkVal < 0) {
            throw new IOException("Corrupted input, " + name + " value negative");
        }
        if (checkVal >= limitExclusive) {
            throw new IOException("Corrupted input, " + name + " value too big");
        }
    }

    /**
     * Called by createHuffmanDecodingTables() exclusively.
     */
    private static void hbCreateDecodeTables(final int[] limit,
            final int[] base, final int[] perm, final char[] length,
            final int minLen, final int maxLen, final int alphaSize)
            throws IOException {
        for (int i = minLen, pp = 0; i <= maxLen; i++) {
            for (int j = 0; j < alphaSize; j++) {
                if (length[j] == i) {
                    perm[pp++] = j;
                }
            }
        }

        for (int i = Constants.MAX_CODE_LEN; --i > 0; ) {
            base[i] = 0;
            limit[i] = 0;
        }

        for (int i = 0; i < alphaSize; i++) {
            final int l = length[i];
            checkBounds(l, Constants.MAX_ALPHA_SIZE, "length");
            base[l + 1]++;
        }

        for (int i = 1, b = base[0]; i < Constants.MAX_CODE_LEN; i++) {
            b += base[i];
            base[i] = b;
        }

        for (int i = minLen, vec = 0, b = base[i]; i <= maxLen; i++) {
            final int nb = base[i + 1];
            vec += nb - b;
            b = nb;
            limit[i] = vec - 1;
            vec <<= 1;
        }

        for (int i = minLen + 1; i <= maxLen; i++) {
            base[i] = ((limit[i - 1] + 1) << 1) - base[i];
        }
    }

    private void recvDecodingTables() throws IOException {
        final BitInputStream bin = this.bin;
        final Bzip2InputStream.Data dataShadow = this.data;
        final boolean[] inUse = dataShadow.inUse;
        final byte[] pos = dataShadow.recvDecodingTables_pos;
        final byte[] selector = dataShadow.selector;
        final byte[] selectorMtf = dataShadow.selectorMtf;

        int inUse16 = 0;

        /* Receive the mapping table */
        for (int i = 0; i < 16; i++) {
            if (bin.bsGetBit()) {
                inUse16 |= 1 << i;
            }
        }

        Arrays.fill(inUse, false);
        for (int i = 0; i < 16; i++) {
            if ((inUse16 & (1 << i)) != 0) {
                final int i16 = i << 4;
                for (int j = 0; j < 16; j++) {
                    if (bin.bsGetBit()) {
                        inUse[i16 + j] = true;
                    }
                }
            }
        }

        makeMaps();
        final int alphaSize = this.nInUse + 2;
        /* Now the selectors */
        final int nGroups = bin.bsR(3);
        final int selectors = bin.bsR(15);
        if (selectors < 0) {
            throw new IOException("Corrupted input, nSelectors value negative");
        }
        checkBounds(alphaSize, Constants.MAX_ALPHA_SIZE + 1, "alphaSize");
        checkBounds(nGroups, Constants.N_GROUPS + 1, "nGroups");

        // Don't fail on nSelectors overflowing boundaries but discard the values in overflow
        // See https://gnu.wildebeest.org/blog/mjw/2019/08/02/bzip2-and-the-cve-that-wasnt/
        // and https://sourceware.org/ml/bzip2-devel/2019-q3/msg00007.html

        for (int i = 0; i < selectors; i++) {
            int j = 0;
            while (bin.bsGetBit()) {
                j++;
            }
            if (i < Constants.MAX_SELECTORS) {
                selectorMtf[i] = (byte)j;
            }
        }
        final int nSelectors = selectors > Constants.MAX_SELECTORS ? Constants.MAX_SELECTORS : selectors;

        /* Undo the MTF values for the selectors. */
        for (int v = nGroups; --v >= 0; ) {
            pos[v] = (byte)v;
        }

        for (int i = 0; i < nSelectors; i++) {
            int v = selectorMtf[i] & 0xff;
            checkBounds(v, Constants.N_GROUPS, "selectorMtf");
            final byte tmp = pos[v];
            while (v > 0) {
                // nearly all times v is zero, 4 in most other cases
                pos[v] = pos[v - 1];
                v--;
            }
            pos[0] = tmp;
            selector[i] = tmp;
        }

        final char[][] len = dataShadow.temp_charArray2d;

        /* Now the coding tables */
        for (int t = 0; t < nGroups; t++) {
            int curr = bin.bsR(5);
            final char[] len_t = len[t];
            for (int i = 0; i < alphaSize; i++) {
                while (bin.bsGetBit()) {
                    curr += bin.bsGetBit() ? -1 : 1;
                }
                len_t[i] = (char)curr;
            }
        }

        // finally create the Huffman tables
        createHuffmanDecodingTables(alphaSize, nGroups);
    }

    /**
     * Called by recvDecodingTables() exclusively.
     */
    private void createHuffmanDecodingTables(final int alphaSize,
            final int nGroups) throws IOException {
        final Bzip2InputStream.Data dataShadow = this.data;
        final char[][] len = dataShadow.temp_charArray2d;
        final int[] minLens = dataShadow.minLens;
        final int[][] limit = dataShadow.limit;
        final int[][] base = dataShadow.base;
        final int[][] perm = dataShadow.perm;

        for (int t = 0; t < nGroups; t++) {
            int minLen = 32;
            int maxLen = 0;
            final char[] len_t = len[t];
            for (int i = alphaSize; --i >= 0; ) {
                final char lent = len_t[i];
                if (lent > maxLen) {
                    maxLen = lent;
                }
                if (lent < minLen) {
                    minLen = lent;
                }
            }
            hbCreateDecodeTables(limit[t], base[t], perm[t], len[t], minLen,
                    maxLen, alphaSize);
            minLens[t] = minLen;
        }
    }

    private void getAndMoveToFrontDecode() throws IOException {
        final BitInputStream bin = this.bin;
        this.origPtr = bin.bsR(24);
        recvDecodingTables();

        final Bzip2InputStream.Data dataShadow = this.data;
        final byte[] ll8 = dataShadow.ll8;
        final int[] unzftab = dataShadow.unzftab;
        final byte[] selector = dataShadow.selector;
        final byte[] seqToUnseq = dataShadow.seqToUnseq;
        final char[] yy = dataShadow.getAndMoveToFrontDecode_yy;
        final int[] minLens = dataShadow.minLens;
        final int[][] limit = dataShadow.limit;
        final int[][] base = dataShadow.base;
        final int[][] perm = dataShadow.perm;
        final int limitLast = this.blockSize100k * 100000;

        /*
         * Setting up the unzftab entries here is not strictly necessary, but it
         * does save having to do it later in a separate pass, and so saves a
         * block's worth of cache misses.
         */
        for (int i = 256; --i >= 0; ) {
            yy[i] = (char)i;
            unzftab[i] = 0;
        }

        int groupNo = 0;
        int groupPos = Constants.G_SIZE - 1;
        final int eob = this.nInUse + 1;
        int nextSym = getAndMoveToFrontDecode0();
        int lastShadow = -1;
        int zt = selector[groupNo] & 0xff;
        checkBounds(zt, Constants.N_GROUPS, "zt");
        int[] base_zt = base[zt];
        int[] limit_zt = limit[zt];
        int[] perm_zt = perm[zt];
        int minLens_zt = minLens[zt];

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
                        zt = selector[groupNo] & 0xff;
                        checkBounds(zt, Constants.N_GROUPS, "zt");
                        base_zt = base[zt];
                        limit_zt = limit[zt];
                        perm_zt = perm[zt];
                        minLens_zt = minLens[zt];
                    } else {
                        groupPos--;
                    }

                    int zn = minLens_zt;
                    checkBounds(zn, Constants.MAX_ALPHA_SIZE, "zn");
                    int zvec = bin.bsR(zn);
                    while (zvec > limit_zt[zn]) {
                        checkBounds(++zn, Constants.MAX_ALPHA_SIZE, "zn");
                        zvec = (zvec << 1) | bin.bsR(1);
                    }
                    final int tmp = zvec - base_zt[zn];
                    checkBounds(tmp, Constants.MAX_ALPHA_SIZE, "zvec");
                    nextSym = perm_zt[tmp];
                }

                final int yy0 = yy[0];
                checkBounds(yy0, 256, "yy");
                final byte ch = seqToUnseq[yy0];
                unzftab[ch & 0xff] += s + 1;

                final int from = ++lastShadow;
                lastShadow += s;
                Arrays.fill(ll8, from, lastShadow + 1, ch);

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

                final char tmp = yy[nextSym - 1];
                checkBounds(tmp, 256, "yy");
                unzftab[seqToUnseq[tmp] & 0xff]++;
                ll8[lastShadow] = seqToUnseq[tmp];

                /*
                 * This loop is hammered during decompression, hence avoid
                 * native method call overhead of System.arraycopy for very
                 * small ranges to copy.
                 */
                if (nextSym <= 16) {
                    for (int j = nextSym - 1; j > 0; ) {
                        yy[j] = yy[--j];
                    }
                } else {
                    System.arraycopy(yy, 0, yy, 1, nextSym - 1);
                }

                yy[0] = tmp;

                if (groupPos == 0) {
                    groupPos = Constants.G_SIZE - 1;
                    checkBounds(++groupNo, Constants.MAX_SELECTORS, "groupNo");
                    zt = selector[groupNo] & 0xff;
                    checkBounds(zt, Constants.N_GROUPS, "zt");
                    base_zt = base[zt];
                    limit_zt = limit[zt];
                    perm_zt = perm[zt];
                    minLens_zt = minLens[zt];
                } else {
                    groupPos--;
                }

                int zn = minLens_zt;
                checkBounds(zn, Constants.MAX_ALPHA_SIZE, "zn");
                int zvec = bin.bsR(zn);
                while (zvec > limit_zt[zn]) {
                    checkBounds(++zn, Constants.MAX_ALPHA_SIZE, "zn");
                    zvec = (zvec << 1) | bin.bsR(1);
                }
                final int idx = zvec - base_zt[zn];
                checkBounds(idx, Constants.MAX_ALPHA_SIZE, "zvec");
                nextSym = perm_zt[idx];
            }
        }

        this.last = lastShadow;
    }

    private int getAndMoveToFrontDecode0() throws IOException {
        final Bzip2InputStream.Data dataShadow = this.data;
        final int zt = dataShadow.selector[0] & 0xff;
        checkBounds(zt, Constants.N_GROUPS, "zt");
        final int[] limit_zt = dataShadow.limit[zt];
        int zn = dataShadow.minLens[zt];
        checkBounds(zn, Constants.MAX_ALPHA_SIZE, "zn");
        int zvec = bin.bsR(zn);
        while (zvec > limit_zt[zn]) {
            checkBounds(++zn, Constants.MAX_ALPHA_SIZE, "zn");
            zvec = (zvec << 1) | bin.bsR(1);
        }
        final int tmp = zvec - dataShadow.base[zt][zn];
        checkBounds(tmp, Constants.MAX_ALPHA_SIZE, "zvec");

        return dataShadow.perm[zt][tmp];
    }

    private int setupBlock() throws IOException {
        if (currentState == State.EOF || this.data == null) {
            return -1;
        }

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

        this.su_tPos = tt[this.origPtr];
        this.su_count = 0;
        this.su_i2 = 0;
        this.su_ch2 = 256; /* not a char and not EOF */

        if (this.blockRandomised) {
            this.su_rNToGo = 0;
            this.su_rTPos = 0;
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
        if (this.su_ch2 != this.su_chPrev) {
            this.currentState = State.RAND_PART_A_STATE;
            this.su_count = 1;
            return setupRandPartA();
        } else if (++this.su_count >= 4) {
            this.su_z = (char)(this.data.ll8[this.su_tPos] & 0xff);
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
            this.su_j2 = 0;
            this.currentState = State.RAND_PART_C_STATE;
            if (this.su_rNToGo == 1) {
                this.su_z ^= 1;
            }
            return setupRandPartC();
        } else {
            this.currentState = State.RAND_PART_A_STATE;
            return setupRandPartA();
        }
    }

    private int setupRandPartC() throws IOException {
        if (this.su_j2 < this.su_z) {
            this.crc32.updateCRC(this.su_ch2);
            this.su_j2++;
            return this.su_ch2;
        }
        this.currentState = State.RAND_PART_A_STATE;
        this.su_i2++;
        this.su_count = 0;
        return setupRandPartA();
    }

    private int setupNoRandPartB() throws IOException {
        if (this.su_ch2 != this.su_chPrev) {
            this.su_count = 1;
            return setupNoRandPartA();
        } else if (++this.su_count >= 4) {
            checkBounds(this.su_tPos, this.data.ll8.length, "su_tPos");
            this.su_z = (char)(this.data.ll8[this.su_tPos] & 0xff);
            this.su_tPos = this.data.tt[this.su_tPos];
            this.su_j2 = 0;
            return setupNoRandPartC();
        } else {
            return setupNoRandPartA();
        }
    }

    private int setupNoRandPartC() throws IOException {
        if (this.su_j2 < this.su_z) {
            final int su_ch2Shadow = this.su_ch2;
            this.crc32.updateCRC(su_ch2Shadow);
            this.su_j2++;
            this.currentState = State.NO_RAND_PART_C_STATE;
            return su_ch2Shadow;
        }
        this.su_i2++;
        this.su_count = 0;
        return setupNoRandPartA();
    }

    private static final class Data {

        // (with blockSize 900k)
        final boolean[] inUse = new boolean[256]; // 256 byte

        final byte[] seqToUnseq = new byte[256]; // 256 byte
        final byte[] selector = new byte[Constants.MAX_SELECTORS]; // 18002 byte
        final byte[] selectorMtf = new byte[Constants.MAX_SELECTORS]; // 18002 byte

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
        byte[] ll8; // 900000 byte

        // ---------------
        // 4560782 byte
        // ===============

        Data(final int blockSize100k) {
            this.ll8 = new byte[blockSize100k * Constants.BASEBLOCKSIZE];
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
