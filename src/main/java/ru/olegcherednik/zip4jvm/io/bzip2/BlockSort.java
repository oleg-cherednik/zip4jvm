package ru.olegcherednik.zip4jvm.io.bzip2;

import lombok.RequiredArgsConstructor;

import java.util.BitSet;
import java.util.Deque;
import java.util.LinkedList;

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
class BlockSort {

    private static final int FALLBACK_QSORT_SMALL_THRESH = 10;

    private static final int SMALL_THRESH = 20;
    private static final int DEPTH_THRESH = 10;
    private static final int WORK_FACTOR = 30;

    private static final int SETMASK = 1 << 21;
    private static final int CLEARMASK = ~SETMASK;

    /*
     * Used when sorting. If too many long comparisons happen, we stop sorting,
     * and use fallbackSort instead.
     */
    private int workDone;
    private int workLimit;
    private boolean firstAttempt;

    private final int[] mainSortRunningOrder = new int[256]; // 1024 byte
    private final int[] mainSortCopy = new int[256]; // 1024 byte
    private final boolean[] mainSortBigDone = new boolean[256]; // 256 byte

    private final int[] ftab = new int[65537]; // 262148 byte

    private final Bzip2OutputStream.Data data;

    @RequiredArgsConstructor
    private static final class Data {

        private final int lo;
        private final int hi;
        private final int d;

        public Data(int lo, int hi) {
            this(lo, hi, 0);
        }
    }

    public BlockSort(Bzip2OutputStream.Data data) {
        this.data = data;
    }

    public void blockSort(int last) {
        workLimit = WORK_FACTOR * last;
        workDone = 0;
        firstAttempt = true;

        if (last + 1 < 10000)
            fallbackSort(last);
        else {
            mainSort(data, last);

            if (firstAttempt && (workDone > workLimit))
                fallbackSort(last);
        }

        data.origPtr = -1;

        for (int i = 0; i <= last; i++) {
            if (data.fmap[i] != 0)
                continue;

            data.origPtr = i;
            break;
        }
    }

    private void fallbackSimpleSort(int[] eclass, int lo, int hi) {
        if (lo == hi)
            return;

        int j;

        if (hi - lo > 3) {
            for (int i = hi - 4; i >= lo; i--) {
                int tmp = data.fmap[i];
                int ec_tmp = eclass[tmp];

                for (j = i + 4; j <= hi && ec_tmp > eclass[data.fmap[j]]; j += 4)
                    data.fmap[j - 4] = data.fmap[j];

                data.fmap[j - 4] = tmp;
            }
        }

        for (int i = hi - 1; i >= lo; i--) {
            int tmp = data.fmap[i];
            int ec_tmp = eclass[tmp];

            for (j = i + 1; j <= hi && ec_tmp > eclass[data.fmap[j]]; j++)
                data.fmap[j - 1] = data.fmap[j];

            data.fmap[j - 1] = tmp;
        }
    }

    private static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    private static void swap(int[] arr, int i, int j, int len) {
        while (len-- > 0)
            swap(arr, i++, j++);
    }

