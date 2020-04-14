package ru.olegcherednik.zip4jvm.io.bzip2;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
public class Bzip2OutputStream extends OutputStream {

    private static final int GREATER_ICOST = 15;
    private static final int LESSER_ICOST = 0;

    private static void hbMakeCodeLengths(final byte[] len, final int[] freq,
            final Data dat, final int alphaSize,
            final int maxLen) {
        /*
         * Nodes and heap entries run from 1. Entry 0 for both the heap and
         * nodes is a sentinel.
         */
        final int[] heap = dat.heap;
        final int[] weight = dat.weight;
        final int[] parent = dat.parent;

        for (int i = alphaSize; --i >= 0; ) {
            weight[i + 1] = (freq[i] == 0 ? 1 : freq[i]) << 8;
        }

        for (boolean tooLong = true; tooLong; ) {
            tooLong = false;

            int nNodes = alphaSize;
            int nHeap = 0;
            heap[0] = 0;
            weight[0] = 0;
            parent[0] = -2;

            for (int i = 1; i <= alphaSize; i++) {
                parent[i] = -1;
                nHeap++;
                heap[nHeap] = i;

                int zz = nHeap;
                final int tmp = heap[zz];
                while (weight[tmp] < weight[heap[zz >> 1]]) {
                    heap[zz] = heap[zz >> 1];
                    zz >>= 1;
                }
                heap[zz] = tmp;
            }

            while (nHeap > 1) {
                final int n1 = heap[1];
                heap[1] = heap[nHeap];
                nHeap--;

                int yy = 0;
                int zz = 1;
                int tmp = heap[1];

                while (true) {
                    yy = zz << 1;

                    if (yy > nHeap) {
                        break;
                    }

                    if ((yy < nHeap)
                            && (weight[heap[yy + 1]] < weight[heap[yy]])) {
                        yy++;
                    }

                    if (weight[tmp] < weight[heap[yy]]) {
                        break;
                    }

                    heap[zz] = heap[yy];
                    zz = yy;
                }

                heap[zz] = tmp;

                final int n2 = heap[1];
                heap[1] = heap[nHeap];
                nHeap--;

                yy = 0;
                zz = 1;
                tmp = heap[1];

                while (true) {
                    yy = zz << 1;

                    if (yy > nHeap) {
                        break;
                    }

                    if ((yy < nHeap)
                            && (weight[heap[yy + 1]] < weight[heap[yy]])) {
                        yy++;
                    }

                    if (weight[tmp] < weight[heap[yy]]) {
                        break;
                    }

                    heap[zz] = heap[yy];
                    zz = yy;
                }

                heap[zz] = tmp;
                nNodes++;
                parent[n1] = parent[n2] = nNodes;

                final int weight_n1 = weight[n1];
                final int weight_n2 = weight[n2];
                weight[nNodes] = ((weight_n1 & 0xffffff00)
                        + (weight_n2 & 0xffffff00))
                        | (1 + (((weight_n1 & 0x000000ff)
                        > (weight_n2 & 0x000000ff))
                                ? (weight_n1 & 0x000000ff)
                                : (weight_n2 & 0x000000ff)));

                parent[nNodes] = -1;
                nHeap++;
                heap[nHeap] = nNodes;

                tmp = 0;
                zz = nHeap;
                tmp = heap[zz];
                final int weight_tmp = weight[tmp];
                while (weight_tmp < weight[heap[zz >> 1]]) {
                    heap[zz] = heap[zz >> 1];
                    zz >>= 1;
                }
                heap[zz] = tmp;

            }

            for (int i = 1; i <= alphaSize; i++) {
                int j = 0;
                int k = i;

                for (int parent_k; (parent_k = parent[k]) >= 0; ) {
                    k = parent_k;
                    j++;
                }

                len[i - 1] = (byte)j;
                if (j > maxLen) {
                    tooLong = true;
                }
            }

            if (tooLong) {
                for (int i = 1; i < alphaSize; i++) {
                    int j = weight[i] >> 8;
                    j = 1 + (j >> 1);
                    weight[i] = j << 8;
                }
            }
        }
    }

    /**
     * Index of the last char in the block, so the block size == last + 1.
     */
    private int last;

    private final int blockSize100k;

