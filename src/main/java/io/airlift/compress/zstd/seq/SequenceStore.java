/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.airlift.compress.zstd.seq;

import io.airlift.compress.zstd.io.ByteArrayRandomAccess;
import io.airlift.compress.zstd.io.ByteArrayRandomAccessIn;
import io.airlift.compress.zstd.io.ByteArrayWithOffs;
import io.airlift.compress.zstd.io.RandomAccess;
import io.airlift.compress.zstd.model.Constants;
import io.airlift.compress.zstd.model.Util;

public class SequenceStore {

    public final ByteArrayWithOffs literalsBuf;

    public final int[] offsets;
    public final int[] literalLengths;
    public final int[] matchLengths;
    public int sequenceCount;

    public final byte[] literalLengthCodes;
    public final byte[] matchLengthCodes;
    public final byte[] offsetCodes;

    public LongField longLengthField;
    public int longLengthPosition;

    private static final byte[] LITERAL_LENGTH_CODE = {
            0, 1, 2, 3, 4, 5, 6, 7,
            8, 9, 10, 11, 12, 13, 14, 15,
            16, 16, 17, 17, 18, 18, 19, 19,
            20, 20, 20, 20, 21, 21, 21, 21,
            22, 22, 22, 22, 22, 22, 22, 22,
            23, 23, 23, 23, 23, 23, 23, 23,
            24, 24, 24, 24, 24, 24, 24, 24,
            24, 24, 24, 24, 24, 24, 24, 24 };

    private static final byte[] MATCH_LENGTH_CODE = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
            16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
            32, 32, 33, 33, 34, 34, 35, 35, 36, 36, 36, 36, 37, 37, 37, 37,
            38, 38, 38, 38, 38, 38, 38, 38, 39, 39, 39, 39, 39, 39, 39, 39,
            40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
            41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
            42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42,
            42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42 };

    public SequenceStore(int blockSize, int maxSequences) {
        offsets = new int[maxSequences];
        literalLengths = new int[maxSequences];
        matchLengths = new int[maxSequences];

        literalLengthCodes = new byte[maxSequences];
        matchLengthCodes = new byte[maxSequences];
        offsetCodes = new byte[maxSequences];
        literalsBuf = new ByteArrayWithOffs(new byte[blockSize]);
    }

    public RandomAccess getLiteralsBufSlice() {
        return new ByteArrayRandomAccess(literalsBuf.buf,
                                         literalsBuf.getOffs(),
                                         literalsBuf.getOffs());
    }

    public int getLiteralsSize() {
        return literalsBuf.getOffs();
    }

    public void appendLiterals(byte[] in, int offs, int inputSize) {
        for (int i = 0; i < inputSize; i++)
            literalsBuf.putByte(in[offs + i]);
    }

    public void storeSequence(ByteArrayRandomAccessIn in,
                              int literalLength,
                              int offsetCode,
                              int matchLengthBase) {
        literalsBuf.copyMemory(in, literalLength);

        if (literalLength > 65535) {
            longLengthField = LongField.LITERAL;
            longLengthPosition = sequenceCount;
        }
        literalLengths[sequenceCount] = literalLength;

        offsets[sequenceCount] = offsetCode + 1;

        if (matchLengthBase > 65535) {
            longLengthField = LongField.MATCH;
            longLengthPosition = sequenceCount;
        }

        matchLengths[sequenceCount] = matchLengthBase;
        sequenceCount++;
    }

    public void reset() {
        literalsBuf.setOffs(0);
        sequenceCount = 0;
        longLengthField = null;
    }

    public void generateCodes() {
        for (int i = 0; i < sequenceCount; ++i) {
            literalLengthCodes[i] = (byte) literalLengthToCode(literalLengths[i]);
            offsetCodes[i] = (byte) Util.highestBit(offsets[i]);
            matchLengthCodes[i] = (byte) matchLengthToCode(matchLengths[i]);
        }

        if (longLengthField == LongField.LITERAL)
            literalLengthCodes[longLengthPosition] = Constants.MAX_LITERALS_LENGTH_SYMBOL;
        if (longLengthField == LongField.MATCH)
            matchLengthCodes[longLengthPosition] = Constants.MAX_MATCH_LENGTH_SYMBOL;
    }

    private static int literalLengthToCode(int literalLength) {
        return literalLength >= 64 ? Util.highestBit(literalLength) + 19
                                   : LITERAL_LENGTH_CODE[literalLength];
    }

    /*
     * matchLengthBase = matchLength - MINMATCH
     * (that's how it's stored in SequenceStore)
     */
    private static int matchLengthToCode(int matchLengthBase) {
        return matchLengthBase >= 128 ? Util.highestBit(matchLengthBase) + 36
                                      : MATCH_LENGTH_CODE[matchLengthBase];
    }

    public enum LongField {
        LITERAL,
        MATCH
    }
}