    /**
     * @param eclass points from the index of a character inside the
     *               block to the first index in fmap that contains the
     *               bucket of its suffix that is sorted in this step.
     * @param lo     lower boundary of the fmap-interval to be sorted
     * @param hi     upper boundary of the fmap-interval to be sorted
     */
    private void fallbackQSort3(int[] eclass, int lo, int hi) {
        int unLo, ltLo, unHi, gtHi, n;

        long r = 0;

        Deque<Data> stack = new LinkedList<>();
        stack.push(new Data(lo, hi));

        while (!stack.isEmpty()) {
            Data dd = stack.pop();

            if (dd.hi - dd.lo < FALLBACK_QSORT_SMALL_THRESH) {
                fallbackSimpleSort(eclass, dd.lo, dd.hi);
                continue;
            }

            /* LBZ2: Random partitioning.  Median of 3 sometimes fails to
               avoid bad cases.  Median of 9 seems to help but
               looks rather expensive.  This too seems to work but
               is cheaper.  Guidance for the magic constants
               7621 and 32768 is taken from Sedgewick's algorithms
               book, chapter 35.
            */
            r = ((r * 7621) + 1) % 32768;
            final long r3 = r % 3;
            long med;

            if (r3 == 0)
                med = eclass[data.fmap[dd.lo]];
            else if (r3 == 1)
                med = eclass[data.fmap[(dd.lo + dd.hi) >>> 1]];
            else
                med = eclass[data.fmap[dd.hi]];

            unLo = ltLo = dd.lo;
            unHi = gtHi = dd.hi;

            // looks like the ternary partition attributed to Wegner
            // in the cited Sedgewick paper
            while (true) {
                while (true) {
                    if (unLo > unHi) {
                        break;
                    }
                    n = eclass[data.fmap[unLo]] - (int)med;
                    if (n == 0) {
                        swap(data.fmap, unLo, ltLo);
                        ltLo++;
                        unLo++;
                        continue;
                    }
                    if (n > 0) {
                        break;
                    }
                    unLo++;
                }
                while (true) {
                    if (unLo > unHi) {
                        break;
                    }
                    n = eclass[data.fmap[unHi]] - (int)med;
                    if (n == 0) {
                        swap(data.fmap, unHi, gtHi);
                        gtHi--;
                        unHi--;
                        continue;
                    }
                    if (n < 0) {
                        break;
                    }
                    unHi--;
                }
                if (unLo > unHi) {
                    break;
                }
                swap(data.fmap, unLo, unHi);
                unLo++;
                unHi--;
            }

            if (gtHi < ltLo) {
                continue;
            }

            n = Math.min(ltLo - dd.lo, unLo - ltLo);
            swap(data.fmap, dd.lo, unLo - n, n);
            int m = Math.min(dd.hi - gtHi, gtHi - unHi);
            swap(data.fmap, unHi + 1, dd.hi - m + 1, m);

            n = dd.lo + unLo - ltLo - 1;
            m = dd.hi - (gtHi - unHi) + 1;

            if (n - dd.lo > dd.hi - m) {
                stack.push(new Data(dd.lo, n));
                stack.push(new Data(m, dd.hi));
            } else {
                stack.push(new Data(m, dd.hi));
                stack.push(new Data(dd.lo, n));
            }
        }
    }

    private int[] eclass;

    private int[] getEclass() {
        if (eclass == null) {
            eclass = new int[data.sfmap.length / 2];
        }
        return eclass;
    }

    /**
     * Adapt fallbackSort to the expected interface of the rest of the
     * code, in particular deal with the fact that block starts at
     * offset 1 (in libbzip2 1.0.6 it starts at 0).
     */
    private void fallbackSort(int last) {
        data.block[0] = data.block[last + 1];
        fallbackSortImpl(last + 1);

        for (int i = 0; i < last + 1; i++)
            --data.fmap[i];

        for (int i = 0; i < last + 1; i++) {
            if (data.fmap[i] != -1)
                continue;

            data.fmap[i] = last;
            break;
        }
    }

    /*
     * The C code uses an array of ints (each int holding 32 flags) to
     * represents the bucket-start flags (bhtab).  It also contains
     * optimizations to skip over 32 consecutively set or
     * consecutively unset bits on word boundaries at once.  For now
     * I've chosen to use the simpler but potentially slower code
     * using BitSet - also in the hope that using the BitSet#nextXXX
     * methods may be fast enough.
     */

    final void fallbackSortImpl(int nblock) {
        final int[] ftab = new int[257];
        int H, i, j, k, lo, hi, cc, cc1;
        int nNotDone;
        int nBhtab;
        final int[] eclass = getEclass();

        for (i = 0; i < nblock; i++) {
            eclass[i] = 0;
        }
        /*--
          LBZ2: Initial 1-char radix sort to generate
          initial fmap and initial BH bits.
          --*/
        for (i = 0; i < nblock; i++) {
            ftab[data.block[i] & 0xff]++;
        }
        for (i = 1; i < 257; i++) {
            ftab[i] += ftab[i - 1];
        }

        for (i = 0; i < nblock; i++) {
            j = data.block[i] & 0xff;
            k = ftab[j] - 1;
            ftab[j] = k;
            data.fmap[k] = i;
        }

        nBhtab = 64 + nblock;
        final BitSet bhtab = new BitSet(nBhtab);
        for (i = 0; i < 256; i++) {
            bhtab.set(ftab[i]);
        }

        /*--
          LBZ2: Inductively refine the buckets.  Kind-of an
          "exponential radix sort" (!), inspired by the
          Manber-Myers suffix array construction algorithm.
          --*/

        /*-- LBZ2: set sentinel bits for block-end detection --*/
        for (i = 0; i < 32; i++) {
            bhtab.set(nblock + 2 * i);
            bhtab.clear(nblock + 2 * i + 1);
        }

        /*-- LBZ2: the log(N) loop --*/
        H = 1;
        while (true) {

            j = 0;
            for (i = 0; i < nblock; i++) {
                if (bhtab.get(i)) {
                    j = i;
                }
                k = data.fmap[i] - H;
                if (k < 0) {
                    k += nblock;
                }
                eclass[k] = j;
            }

            nNotDone = 0;
            hi = -1;
            while (true) {

                /*-- LBZ2: find the next non-singleton bucket --*/
                k = hi + 1;
                k = bhtab.nextClearBit(k);
                lo = k - 1;
                if (lo >= nblock) {
                    break;
                }
                k = bhtab.nextSetBit(k + 1);
                hi = k - 1;
                if (hi >= nblock) {
                    break;
                }

                /*-- LBZ2: now [lo, hi] bracket current bucket --*/
                if (hi > lo) {
                    nNotDone += hi - lo + 1;
                    fallbackQSort3(eclass, lo, hi);

                    /*-- LBZ2: scan bucket and generate header bits-- */
                    cc = -1;
                    for (i = lo; i <= hi; i++) {
                        cc1 = eclass[data.fmap[i]];
                        if (cc != cc1) {
                            bhtab.set(i);
                            cc = cc1;
                        }
                    }
                }
            }

            H *= 2;
            if (H > nblock || nNotDone == 0) {
                break;
            }
        }
    }