    private int bsBuff;
    private int bsLive;
    private final CRC32 crc32 = new CRC32();

    private int nInUse;

    private int nMTF;

    private int currentChar = -1;
    private int runLength = 0;

    private int blockCRC;
    private int combinedCRC;
    private final int allowableBlockSize;

    private Data data;
    private BlockSort blockSorter;

    private final DataOutput out;
    private volatile boolean closed;

    public Bzip2OutputStream(DataOutput out, CompressionLevel compressionLevel) throws IOException {
        blockSize100k = compressionLevel.getCode();
        this.out = out;

        /* 20 is just a paranoia constant */
        this.allowableBlockSize = (this.blockSize100k * Constants.BASEBLOCKSIZE) - 20;
    }

    @Override
    public void write(final int b) throws IOException {
        init();
        if (!closed)
            write0(b);
        else
            throw new IOException("Closed");
    }

    /**
     * Writes the current byte to the buffer, run-length encoding it
     * if it has been repeated at least four times (the first step
     * RLEs sequences of four identical bytes).
     *
     * <p>Flushes the current block before writing data if it is
     * full.</p>
     *
     * <p>"write to the buffer" means adding to data.buffer starting
     * two steps "after" this.last - initially starting at index 1
     * (not 0) - and updating this.last to point to the last index
     * written minus 1.</p>
     */
    private void writeRun() throws IOException {
        final int lastShadow = this.last;

        if (lastShadow < this.allowableBlockSize) {
            final int currentCharShadow = this.currentChar;
            final Data dataShadow = this.data;
            dataShadow.inUse[currentCharShadow] = true;
            final byte ch = (byte)currentCharShadow;

            int runLengthShadow = this.runLength;
            this.crc32.update(currentCharShadow, runLengthShadow);

            switch (runLengthShadow) {
                case 1:
                    dataShadow.block[lastShadow + 2] = ch;
                    this.last = lastShadow + 1;
                    break;

                case 2:
                    dataShadow.block[lastShadow + 2] = ch;
                    dataShadow.block[lastShadow + 3] = ch;
                    this.last = lastShadow + 2;
                    break;

                case 3: {
                    final byte[] block = dataShadow.block;
                    block[lastShadow + 2] = ch;
                    block[lastShadow + 3] = ch;
                    block[lastShadow + 4] = ch;
                    this.last = lastShadow + 3;
                }
                break;

                default: {
                    runLengthShadow -= 4;
                    dataShadow.inUse[runLengthShadow] = true;
                    final byte[] block = dataShadow.block;
                    block[lastShadow + 2] = ch;
                    block[lastShadow + 3] = ch;
                    block[lastShadow + 4] = ch;
                    block[lastShadow + 5] = ch;
                    block[lastShadow + 6] = (byte)runLengthShadow;
                    this.last = lastShadow + 5;
                }
                break;

            }
        } else {
            endBlock();
            initBlock();
            writeRun();
        }
    }

    /**
     * Overriden to warn about an unclosed stream.
     */
    @Override
    protected void finalize() throws Throwable {
        if (!closed) {
            System.err.println("Unclosed BZip2CompressorOutputStream detected, will *not* close it");
        }
        super.finalize();
    }


    public void finish() throws IOException {
        if (!closed) {
            closed = true;
            try {
                if (this.runLength > 0) {
                    writeRun();
                }
                this.currentChar = -1;
                endBlock();
                endCompression();
            } finally {
                this.blockSorter = null;
                this.data = null;
            }
        }
    }

