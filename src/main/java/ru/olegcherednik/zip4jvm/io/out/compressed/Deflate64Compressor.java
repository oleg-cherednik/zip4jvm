/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
package ru.olegcherednik.zip4jvm.io.out.compressed;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal Deflate64 (a.k.a. "Enhanced Deflate", PKWARE method 9) encoder.
 * <p>
 * This is a baseline implementation: a greedy LZ77 match finder (hash chains)
 * combined with <b>fixed Huffman</b> and <b>stored</b> blocks only (no dynamic
 * Huffman). It is faithful to the Deflate64 bitstream format defined by 7-Zip's
 * {@code DeflateConst.h}:
 * <ul>
 *   <li>a 64 KB sliding window (match distances up to {@value #WINDOW_SIZE}),
 *       which is the essential difference from classic 32 KB Deflate and is
 *       expressed through distance codes 30 and 31;</li>
 *   <li>match length is capped at {@value #MAX_MATCH} so that only length codes
 *       257..284 are used - their meaning is identical in Deflate and Deflate64,
 *       which avoids the length-code-285 ambiguity between the two formats.</li>
 * </ul>
 * The produced stream is decodable by any conformant Deflate64 inflater,
 * including Apache Commons Compress {@code Deflate64CompressorInputStream} used
 * on the read side of zip4jvm.
 * <p>
 * The whole entry is buffered in memory and compressed in one shot. A streaming
 * variant and dynamic Huffman blocks are possible future improvements.
 *
 * @author Oleg Cherednik
 * @since 26.07.2026
 */
final class Deflate64Compressor {

    static final int WINDOW_SIZE = 1 << 16; // 65536, Deflate64 history
    private static final int MIN_MATCH = 3;
    private static final int MAX_MATCH = 257; // keeps us within length codes 257..284
    private static final int MAX_STORED_BLOCK = (1 << 16) - 1; // 65535

    private static final int HASH_BITS = 16;
    private static final int HASH_SIZE = 1 << HASH_BITS;
    private static final int HASH_MASK = HASH_SIZE - 1;

    // ---------- RFC 1951 length codes for lengths 3..257 (codes 257..284) ----------
    // { firstLength, extraBits } per code, code = 257 + index
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
        for (; i < 144; i++) { FIXED_CODE[i] = 0x30 + i; FIXED_LEN[i] = 8; }
        for (; i < 256; i++) { FIXED_CODE[i] = 0x190 + (i - 144); FIXED_LEN[i] = 9; }
        for (; i < 280; i++) { FIXED_CODE[i] = i - 256; FIXED_LEN[i] = 7; }
        for (; i < 288; i++) { FIXED_CODE[i] = 0xC0 + (i - 280); FIXED_LEN[i] = 8; }
    }

    private final int maxChain;

    Deflate64Compressor(int maxChain) {
        this.maxChain = maxChain;
    }

    byte[] compress(byte[] data) {
        BitWriter bw = new BitWriter();

        if (data.length == 0) {
            bw.writeBits(1, 1);              // BFINAL = 1
            bw.writeBits(1, 2);              // BTYPE = 01 (fixed Huffman)
            writeSymbol(bw, 256);            // end of block
            bw.align();
            return bw.toByteArray();
        }

        int[] tokens = lz77(data);
        List<int[]> segments = split(tokens); // each: { startTok, endTok, coverage, inStart }

        for (int s = 0; s < segments.size(); s++) {
            int[] seg = segments.get(s);
            boolean last = s == segments.size() - 1;
            writeSegment(bw, data, tokens, seg, last);
        }

        bw.align();
        return bw.toByteArray();
    }

    // ---------- LZ77 (greedy, hash chains) ----------

    /**
     * Returns a packed token stream. Each token is a single int:
     * <ul>
     *   <li>literal: bit30 = 0, low 8 bits = byte value;</li>
     *   <li>match:   bit30 = 1, bits 17..25 = length (3..257), bits 0..16 = distance (1..65536).</li>
     * </ul>
     */
    private int[] lz77(byte[] data) {
        int n = data.length;
        int[] head = new int[HASH_SIZE];
        int[] prev = new int[n];
        java.util.Arrays.fill(head, -1);

        int[] tokens = new int[n + 1];
        int count = 0;
        int i = 0;

        while (i < n) {
            int bestLen = 0;
            int bestDist = 0;

            if (i + MIN_MATCH <= n) {
                int h = hash(data, i);
                int j = head[h];
                int chain = 0;
                int limit = Math.min(MAX_MATCH, n - i);

                while (j >= 0 && (i - j) <= WINDOW_SIZE && chain < maxChain) {
                    int len = 0;
                    while (len < limit && data[j + len] == data[i + len])
                        len++;
                    if (len > bestLen) {
                        bestLen = len;
                        bestDist = i - j;
                        if (len >= limit)
                            break;
                    }
                    j = prev[j];
                    chain++;
                }
            }

            if (bestLen >= MIN_MATCH) {
                tokens[count++] = (1 << 30) | (bestLen << 17) | bestDist;
                int end = i + bestLen;
                while (i < end) {
                    if (i + MIN_MATCH <= n)
                        insert(data, i, head, prev);
                    i++;
                }
            } else {
                tokens[count++] = data[i] & 0xFF;
                if (i + MIN_MATCH <= n)
                    insert(data, i, head, prev);
                i++;
            }
        }

        int[] res = new int[count];
        System.arraycopy(tokens, 0, res, 0, count);
        return res;
    }

    private static void insert(byte[] data, int i, int[] head, int[] prev) {
        int h = hash(data, i);
        prev[i] = head[h];
        head[h] = i;
    }

    private static int hash(byte[] data, int i) {
        int v = ((data[i] & 0xFF) << 16) | ((data[i + 1] & 0xFF) << 8) | (data[i + 2] & 0xFF);
        return (v * 0x9E3779B1) >>> (32 - HASH_BITS) & HASH_MASK;
    }

    // ---------- block splitting ----------

    private static boolean isMatch(int token) {
        return (token & (1 << 30)) != 0;
    }

    private static int tokenLength(int token) {
        return (token >>> 17) & 0x1FF;
    }

    private static int tokenDistance(int token) {
        return token & 0x1FFFF;
    }

    private static int advance(int token) {
        return isMatch(token) ? tokenLength(token) : 1;
    }

    private List<int[]> split(int[] tokens) {
        List<int[]> segments = new ArrayList<>();
        int start = 0;
        int cov = 0;
        int inStart = 0;

        for (int t = 0; t < tokens.length; t++) {
            int adv = advance(tokens[t]);
            if (cov + adv > MAX_STORED_BLOCK && t > start) {
                segments.add(new int[] { start, t, cov, inStart });
                inStart += cov;
                start = t;
                cov = 0;
            }
            cov += adv;
        }
        segments.add(new int[] { start, tokens.length, cov, inStart });
        return segments;
    }

    // ---------- block emission ----------

    private void writeSegment(BitWriter bw, byte[] data, int[] tokens, int[] seg, boolean last) {
        int startTok = seg[0];
        int endTok = seg[1];
        int cov = seg[2];
        int inStart = seg[3];

        long fixedBits = fixedCostBits(tokens, startTok, endTok);
        long storedBits = 3L + 8 + 32 + (long) cov * 8; // header + align + LEN/NLEN + payload

        int bfinal = last ? 1 : 0;

        if (storedBits < fixedBits) {
            bw.writeBits(bfinal, 1);
            bw.writeBits(0, 2);         // BTYPE = 00 (stored)
            bw.align();
            bw.writeBits(cov & 0xFFFF, 16);
            bw.writeBits(~cov & 0xFFFF, 16);
            for (int k = 0; k < cov; k++)
                bw.writeByte(data[inStart + k] & 0xFF);
        } else {
            bw.writeBits(bfinal, 1);
            bw.writeBits(1, 2);         // BTYPE = 01 (fixed Huffman)
            for (int t = startTok; t < endTok; t++)
                writeToken(bw, tokens[t]);
            writeSymbol(bw, 256);       // end of block
        }
    }

    private void writeToken(BitWriter bw, int token) {
        if (!isMatch(token)) {
            writeSymbol(bw, token & 0xFF);
            return;
        }

        int length = tokenLength(token);
        int lenIdx = lengthIndex(length);
        writeSymbol(bw, 257 + lenIdx);
        if (LEN_EXTRA[lenIdx] > 0)
            bw.writeBits(length - LEN_BASE[lenIdx], LEN_EXTRA[lenIdx]);

        int distance = tokenDistance(token);
        int distIdx = distanceIndex(distance);
        bw.writeHuff(distIdx, 5);       // fixed distance codes are 5 bits, value == code
        if (DIST_EXTRA[distIdx] > 0)
            bw.writeBits(distance - DIST_BASE[distIdx], DIST_EXTRA[distIdx]);
    }

    private static void writeSymbol(BitWriter bw, int symbol) {
        bw.writeHuff(FIXED_CODE[symbol], FIXED_LEN[symbol]);
    }

    private static long fixedCostBits(int[] tokens, int startTok, int endTok) {
        long bits = 0;
        for (int t = startTok; t < endTok; t++) {
            int token = tokens[t];
            if (!isMatch(token))
                bits += FIXED_LEN[token & 0xFF];
            else {
                int lenIdx = lengthIndex(tokenLength(token));
                int distIdx = distanceIndex(tokenDistance(token));
                bits += FIXED_LEN[257 + lenIdx] + LEN_EXTRA[lenIdx] + 5 + DIST_EXTRA[distIdx];
            }
        }
        bits += FIXED_LEN[256];
        return bits;
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

    private static final class BitWriter {

        private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        private int cur;
        private int n;

        /** Writes {@code numBits} of {@code value}, least significant bit first. */
        void writeBits(int value, int numBits) {
            for (int i = 0; i < numBits; i++) {
                pushBit(value & 1);
                value >>>= 1;
            }
        }

        /** Writes a canonical Huffman {@code code} of {@code numBits}, most significant bit first. */
        void writeHuff(int code, int numBits) {
            for (int i = numBits - 1; i >= 0; i--)
                pushBit((code >>> i) & 1);
        }

        void writeByte(int b) {
            buf.write(b & 0xFF);
        }

        void align() {
            if (n > 0) {
                buf.write(cur);
                cur = 0;
                n = 0;
            }
        }

        byte[] toByteArray() {
            align();
            return buf.toByteArray();
        }

        private void pushBit(int bit) {
            cur |= bit << n;
            if (++n == 8) {
                buf.write(cur);
                cur = 0;
                n = 0;
            }
        }
    }

}