    private static final int[] INCS = { 1, 4, 13, 40, 121, 364, 1093, 3280, 9841, 29524, 88573, 265720, 797161, 2391484 };

    /**
     * This is the most hammered method of this class.
     *
     * <p>
     * This is the version using unrolled loops. Normally I never use such ones
     * in Java code. The unrolling has shown a noticable performance improvement
     * on JRE 1.4.2 (Linux i586 / HotSpot Client). Of course it depends on the
     * JIT compiler of the vm.
     * </p>
     */
    private boolean mainSimpleSort(final int lo, final int hi, final int d, final int lastShadow) {
        final int bigN = hi - lo + 1;

        if (bigN < 2)
            return firstAttempt && (workDone > workLimit);

        int hp = 0;
        while (INCS[hp] < bigN) {
            hp++;
        }

        final int lastPlus1 = lastShadow + 1;
        final boolean firstAttemptShadow = firstAttempt;
        final int workLimitShadow = workLimit;
        int workDoneShadow = workDone;

        // Following block contains unrolled code which could be shortened by
        // coding it in additional loops.

        HP:
        while (--hp >= 0) {
            final int h = INCS[hp];
            final int mj = lo + h - 1;

            for (int i = lo + h; i <= hi; ) {
                // copy
                for (int k = 3; (i <= hi) && (--k >= 0); i++) {
                    final int v = data.fmap[i];
                    final int vd = v + d;
                    int j = i;

                    // for (int a;
                    // (j > mj) && mainGtU((a = fmap[j - h]) + d, vd,
                    // block, quadrant, lastShadow);
                    // j -= h) {
                    // fmap[j] = a;
                    // }
                    //
                    // unrolled version:

                    // start inline mainGTU
                    boolean onceRunned = false;
                    int a = 0;

                    HAMMER:
                    while (true) {
                        if (onceRunned) {
                            data.fmap[j] = a;
                            if ((j -= h) <= mj) { //NOSONAR
                                break HAMMER;
                            }
                        } else {
                            onceRunned = true;
                        }

                        a = data.fmap[j - h];
                        int i1 = a + d;
                        int i2 = vd;

                        // following could be done in a loop, but
                        // unrolled it for performance:
                        if (data.block[i1 + 1] == data.block[i2 + 1]) {
                            if (data.block[i1 + 2] == data.block[i2 + 2]) {
                                if (data.block[i1 + 3] == data.block[i2 + 3]) {
                                    if (data.block[i1 + 4] == data.block[i2 + 4]) {
                                        if (data.block[i1 + 5] == data.block[i2 + 5]) {
                                            if (data.block[i1 += 6] == data.block[i2 += 6]) { //NOSONAR
                                                int x = lastShadow;
                                                X:
                                                while (x > 0) {
                                                    x -= 4;

                                                    if (data.block[i1 + 1] == data.block[i2 + 1]) {
                                                        if (this.data.sfmap[i1] == this.data.sfmap[i2]) {
                                                            if (data.block[i1 + 2] == data.block[i2 + 2]) {
                                                                if (this.data.sfmap[i1 + 1] == this.data.sfmap[i2 + 1]) {
                                                                    if (data.block[i1 + 3] == data.block[i2 + 3]) {
                                                                        if (this.data.sfmap[i1 + 2] == this.data.sfmap[i2 + 2]) {
                                                                            if (data.block[i1 + 4] == data.block[i2 + 4]) {
                                                                                if (this.data.sfmap[i1 + 3] == this.data.sfmap[i2 + 3]) {
                                                                                    if ((i1 += 4) >= lastPlus1) { //NOSONAR
                                                                                        i1 -= lastPlus1;
                                                                                    }
                                                                                    if ((i2 += 4) >= lastPlus1) { //NOSONAR
                                                                                        i2 -= lastPlus1;
                                                                                    }
                                                                                    workDoneShadow++;
                                                                                    continue X;
                                                                                } else if (this.data.sfmap[i1 + 3] > this.data.sfmap[i2 + 3]) {
                                                                                    continue HAMMER;
                                                                                } else {
                                                                                    break HAMMER;
                                                                                }
                                                                            } else if ((data.block[i1 + 4] & 0xff) > (data.block[i2 + 4] & 0xff)) {
                                                                                continue HAMMER;
                                                                            } else {
                                                                                break HAMMER;
                                                                            }
                                                                        } else if (this.data.sfmap[i1 + 2] > this.data.sfmap[i2 + 2]) {
                                                                            continue HAMMER;
                                                                        } else {
                                                                            break HAMMER;
                                                                        }
                                                                    } else if ((data.block[i1 + 3] & 0xff) > (data.block[i2 + 3] & 0xff)) {
                                                                        continue HAMMER;
                                                                    } else {
                                                                        break HAMMER;
                                                                    }
                                                                } else if (this.data.sfmap[i1 + 1] > this.data.sfmap[i2 + 1]) {
                                                                    continue HAMMER;
                                                                } else {
                                                                    break HAMMER;
                                                                }
                                                            } else if ((data.block[i1 + 2] & 0xff) > (data.block[i2 + 2] & 0xff)) {
                                                                continue HAMMER;
                                                            } else {
                                                                break HAMMER;
                                                            }
                                                        } else if (this.data.sfmap[i1] > this.data.sfmap[i2]) {
                                                            continue HAMMER;
                                                        } else {
                                                            break HAMMER;
                                                        }
                                                    } else if ((data.block[i1 + 1] & 0xff) > (data.block[i2 + 1] & 0xff)) {
                                                        continue HAMMER;
                                                    } else {
                                                        break HAMMER;
                                                    }

                                                }
                                                break HAMMER;
                                            } // while x > 0
                                            if ((data.block[i1] & 0xff) > (data.block[i2] & 0xff)) {
                                                continue HAMMER;
                                            }
                                            break HAMMER;
                                        } else if ((data.block[i1 + 5] & 0xff) > (data.block[i2 + 5] & 0xff)) {
                                            continue HAMMER;
                                        } else {
                                            break HAMMER;
                                        }
                                    } else if ((data.block[i1 + 4] & 0xff) > (data.block[i2 + 4] & 0xff)) {
                                        continue HAMMER;
                                    } else {
                                        break HAMMER;
                                    }
                                } else if ((data.block[i1 + 3] & 0xff) > (data.block[i2 + 3] & 0xff)) {
                                    continue HAMMER;
                                } else {
                                    break HAMMER;
                                }
                            } else if ((data.block[i1 + 2] & 0xff) > (data.block[i2 + 2] & 0xff)) {
                                continue HAMMER;
                            } else {
                                break HAMMER;
                            }
                        } else if ((data.block[i1 + 1] & 0xff) > (data.block[i2 + 1] & 0xff)) {
                            continue HAMMER;
                        } else {
                            break HAMMER;
                        }

                    } // HAMMER
                    // end inline mainGTU

                    data.fmap[j] = v;
                }

                if (firstAttemptShadow && (i <= hi)
                        && (workDoneShadow > workLimitShadow)) {
                    break HP;
                }
            }
        }

        workDone = workDoneShadow;
        return firstAttemptShadow && (workDoneShadow > workLimitShadow);
    }

/*--
   LBZ2: The following is an implementation of
   an elegant 3-way quicksort for strings,
   described in a paper "Fast Algorithms for
   Sorting and Searching Strings", by Robert
   Sedgewick and Jon L. Bentley.
--*/

