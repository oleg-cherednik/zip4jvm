/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.io.out.compressed.deflate64;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Minimal streaming Deflate64 (a.k.a. "Enhanced Deflate", PKWARE method 9) encoder.
 * <p>
 * Bytes are pushed in one at a time and accumulated in a fixed size buffer. As
 * soon as {@value #BATCH_SIZE} bytes are pending, they are compressed and emitted
 * as one deflate block; the buffer, then slides, keeping the last
 * {@value #WINDOW_SIZE} bytes as history so that matches may still point back
 * into data emitted by earlier blocks. Memory use is therefore constant and
 * independent of the entry size, mirroring the way {@code DeflateEntryDataOutput}
 * reuses a small buffer around {@link java.util.zip.Deflater}.
 * <p>
 * The encoder is deliberately simple: a greedy LZ77 match finder (hash chains)
 * with <b>fixed Huffman</b> and <b>stored</b> blocks only (no dynamic Huffman).
 * It is faithful to the Deflate64 bitstream defined by 7-Zip's
 * {@code DeflateConst.h}:
 * <ul>
 *   <li>a 64 KB sliding window (match distances up to {@value #WINDOW_SIZE}),
 *       which is the essential difference from classic 32 KB Deflate and is
 *       expressed through distance codes 30 and 31;</li>
 *   <li>match length is capped at {@value #MAX_MATCH} so that only length codes
 *       257..284 are used - their meaning is identical in Deflate and Deflate64,
 *       which avoids the length-code-285 ambiguity between the two formats.</li>
 * </ul>
 * Compressed bytes are written straight into the {@link OutputStream} handed to
 * the constructor; the stream is never flushed or closed here.
 *
 * @author Oleg Cherednik
 * @since 26.07.2026
 */
public class Deflate64Compressor {

    private static final int WINDOW_SIZE = 1 << 16;         // 65536, Deflate64 history

    private static final int MIN_MATCH = 3;
    private static final int MAX_MATCH = 257;       // keeps us within length codes 257..284
    /** Pending bytes that trigger emission of the next block. */
    private static final int BATCH_SIZE = 1 << 15;  // 32768, keeps a block below the 65535 stored limit
    private static final int BUF_SIZE = WINDOW_SIZE + BATCH_SIZE + MAX_MATCH;

    private static final int HASH_BITS = 16;
    private static final int HASH_SIZE = 1 << HASH_BITS;
    private static final int HASH_MASK = HASH_SIZE - 1;

    // ---------- RFC 1951 length codes for lengths 3..257 (codes 257..284) ----------
    private static final int[] LEN_BASE = {
            3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31, 35, 43, 51, 59,
            67, 83, 99, 115, 131, 163, 195, 227 };
    private static final int[] LEN_EXTRA = {
            0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3,
            4, 4, 4, 4, 5, 5, 5, 5 };

    // ---------- RFC 1951 distance codes 0..31 (30/31 are the Deflate64 extension) ----------
    private static final int[] DIST_BASE = {
            1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193, 257, 385, 513, 769,
            1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577, 32769, 49153 };
    private static final int[] DIST_EXTRA = {
            0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8,
            9, 9, 10, 10, 11, 11, 12, 12, 13, 13, 14, 14 };

    // ---------- fixed Huffman literal/length codes (RFC 1951 3.2.6), MSB-first ----------
    private static final int[] FIXED_CODE = new int[288];
    private static final int[] FIXED_LEN = new int[288];

    static {
        int i = 0;
        for (; i < 144; i++) {
            FIXED_CODE[i] = 0x30 + i;
            FIXED_LEN[i] = 8;
        }
        for (; i < 256; i++) {
            FIXED_CODE[i] = 0x190 + (i - 144);
            FIXED_LEN[i] = 9;
        }
        for (; i < 280; i++) {
            FIXED_CODE[i] = i - 256;
            FIXED_LEN[i] = 7;
        }
        for (; i < 288; i++) {
            FIXED_CODE[i] = 0xC0 + (i - 280);
            FIXED_LEN[i] = 8;
        }
    }

    private final int maxChain;
    private final BitWriter bw;

    /**
     * Sliding buffer: {@code [0, start)} is history, {@code [start, end)}
     * is pending.
     */
    private final byte[] buf = new byte[BUF_SIZE];
    private final int[] head = new int[HASH_SIZE];
    private final int[] prev = new int[BUF_SIZE];
    /** Reused per block, so no allocation happens while streaming. */
    private final int[] tokens = new int[BATCH_SIZE + MAX_MATCH + 1];

    private int start;
    private int end;
    private boolean finished;

    public Deflate64Compressor(OutputStream out, int maxChain) {
        this.maxChain = maxChain;
        bw = new BitWriter(out);
        Arrays.fill(head, -1);
    }

    // ---------- input ----------

    public void write(int b) throws IOException {
        if (end == buf.length) {
            if (start <= WINDOW_SIZE)
                emitBlock(false);
            slide();
        }

        buf[end++] = (byte) b;

        if (end - start >= BATCH_SIZE)
            emitBlock(false);
    }

    /** Emits the final block and pads the last byte. Idempotent. */
    public void finish() throws IOException {
        if (finished)
            return;

        finished = true;
        emitBlock(true);
        bw.align();
    }

    // ---------- sliding window ----------

    /**
     * Drops history older than {@value #WINDOW_SIZE} bytes, moving the live part of
     * the buffer to the front and rebasing the hash chains accordingly.
     */
    private void slide() {
        int shift = start - WINDOW_SIZE;

        if (shift <= 0)
            return;

        int live = end - shift;
        System.arraycopy(buf, shift, buf, 0, live);
        System.arraycopy(prev, shift, prev, 0, live);

        for (int i = 0; i < live; i++)
            prev[i] = prev[i] >= shift ? prev[i] - shift : -1;

        for (int i = 0; i < head.length; i++)
            head[i] = head[i] >= shift ? head[i] - shift : -1;

        start -= shift;
        end -= shift;
    }

    // ---------- LZ77 + block emission ----------

    /**
     * Compresses everything pending and writes it as a single deflate block.
     * When {@code last} is {@code false} the tail of the buffer is held back by
     * {@value #MAX_MATCH} bytes so that a match starting there can still grow once
     * more input arrives.
     */
    @SuppressWarnings("PMD.CognitiveComplexity")
    private void emitBlock(boolean last) throws IOException {
        int limit = last ? end : Math.max(start, end - MAX_MATCH);
        int blockStart = start;
        int count = 0;
        int i = start;

        while (i < limit) {
            int bestLen = 0;
            int bestDist = 0;
            int maxLen = Math.min(MAX_MATCH, end - i);

            if (maxLen >= MIN_MATCH) {
                int h = hash(i);
                int j = head[h];
                int chain = 0;

                while (j >= 0 && (i - j) <= WINDOW_SIZE && chain < maxChain) {
                    int len = 0;
                    while (len < maxLen && buf[j + len] == buf[i + len])
                        len++;
                    if (len > bestLen) {
                        bestLen = len;
                        bestDist = i - j;
                        if (len >= maxLen)
                            break;
                    }
                    j = prev[j];
                    chain++;
                }
            }

            if (bestLen >= MIN_MATCH) {
                tokens[count++] = (1 << 30) | (bestLen << 17) | bestDist;
                int stop = i + bestLen;
                while (i < stop) {
                    insert(i);
                    i++;
                }
            } else {
                tokens[count++] = buf[i] & 0xFF;
                insert(i);
                i++;
            }
        }

        start = i;
        writeBlock(tokens, count, blockStart, start - blockStart, last);
    }

    private void insert(int i) {
        if (i + MIN_MATCH > end)
            return;

        int h = hash(i);
        prev[i] = head[h];
        head[h] = i;
    }

    private int hash(int i) {
        int v = ((buf[i] & 0xFF) << 16) | ((buf[i + 1] & 0xFF) << 8) | (buf[i + 2] & 0xFF);
        return (v * 0x9E3779B1) >>> (32 - HASH_BITS) & HASH_MASK;
    }

    private void writeBlock(int[] tokens, int count, int blockStart, int coverage, boolean last)
            throws IOException {
        long fixedBits = fixedCostBits(tokens, count);
        long storedBits = 3L + 8 + 32 + (long) coverage * 8; // header + align + LEN/NLEN + payload

        bw.writeBits(last ? 1 : 0, 1);

        if (storedBits < fixedBits) {
            bw.writeBits(0, 2);                 // BTYPE = 00 (stored)
            bw.align();
            bw.writeBits(coverage & 0xFFFF, 16);
            bw.writeBits(~coverage & 0xFFFF, 16);
            for (int k = 0; k < coverage; k++)
                bw.writeByte(buf[blockStart + k] & 0xFF);
        } else {
            bw.writeBits(1, 2);                 // BTYPE = 01 (fixed Huffman)
            for (int t = 0; t < count; t++)
                writeToken(tokens[t]);
            writeSymbol(256);                   // end of block
        }
    }

    private void writeToken(int token) throws IOException {
        if (!isMatch(token)) {
            writeSymbol(token & 0xFF);
            return;
        }

        int length = tokenLength(token);
        int lenIdx = lengthIndex(length);
        writeSymbol(257 + lenIdx);
        if (LEN_EXTRA[lenIdx] > 0)
            bw.writeBits(length - LEN_BASE[lenIdx], LEN_EXTRA[lenIdx]);

        int distance = tokenDistance(token);
        int distIdx = distanceIndex(distance);
        bw.writeHuff(distIdx, 5);               // fixed distance codes are 5 bits, value == code
        if (DIST_EXTRA[distIdx] > 0)
            bw.writeBits(distance - DIST_BASE[distIdx], DIST_EXTRA[distIdx]);
    }

    private void writeSymbol(int symbol) throws IOException {
        bw.writeHuff(FIXED_CODE[symbol], FIXED_LEN[symbol]);
    }

    // ---------- token helpers ----------

    private static boolean isMatch(int token) {
        return (token & (1 << 30)) != 0;
    }

    private static int tokenLength(int token) {
        return (token >>> 17) & 0x1FF;
    }

    private static int tokenDistance(int token) {
        return token & 0x1FFFF;
    }

    private static long fixedCostBits(int[] tokens, int count) {
        long bits = 0;

        for (int t = 0; t < count; t++) {
            int token = tokens[t];
            if (!isMatch(token))
                bits += FIXED_LEN[token & 0xFF];
            else {
                int lenIdx = lengthIndex(tokenLength(token));
                int distIdx = distanceIndex(tokenDistance(token));
                bits += FIXED_LEN[257 + lenIdx] + LEN_EXTRA[lenIdx] + 5 + DIST_EXTRA[distIdx];
            }
        }

        return bits + FIXED_LEN[256];
    }

    private static int lengthIndex(int length) {
        for (int i = LEN_BASE.length - 1; i >= 0; i--)
            if (length >= LEN_BASE[i])
                return i;
        throw new IllegalArgumentException("length: " + length);
    }

    private static int distanceIndex(int distance) {
        for (int i = DIST_BASE.length - 1; i >= 0; i--)
            if (distance >= DIST_BASE[i])
                return i;
        throw new IllegalArgumentException("distance: " + distance);
    }

    // ---------- bit writer (least significant bit first) ----------

    @RequiredArgsConstructor
    private static final class BitWriter {

        private final OutputStream out;
        private int cur;
        private int n;

        /**
         * Writes {@code numBits} of {@code value}, least significant bit
         * first.
         */
        public void writeBits(int value, int numBits) throws IOException {
            for (int i = 0; i < numBits; i++) {
                pushBit(value & 1);
                value >>>= 1;
            }
        }

        /**
         * Writes a canonical Huffman {@code code} of {@code numBits},
         * most significant bit first.
         */
        public void writeHuff(int code, int numBits) throws IOException {
            for (int i = numBits - 1; i >= 0; i--)
                pushBit((code >>> i) & 1);
        }

        /**
         * Only legal when byte aligned, i.e. right after {@link #align()}.
         */
        public void writeByte(int b) throws IOException {
            out.write(b & 0xFF);
        }

        /** Flushes a partially filled byte, padding the high bits with zeroes. */
        public void align() throws IOException {
            if (n > 0) {
                out.write(cur);
                cur = 0;
                n = 0;
            }
        }

        @SuppressWarnings({ "PMD.AssignmentInOperand", "PMD.AvoidLiteralsInIfCondition" })
        private void pushBit(int bit) throws IOException {
            cur |= bit << n;
            if (++n == 8) {
                out.write(cur);
                cur = 0;
                n = 0;
            }
        }
    }

}