    @Override
    public void close() throws IOException {
        finish();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    private boolean init = true;

    /**
     * Writes magic bytes like BZ on the first position of the stream
     * and bytes indiciating the file-format, which is
     * huffmanised, followed by a digit indicating blockSize100k.
     *
     * @throws IOException if the magic bytes could not been written
     */
    private void init() throws IOException {
        if (!init)
            return;

        init = false;
        bsPutUByte('B');
        bsPutUByte('Z');

        this.data = new Data(this.blockSize100k);
        this.blockSorter = new BlockSort(this.data);

        // huffmanised magic bytes
        bsPutUByte('h');
        bsPutUByte('0' + this.blockSize100k);

        this.combinedCRC = 0;
        initBlock();
    }

    private void initBlock() {
        // blockNo++;
        this.crc32.init();
        this.last = -1;
        // ch = 0;

        final boolean[] inUse = this.data.inUse;
        for (int i = 256; --i >= 0; ) {
            inUse[i] = false;
        }

    }

    private void endBlock() throws IOException {
        this.blockCRC = this.crc32.checksum();
        this.combinedCRC = (this.combinedCRC << 1) | (this.combinedCRC >>> 31);
        this.combinedCRC ^= this.blockCRC;

        // empty block at end of file
        if (this.last == -1) {
            return;
        }

        /* sort the block and establish posn of original string */
        blockSort();

        /*
         * A 6-byte block header, the value chosen arbitrarily as 0x314159265359
         * :-). A 32 bit value does not really give a strong enough guarantee
         * that the value will not appear by chance in the compressed
         * datastream. Worst-case probability of this event, for a 900k block,
         * is about 2.0e-3 for 32 bits, 1.0e-5 for 40 bits and 4.0e-8 for 48
         * bits. For a compressed file of size 100Gb -- about 100000 blocks --
         * only a 48-bit marker will do. NB: normal compression/ decompression
         * donot rely on these statistical properties. They are only important
         * when trying to recover blocks from damaged files.
         */
        bsPutUByte(0x31);
        bsPutUByte(0x41);
        bsPutUByte(0x59);
        bsPutUByte(0x26);
        bsPutUByte(0x53);
        bsPutUByte(0x59);

        /* Now the block's CRC, so it is in a known place. */
        bsPutInt(this.blockCRC);

        /* Now a single bit indicating no randomisation. */
        bsW(1, 0);

        /* Finally, block's contents proper. */
        moveToFrontCodeAndSend();
    }

    private void endCompression() throws IOException {
        /*
         * Now another magic 48-bit number, 0x177245385090, to indicate the end
         * of the last block. (sqrt(pi), if you want to know. I did want to use
         * e, but it contains too much repetition -- 27 18 28 18 28 46 -- for me
         * to feel statistically comfortable. Call me paranoid.)
         */
        bsPutUByte(0x17);
        bsPutUByte(0x72);
        bsPutUByte(0x45);
        bsPutUByte(0x38);
        bsPutUByte(0x50);
        bsPutUByte(0x90);

        bsPutInt(this.combinedCRC);
        bsFinishedWithStream();
    }

    @Override
    public void write(final byte[] buf, int offs, final int len) throws IOException {
        if (offs < 0)
            throw new IndexOutOfBoundsException("offs(" + offs + ") < 0.");
        if (len < 0)
            throw new IndexOutOfBoundsException("len(" + len + ") < 0.");
        if (offs + len > buf.length)
            throw new IndexOutOfBoundsException("offs(" + offs + ") + len(" + len + ") > buf.length(" + buf.length + ").");
        if (closed)
            throw new IOException("Stream closed");

        init();

        for (final int hi = offs + len; offs < hi; )
            write0(buf[offs++]);
    }

    /**
     * Keeps track of the last bytes written and implicitly performs
     * run-length encoding as the first step of the bzip2 algorithm.
     */
    private void write0(int b) throws IOException {
        if (this.currentChar != -1) {
            b &= 0xff;
            if (this.currentChar == b) {
                if (++this.runLength > 254) {
                    writeRun();
                    this.currentChar = -1;
                    this.runLength = 0;
                }
                // else nothing to do
            } else {
                writeRun();
                this.runLength = 1;
                this.currentChar = b;
            }
        } else {
            this.currentChar = b & 0xff;
            this.runLength++;
        }
    }

    private static void hbAssignCodes(final int[] code, final byte[] length,
            final int minLen, final int maxLen,
            final int alphaSize) {
        int vec = 0;
        for (int n = minLen; n <= maxLen; n++) {
            for (int i = 0; i < alphaSize; i++) {
                if ((length[i] & 0xff) == n) {
                    code[i] = vec;
                    vec++;
                }
            }
            vec <<= 1;
        }
    }

    private void bsFinishedWithStream() throws IOException {
        while (this.bsLive > 0) {
            final int ch = this.bsBuff >> 24;
            out.writeByte(ch); // write 8-bit
            this.bsBuff <<= 8;
            this.bsLive -= 8;
        }
    }

    private void bsW(final int n, final int v) throws IOException {
        int bsLiveShadow = this.bsLive;
        int bsBuffShadow = this.bsBuff;

        while (bsLiveShadow >= 8) {
            out.writeByte(bsBuffShadow >> 24); // write 8-bit
            bsBuffShadow <<= 8;
            bsLiveShadow -= 8;
        }

        this.bsBuff = bsBuffShadow | (v << (32 - bsLiveShadow - n));
        this.bsLive = bsLiveShadow + n;
    }

    private void bsPutUByte(final int c) throws IOException {
        bsW(8, c);
    }

    private void bsPutInt(final int u) throws IOException {
        bsW(8, (u >> 24) & 0xff);
        bsW(8, (u >> 16) & 0xff);
        bsW(8, (u >> 8) & 0xff);
        bsW(8, u & 0xff);
    }

    private void sendMTFValues() throws IOException {
        final byte[][] len = this.data.sendMTFValues_len;
        final int alphaSize = this.nInUse + 2;

        for (int t = Constants.N_GROUPS; --t >= 0; ) {
            final byte[] len_t = len[t];
            for (int v = alphaSize; --v >= 0; ) {
                len_t[v] = GREATER_ICOST;
            }
        }

        /* Decide how many coding tables to use */
        // assert (this.nMTF > 0) : this.nMTF;
        final int nGroups = (this.nMTF < 200) ? 2 : (this.nMTF < 600) ? 3
                                                                      : (this.nMTF < 1200) ? 4 : (this.nMTF < 2400) ? 5 : 6;

        /* Generate an initial set of coding tables */
        sendMTFValues0(nGroups, alphaSize);

        /*
         * Iterate up to N_ITERS times to improve the tables.
         */
        final int nSelectors = sendMTFValues1(nGroups, alphaSize);

        /* Compute MTF values for the selectors. */
        sendMTFValues2(nGroups, nSelectors);

        /* Assign actual codes for the tables. */
        sendMTFValues3(nGroups, alphaSize);

        /* Transmit the mapping table. */
        sendMTFValues4();

        /* Now the selectors. */
        sendMTFValues5(nGroups, nSelectors);

        /* Now the coding tables. */
        sendMTFValues6(nGroups, alphaSize);

        /* And finally, the block data proper */
        sendMTFValues7();
    }

    private void sendMTFValues0(final int nGroups, final int alphaSize) {
        final byte[][] len = this.data.sendMTFValues_len;
        final int[] mtfFreq = this.data.mtfFreq;

        int remF = this.nMTF;
        int gs = 0;

        for (int nPart = nGroups; nPart > 0; nPart--) {
            final int tFreq = remF / nPart;
            int ge = gs - 1;
            int aFreq = 0;

            for (final int a = alphaSize - 1; (aFreq < tFreq) && (ge < a); ) {
                aFreq += mtfFreq[++ge];
            }

            if ((ge > gs) && (nPart != nGroups) && (nPart != 1)
                    && (((nGroups - nPart) & 1) != 0)) {
                aFreq -= mtfFreq[ge--];
            }

            final byte[] len_np = len[nPart - 1];
            for (int v = alphaSize; --v >= 0; ) {
                if ((v >= gs) && (v <= ge)) {
                    len_np[v] = LESSER_ICOST;
                } else {
                    len_np[v] = GREATER_ICOST;
                }
            }

            gs = ge + 1;
            remF -= aFreq;
        }
    }

    private int sendMTFValues1(final int nGroups, final int alphaSize) {
        final Data dataShadow = this.data;
        final int[][] rfreq = dataShadow.sendMTFValues_rfreq;
        final int[] fave = dataShadow.sendMTFValues_fave;
        final short[] cost = dataShadow.sendMTFValues_cost;
        final char[] sfmap = dataShadow.sfmap;
        final byte[] selector = dataShadow.selector;
        final byte[][] len = dataShadow.sendMTFValues_len;
        final byte[] len_0 = len[0];
        final byte[] len_1 = len[1];
        final byte[] len_2 = len[2];
        final byte[] len_3 = len[3];
        final byte[] len_4 = len[4];
        final byte[] len_5 = len[5];
        final int nMTFShadow = this.nMTF;

        int nSelectors = 0;

        for (int iter = 0; iter < Constants.N_ITERS; iter++) {
            for (int t = nGroups; --t >= 0; ) {
                fave[t] = 0;
                final int[] rfreqt = rfreq[t];
                for (int i = alphaSize; --i >= 0; ) {
                    rfreqt[i] = 0;
                }
            }

            nSelectors = 0;

            for (int gs = 0; gs < this.nMTF; ) {
                /* Set group start & end marks. */

                /*
                 * Calculate the cost of this group as coded by each of the
                 * coding tables.
                 */

                final int ge = Math.min(gs + Constants.G_SIZE - 1, nMTFShadow - 1);

                if (nGroups == Constants.N_GROUPS) {
                    // unrolled version of the else-block

                    short cost0 = 0;
                    short cost1 = 0;
                    short cost2 = 0;
                    short cost3 = 0;
                    short cost4 = 0;
                    short cost5 = 0;

                    for (int i = gs; i <= ge; i++) {
                        final int icv = sfmap[i];
                        cost0 += len_0[icv] & 0xff;
                        cost1 += len_1[icv] & 0xff;
                        cost2 += len_2[icv] & 0xff;
                        cost3 += len_3[icv] & 0xff;
                        cost4 += len_4[icv] & 0xff;
                        cost5 += len_5[icv] & 0xff;
                    }

                    cost[0] = cost0;
                    cost[1] = cost1;
                    cost[2] = cost2;
                    cost[3] = cost3;
                    cost[4] = cost4;
                    cost[5] = cost5;

                } else {
                    for (int t = nGroups; --t >= 0; ) {
                        cost[t] = 0;
                    }

                    for (int i = gs; i <= ge; i++) {
                        final int icv = sfmap[i];
                        for (int t = nGroups; --t >= 0; ) {
                            cost[t] += len[t][icv] & 0xff;
                        }
                    }
                }

                /*
                 * Find the coding table which is best for this group, and
                 * record its identity in the selector table.
                 */
                int bt = -1;
                for (int t = nGroups, bc = 999999999; --t >= 0; ) {
                    final int cost_t = cost[t];
                    if (cost_t < bc) {
                        bc = cost_t;
                        bt = t;
                    }
                }

                fave[bt]++;
                selector[nSelectors] = (byte)bt;
                nSelectors++;

                /*
                 * Increment the symbol frequencies for the selected table.
                 */
                final int[] rfreq_bt = rfreq[bt];
                for (int i = gs; i <= ge; i++) {
                    rfreq_bt[sfmap[i]]++;
                }

                gs = ge + 1;
            }

            /*
             * Recompute the tables based on the accumulated frequencies.
             */
            for (int t = 0; t < nGroups; t++) {
                hbMakeCodeLengths(len[t], rfreq[t], this.data, alphaSize, 20);
            }
        }

        return nSelectors;
    }

    private void sendMTFValues2(final int nGroups, final int nSelectors) {
        // assert (nGroups < 8) : nGroups;

        final Data dataShadow = this.data;
        final byte[] pos = dataShadow.sendMTFValues2_pos;

        for (int i = nGroups; --i >= 0; ) {
            pos[i] = (byte)i;
        }

        for (int i = 0; i < nSelectors; i++) {
            final byte ll_i = dataShadow.selector[i];
            byte tmp = pos[0];
            int j = 0;

            while (ll_i != tmp) {
                j++;
                final byte tmp2 = tmp;
                tmp = pos[j];
                pos[j] = tmp2;
            }

            pos[0] = tmp;
            dataShadow.selectorMtf[i] = (byte)j;
        }
    }

    private void sendMTFValues3(final int nGroups, final int alphaSize) {
        final int[][] code = this.data.sendMTFValues_code;
        final byte[][] len = this.data.sendMTFValues_len;

        for (int t = 0; t < nGroups; t++) {
            int minLen = 32;
            int maxLen = 0;
            final byte[] len_t = len[t];
            for (int i = alphaSize; --i >= 0; ) {
                final int l = len_t[i] & 0xff;
                if (l > maxLen) {
                    maxLen = l;
                }
                if (l < minLen) {
                    minLen = l;
                }
            }

            // assert (maxLen <= 20) : maxLen;
            // assert (minLen >= 1) : minLen;

            hbAssignCodes(code[t], len[t], minLen, maxLen, alphaSize);
        }
    }

    private void sendMTFValues4() throws IOException {
        final boolean[] inUse = this.data.inUse;
        final boolean[] inUse16 = this.data.sentMTFValues4_inUse16;

        for (int i = 16; --i >= 0; ) {
            inUse16[i] = false;
            final int i16 = i * 16;
            for (int j = 16; --j >= 0; ) {
                if (inUse[i16 + j]) {
                    inUse16[i] = true;
                }
            }
        }

        for (int i = 0; i < 16; i++) {
            bsW(1, inUse16[i] ? 1 : 0);
        }

        int bsLiveShadow = this.bsLive;
        int bsBuffShadow = this.bsBuff;

        for (int i = 0; i < 16; i++) {
            if (inUse16[i]) {
                final int i16 = i * 16;
                for (int j = 0; j < 16; j++) {
                    // inlined: bsW(1, inUse[i16 + j] ? 1 : 0);
                    while (bsLiveShadow >= 8) {
                        out.writeByte(bsBuffShadow >> 24); // write 8-bit
                        bsBuffShadow <<= 8;
                        bsLiveShadow -= 8;
                    }
                    if (inUse[i16 + j]) {
                        bsBuffShadow |= 1 << (32 - bsLiveShadow - 1);
                    }
                    bsLiveShadow++;
                }
            }
        }

        this.bsBuff = bsBuffShadow;
        this.bsLive = bsLiveShadow;
    }

    private void sendMTFValues5(final int nGroups, final int nSelectors)
            throws IOException {
        bsW(3, nGroups);
        bsW(15, nSelectors);

        final byte[] selectorMtf = this.data.selectorMtf;

        int bsLiveShadow = this.bsLive;
        int bsBuffShadow = this.bsBuff;

        for (int i = 0; i < nSelectors; i++) {
            for (int j = 0, hj = selectorMtf[i] & 0xff; j < hj; j++) {
                // inlined: bsW(1, 1);
                while (bsLiveShadow >= 8) {
                    out.writeByte(bsBuffShadow >> 24);
                    bsBuffShadow <<= 8;
                    bsLiveShadow -= 8;
                }
                bsBuffShadow |= 1 << (32 - bsLiveShadow - 1);
                bsLiveShadow++;
            }

            // inlined: bsW(1, 0);
            while (bsLiveShadow >= 8) {
                out.writeByte(bsBuffShadow >> 24);
                bsBuffShadow <<= 8;
                bsLiveShadow -= 8;
            }
            // bsBuffShadow |= 0 << (32 - bsLiveShadow - 1);
            bsLiveShadow++;
        }

        this.bsBuff = bsBuffShadow;
        this.bsLive = bsLiveShadow;
    }

    private void sendMTFValues6(final int nGroups, final int alphaSize)
            throws IOException {
        final byte[][] len = this.data.sendMTFValues_len;

        int bsLiveShadow = this.bsLive;
        int bsBuffShadow = this.bsBuff;

        for (int t = 0; t < nGroups; t++) {
            final byte[] len_t = len[t];
            int curr = len_t[0] & 0xff;

            // inlined: bsW(5, curr);
            while (bsLiveShadow >= 8) {
                out.writeByte(bsBuffShadow >> 24); // write 8-bit
                bsBuffShadow <<= 8;
                bsLiveShadow -= 8;
            }
            bsBuffShadow |= curr << (32 - bsLiveShadow - 5);
            bsLiveShadow += 5;

            for (int i = 0; i < alphaSize; i++) {
                final int lti = len_t[i] & 0xff;
                while (curr < lti) {
                    // inlined: bsW(2, 2);
                    while (bsLiveShadow >= 8) {
                        out.writeByte(bsBuffShadow >> 24); // write 8-bit
                        bsBuffShadow <<= 8;
                        bsLiveShadow -= 8;
                    }
                    bsBuffShadow |= 2 << (32 - bsLiveShadow - 2);
                    bsLiveShadow += 2;

                    curr++; /* 10 */
                }

                while (curr > lti) {
                    // inlined: bsW(2, 3);
                    while (bsLiveShadow >= 8) {
                        out.writeByte(bsBuffShadow >> 24); // write 8-bit
                        bsBuffShadow <<= 8;
                        bsLiveShadow -= 8;
                    }
                    bsBuffShadow |= 3 << (32 - bsLiveShadow - 2);
                    bsLiveShadow += 2;

                    curr--; /* 11 */
                }

                // inlined: bsW(1, 0);
                while (bsLiveShadow >= 8) {
                    out.writeByte(bsBuffShadow >> 24); // write 8-bit
                    bsBuffShadow <<= 8;
                    bsLiveShadow -= 8;
                }
                // bsBuffShadow |= 0 << (32 - bsLiveShadow - 1);
                bsLiveShadow++;
            }
        }

        this.bsBuff = bsBuffShadow;
        this.bsLive = bsLiveShadow;
    }

    private void sendMTFValues7() throws IOException {
        final Data dataShadow = this.data;
        final byte[][] len = dataShadow.sendMTFValues_len;
        final int[][] code = dataShadow.sendMTFValues_code;
        final byte[] selector = dataShadow.selector;
        final char[] sfmap = dataShadow.sfmap;
        final int nMTFShadow = this.nMTF;

        int selCtr = 0;

        int bsLiveShadow = this.bsLive;
        int bsBuffShadow = this.bsBuff;

        for (int gs = 0; gs < nMTFShadow; ) {
            final int ge = Math.min(gs + Constants.G_SIZE - 1, nMTFShadow - 1);
            final int selector_selCtr = selector[selCtr] & 0xff;
            final int[] code_selCtr = code[selector_selCtr];
            final byte[] len_selCtr = len[selector_selCtr];

            while (gs <= ge) {
                final int sfmap_i = sfmap[gs];

                //
                // inlined: bsW(len_selCtr[sfmap_i] & 0xff,
                // code_selCtr[sfmap_i]);
                //
                while (bsLiveShadow >= 8) {
                    out.writeByte(bsBuffShadow >> 24);
                    bsBuffShadow <<= 8;
                    bsLiveShadow -= 8;
                }
                final int n = len_selCtr[sfmap_i] & 0xFF;
                bsBuffShadow |= code_selCtr[sfmap_i] << (32 - bsLiveShadow - n);
                bsLiveShadow += n;

                gs++;
            }

            gs = ge + 1;
            selCtr++;
        }

        this.bsBuff = bsBuffShadow;
        this.bsLive = bsLiveShadow;
    }

    private void moveToFrontCodeAndSend() throws IOException {
        bsW(24, this.data.origPtr);
        generateMTFValues();
        sendMTFValues();
    }

    private void blockSort() {
        blockSorter.blockSort(data, last);
    }

    /*
     * Performs Move-To-Front on the Burrows-Wheeler transformed
     * buffer, storing the MTFed data in data.sfmap in RUNA/RUNB
     * run-length-encoded form.
     *
     * <p>Keeps track of byte frequencies in data.mtfFreq at the same time.</p>
     */
    private void generateMTFValues() {
        final int lastShadow = this.last;
        final Data dataShadow = this.data;
        final boolean[] inUse = dataShadow.inUse;
        final byte[] block = dataShadow.block;
        final int[] fmap = dataShadow.fmap;
        final char[] sfmap = dataShadow.sfmap;
        final int[] mtfFreq = dataShadow.mtfFreq;
        final byte[] unseqToSeq = dataShadow.unseqToSeq;
        final byte[] yy = dataShadow.generateMTFValues_yy;

        // make maps
        int nInUseShadow = 0;
        for (int i = 0; i < 256; i++) {
            if (inUse[i]) {
                unseqToSeq[i] = (byte)nInUseShadow;
                nInUseShadow++;
            }
        }
        this.nInUse = nInUseShadow;

        final int eob = nInUseShadow + 1;

        for (int i = eob; i >= 0; i--) {
            mtfFreq[i] = 0;
        }

        for (int i = nInUseShadow; --i >= 0; ) {
            yy[i] = (byte)i;
        }

        int wr = 0;
        int zPend = 0;

        for (int i = 0; i <= lastShadow; i++) {
            final byte ll_i = unseqToSeq[block[fmap[i]] & 0xff];
            byte tmp = yy[0];
            int j = 0;

            while (ll_i != tmp) {
                j++;
                final byte tmp2 = tmp;
                tmp = yy[j];
                yy[j] = tmp2;
            }
            yy[0] = tmp;

            if (j == 0) {
                zPend++;
            } else {
                if (zPend > 0) {
                    zPend--;
                    while (true) {
                        if ((zPend & 1) == 0) {
                            sfmap[wr] = Constants.RUNA;
                            wr++;
                            mtfFreq[Constants.RUNA]++;
                        } else {
                            sfmap[wr] = Constants.RUNB;
                            wr++;
                            mtfFreq[Constants.RUNB]++;
                        }

                        if (zPend >= 2) {
                            zPend = (zPend - 2) >> 1;
                        } else {
                            break;
                        }
                    }
                    zPend = 0;
                }
                sfmap[wr] = (char)(j + 1);
                wr++;
                mtfFreq[j + 1]++;
            }
        }

        if (zPend > 0) {
            zPend--;
            while (true) {
                if ((zPend & 1) == 0) {
                    sfmap[wr] = Constants.RUNA;
                    wr++;
                    mtfFreq[Constants.RUNA]++;
                } else {
                    sfmap[wr] = Constants.RUNB;
                    wr++;
                    mtfFreq[Constants.RUNB]++;
                }

                if (zPend >= 2) {
                    zPend = (zPend - 2) >> 1;
                } else {
                    break;
                }
            }
        }

        sfmap[wr] = (char)eob;
        mtfFreq[eob]++;
        this.nMTF = wr + 1;
    }

    static final class Data {

        // with blockSize 900k
        /* maps unsigned byte => "does it occur in block" */
        final boolean[] inUse = new boolean[256]; // 256 byte
        final byte[] unseqToSeq = new byte[256]; // 256 byte
        final int[] mtfFreq = new int[Constants.MAX_ALPHA_SIZE]; // 1032 byte
        final byte[] selector = new byte[Constants.MAX_SELECTORS]; // 18002 byte
        final byte[] selectorMtf = new byte[Constants.MAX_SELECTORS]; // 18002 byte

        final byte[] generateMTFValues_yy = new byte[256]; // 256 byte
        final byte[][] sendMTFValues_len = new byte[Constants.N_GROUPS][Constants.MAX_ALPHA_SIZE]; // 1548
        // byte
        final int[][] sendMTFValues_rfreq = new int[Constants.N_GROUPS][Constants.MAX_ALPHA_SIZE]; // 6192
        // byte
        final int[] sendMTFValues_fave = new int[Constants.N_GROUPS]; // 24 byte
        final short[] sendMTFValues_cost = new short[Constants.N_GROUPS]; // 12 byte
        final int[][] sendMTFValues_code = new int[Constants.N_GROUPS][Constants.MAX_ALPHA_SIZE]; // 6192
        // byte
        final byte[] sendMTFValues2_pos = new byte[Constants.N_GROUPS]; // 6 byte
        final boolean[] sentMTFValues4_inUse16 = new boolean[16]; // 16 byte

        final int[] heap = new int[Constants.MAX_ALPHA_SIZE + 2]; // 1040 byte
        final int[] weight = new int[Constants.MAX_ALPHA_SIZE * 2]; // 2064 byte
        final int[] parent = new int[Constants.MAX_ALPHA_SIZE * 2]; // 2064 byte

        // ------------
        // 333408 byte

        /* holds the RLEd block of original data starting at index 1.
         * After sorting the last byte added to the buffer is at index
         * 0. */
        final byte[] block; // 900021 byte
        /* maps index in Burrows-Wheeler transformed block => index of
         * byte in original block */
        final int[] fmap; // 3600000 byte
        final char[] sfmap; // 3600000 byte
        // ------------
        // 8433529 byte
        // ============

        /**
         * Index of original line in Burrows-Wheeler table.
         *
         * <p>This is the index in fmap that points to the last byte
         * of the original data.</p>
         */
        int origPtr;

        Data(final int blockSize100k) {
            final int n = blockSize100k * Constants.BASEBLOCKSIZE;
            this.block = new byte[(n + 1 + Constants.NUM_OVERSHOOT_BYTES)];
            this.fmap = new int[n];
            this.sfmap = new char[2 * n];
        }

    }

}