    private static void vswap(final int[] fmap, int p1, int p2, int n) {
        n += p1;
        while (p1 < n) {
            final int t = fmap[p1];
            fmap[p1++] = fmap[p2];
            fmap[p2++] = t;
        }
    }

    private static byte med3(final byte a, final byte b, final byte c) {
        return (a < b) ? (b < c ? b : a < c ? c : a) : (b > c ? b : a > c ? c : a);
    }

    private void mainQSort3(final int loSt, final int hiSt, final int dSt, final int last) {
        Deque<Data> stack = new LinkedList<>();
        stack.push(new Data(loSt, hiSt, dSt));

        while (!stack.isEmpty()) {
            Data dd = stack.pop();

            if ((dd.hi - dd.lo < SMALL_THRESH) || (dd.d > DEPTH_THRESH)) {
                if (mainSimpleSort(dd.lo, dd.hi, dd.d, last))
                    return;
            } else {
                final int d1 = dd.d + 1;
                final int med = med3(data.block[data.fmap[dd.lo] + d1], data.block[data.fmap[dd.hi] + d1],
                        data.block[data.fmap[(dd.lo + dd.hi) >>> 1] + d1]) & 0xff;

                int unLo = dd.lo;
                int unHi = dd.hi;
                int ltLo = dd.lo;
                int gtHi = dd.hi;

                while (true) {
                    while (unLo <= unHi) {
                        final int n = (data.block[data.fmap[unLo] + d1] & 0xff) - med;
                        if (n == 0) {
                            final int temp = data.fmap[unLo];
                            data.fmap[unLo++] = data.fmap[ltLo];
                            data.fmap[ltLo++] = temp;
                        } else if (n < 0) {
                            unLo++;
                        } else {
                            break;
                        }
                    }

                    while (unLo <= unHi) {
                        final int n = (data.block[data.fmap[unHi] + d1] & 0xff) - med;
                        if (n == 0) {
                            final int temp = data.fmap[unHi];
                            data.fmap[unHi--] = data.fmap[gtHi];
                            data.fmap[gtHi--] = temp;
                        } else if (n > 0) {
                            unHi--;
                        } else {
                            break;
                        }
                    }

                    if (unLo <= unHi) {
                        final int temp = data.fmap[unLo];
                        data.fmap[unLo++] = data.fmap[unHi];
                        data.fmap[unHi--] = temp;
                    } else {
                        break;
                    }
                }

                if (gtHi < ltLo)
                    stack.push(new Data(dd.lo, dd.hi, d1));
                else {
                    int n = ((ltLo - dd.lo) < (unLo - ltLo)) ? (ltLo - dd.lo) : (unLo - ltLo);
                    vswap(data.fmap, dd.lo, unLo - n, n);
                    int m = ((dd.hi - gtHi) < (gtHi - unHi)) ? (dd.hi - gtHi) : (gtHi - unHi);
                    vswap(data.fmap, unLo, dd.hi - m + 1, m);

                    n = dd.lo + unLo - ltLo - 1;
                    m = dd.hi - (gtHi - unHi) + 1;

                    stack.push(new Data(dd.lo, n, dd.d));
                    stack.push(new Data(n + 1, m - 1, d1));
                    stack.push(new Data(m, dd.hi, dd.d));
                }
            }
        }
    }

    private final void mainSort(final Bzip2OutputStream.Data data, final int lastShadow) {
        final int[] runningOrder = mainSortRunningOrder;
        final int[] copy = mainSortCopy;
        final boolean[] bigDone = mainSortBigDone;
        final int[] ftab = this.ftab;
        final int workLimitShadow = workLimit;
        final boolean firstAttemptShadow = firstAttempt;

        // LBZ2: Set up the 2-byte frequency table
        for (int i = 65537; --i >= 0; ) {
            ftab[i] = 0;
        }

        /*
         * In the various block-sized structures, live data runs from 0 to
         * last+NUM_OVERSHOOT_BYTES inclusive. First, set up the overshoot area
         * for block.
         */
        for (int i = 0; i < Constants.NUM_OVERSHOOT_BYTES; i++) {
            data.block[lastShadow + i + 2] = data.block[(i % (lastShadow + 1)) + 1];
        }
        for (int i = lastShadow + Constants.NUM_OVERSHOOT_BYTES + 1; --i >= 0; ) {
            this.data.sfmap[i] = 0;
        }
        data.block[0] = data.block[lastShadow + 1];

        // LBZ2: Complete the initial radix sort:

        int c1 = data.block[0] & 0xff;
        for (int i = 0; i <= lastShadow; i++) {
            final int c2 = data.block[i + 1] & 0xff;
            ftab[(c1 << 8) + c2]++;
            c1 = c2;
        }

        for (int i = 1; i <= 65536; i++) {
            ftab[i] += ftab[i - 1];
        }

        c1 = data.block[1] & 0xff;
        for (int i = 0; i < lastShadow; i++) {
            final int c2 = data.block[i + 2] & 0xff;
            data.fmap[--ftab[(c1 << 8) + c2]] = i;
            c1 = c2;
        }

        data.fmap[--ftab[((data.block[lastShadow + 1] & 0xff) << 8) + (data.block[1] & 0xff)]] = lastShadow;

        /*
         * LBZ2: Now ftab contains the first loc of every small bucket. Calculate the
         * running order, from smallest to largest big bucket.
         */
        for (int i = 256; --i >= 0; ) {
            bigDone[i] = false;
            runningOrder[i] = i;
        }

        // h = 364, 121, 40, 13, 4, 1
        for (int h = 364; h != 1; ) { //NOSONAR
            h /= 3;
            for (int i = h; i <= 255; i++) {
                final int vv = runningOrder[i];
                final int a = ftab[(vv + 1) << 8] - ftab[vv << 8];
                final int b = h - 1;
                int j = i;
                for (int ro = runningOrder[j - h]; (ftab[(ro + 1) << 8] - ftab[ro << 8]) > a; ro = runningOrder[j
                        - h]) {
                    runningOrder[j] = ro;
                    j -= h;
                    if (j <= b) {
                        break;
                    }
                }
                runningOrder[j] = vv;
            }
        }

        /*
         * LBZ2: The main sorting loop.
         */
        for (int i = 0; i <= 255; i++) {
            /*
             * LBZ2: Process big buckets, starting with the least full.
             */
            final int ss = runningOrder[i];

            // Step 1:
            /*
             * LBZ2: Complete the big bucket [ss] by quicksorting any unsorted small
             * buckets [ss, j]. Hopefully previous pointer-scanning phases have
             * already completed many of the small buckets [ss, j], so we don't
             * have to sort them at all.
             */
            for (int j = 0; j <= 255; j++) {
                final int sb = (ss << 8) + j;
                final int ftab_sb = ftab[sb];
                if ((ftab_sb & SETMASK) != SETMASK) {
                    final int lo = ftab_sb & CLEARMASK;
                    final int hi = (ftab[sb + 1] & CLEARMASK) - 1;
                    if (hi > lo) {
                        mainQSort3(lo, hi, 2, lastShadow);
                        if (firstAttemptShadow && (workDone > workLimitShadow)) {
                            return;
                        }
                    }
                    ftab[sb] = ftab_sb | SETMASK;
                }
            }

            // Step 2:
            // LBZ2: Now scan this big bucket so as to synthesise the
            // sorted order for small buckets [t, ss] for all t != ss.

            for (int j = 0; j <= 255; j++) {
                copy[j] = ftab[(j << 8) + ss] & CLEARMASK;
            }

            for (int j = ftab[ss << 8] & CLEARMASK, hj = (ftab[(ss + 1) << 8] & CLEARMASK); j < hj; j++) {
                final int fmap_j = data.fmap[j];
                c1 = data.block[fmap_j] & 0xff;
                if (!bigDone[c1]) {
                    data.fmap[copy[c1]] = (fmap_j == 0) ? lastShadow : (fmap_j - 1);
                    copy[c1]++;
                }
            }

            for (int j = 256; --j >= 0; ) {
                ftab[(j << 8) + ss] |= SETMASK;
            }

            // Step 3:
            /*
             * LBZ2: The ss big bucket is now done. Record this fact, and update the
             * quadrant descriptors. Remember to update quadrants in the
             * overshoot area too, if necessary. The "if (i < 255)" test merely
             * skips this updating for the last bucket processed, since updating
             * for the last bucket is pointless.
             */
            bigDone[ss] = true;

            if (i < 255) {
                final int bbStart = ftab[ss << 8] & CLEARMASK;
                final int bbSize = (ftab[(ss + 1) << 8] & CLEARMASK) - bbStart;
                int shifts = 0;

                while ((bbSize >> shifts) > 65534) {
                    shifts++;
                }

                for (int j = 0; j < bbSize; j++) {
                    final int a2update = data.fmap[bbStart + j];
                    final char qVal = (char)(j >> shifts);
                    this.data.sfmap[a2update] = qVal;
                    if (a2update < Constants.NUM_OVERSHOOT_BYTES) {
                        this.data.sfmap[a2update + lastShadow + 1] = qVal;
                    }
                }
            }

        }
    }

}
