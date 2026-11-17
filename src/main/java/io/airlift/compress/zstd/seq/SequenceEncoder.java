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

import io.airlift.compress.zstd.Histogram;
import io.airlift.compress.zstd.OutByteArrayWithOffs;
import io.airlift.compress.zstd.bos.MaskBitOutputStream;
import io.airlift.compress.zstd.compress.CompressionContext;
import io.airlift.compress.zstd.compress.CompressionParameters;
import io.airlift.compress.zstd.fse.FseCompressionTable;
import io.airlift.compress.zstd.fse.compress.FiniteStateEntropyForCompress;

import static io.airlift.compress.zstd.Constants.DEFAULT_MAX_OFFSET_CODE_SYMBOL;
import static io.airlift.compress.zstd.Constants.LITERALS_LENGTH_BITS;
import static io.airlift.compress.zstd.Constants.LITERAL_LENGTH_TABLE_LOG;
import static io.airlift.compress.zstd.Constants.MATCH_LENGTH_BITS;
import static io.airlift.compress.zstd.Constants.MATCH_LENGTH_TABLE_LOG;
import static io.airlift.compress.zstd.Constants.MAX_LITERALS_LENGTH_SYMBOL;
import static io.airlift.compress.zstd.Constants.MAX_MATCH_LENGTH_SYMBOL;
import static io.airlift.compress.zstd.Constants.MAX_OFFSET_CODE_SYMBOL;
import static io.airlift.compress.zstd.Constants.OFFSET_TABLE_LOG;
import static io.airlift.compress.zstd.seq.SequenceEncodingContext.MAX_SEQUENCES;

public class SequenceEncoder {

    private static final int DEFAULT_LITERAL_LENGTH_NORMALIZED_COUNTS_LOG = 6;
    private static final short[] DEFAULT_LITERAL_LENGTH_NORMALIZED_COUNTS = { 4, 3, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 1, 1, 1,
            2, 2, 2, 2, 2, 2, 2, 2,
            2, 3, 2, 1, 1, 1, 1, 1,
            -1, -1, -1, -1 };

    private static final int DEFAULT_MATCH_LENGTH_NORMALIZED_COUNTS_LOG = 6;
    private static final short[] DEFAULT_MATCH_LENGTH_NORMALIZED_COUNTS = { 1, 4, 3, 2, 2, 2, 2, 2,
            2, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, -1, -1,
            -1, -1, -1, -1, -1 };

    private static final int DEFAULT_OFFSET_NORMALIZED_COUNTS_LOG = 5;
    private static final short[] DEFAULT_OFFSET_NORMALIZED_COUNTS = { 1, 1, 1, 1, 1, 1, 2, 2,
            2, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
            -1, -1, -1, -1, -1 };

    private static final FseCompressionTable DEFAULT_LITERAL_LENGTHS_TABLE = FseCompressionTable.newInstance(
            DEFAULT_LITERAL_LENGTH_NORMALIZED_COUNTS,
            MAX_LITERALS_LENGTH_SYMBOL,
            DEFAULT_LITERAL_LENGTH_NORMALIZED_COUNTS_LOG);
    private static final FseCompressionTable DEFAULT_MATCH_LENGTHS_TABLE = FseCompressionTable.newInstance(
            DEFAULT_MATCH_LENGTH_NORMALIZED_COUNTS,
            MAX_MATCH_LENGTH_SYMBOL,
            DEFAULT_LITERAL_LENGTH_NORMALIZED_COUNTS_LOG);
    private static final FseCompressionTable DEFAULT_OFFSETS_TABLE = FseCompressionTable.newInstance(
            DEFAULT_OFFSET_NORMALIZED_COUNTS,
            DEFAULT_MAX_OFFSET_CODE_SYMBOL,
            DEFAULT_OFFSET_NORMALIZED_COUNTS_LOG);

    // the biggest FSE table description of literal lengths, offsets or match lengths: up to 53 symbols with table log
    // up to 9, i.e. 4 bits + up to (9 + 1) bits per symbol + 2 bits repeat flags
    // https://www.rfc-editor.org/rfc/rfc8878.html#section-4.1.1
    private static final int MAX_FSE_TABLE_DESCRIPTION_SIZE = 128;
    // Number_of_Sequences (up to 3 bytes) + Symbol_Compression_Modes (1 byte)
    private static final int MAX_SEQUENCES_SECTION_HEADER_SIZE = 3 + 1;

    private SequenceEncoder() {
    }

    public static boolean compressSequences(OutByteArrayWithOffs out, CompressionContext context) {
        if (out.available() < MAX_SEQUENCES_SECTION_HEADER_SIZE)
            return false;

        SequenceHeader sequenceHeader = new SequenceHeader();
        sequenceHeader.setSequenceCount(context.sequenceStore.sequenceCount);

        sequenceHeader.encodeSequenceCount(out);

        if (sequenceHeader.getSequenceCount() > 0) {
            // flags for FSE encoding type
            int headerAddress = out.getOffs();
            out.putByte((byte) 0xFF);

            int maxSymbol;
            int largestCount;

            // literal lengths
            Histogram histogram = new Histogram(MAX_SEQUENCES + 1);
            histogram.count(context.sequenceStore.literalLengthCodes, sequenceHeader.getSequenceCount());
            maxSymbol = histogram.findMaxSymbol(MAX_LITERALS_LENGTH_SYMBOL);
            largestCount = histogram.findLargestCount(maxSymbol);

            // ---------- literalsLengthCompressionMode ----------

            sequenceHeader.setLiteralsLengthCompressionMode(
                    selectCompressionMode(largestCount,
                                          sequenceHeader.getSequenceCount(),
                                          DEFAULT_LITERAL_LENGTH_NORMALIZED_COUNTS_LOG,
                                          true,
                                          context.parameters.getStrategy())
            );

            if (!hasRoomForTable(sequenceHeader.getLiteralsLengthCompressionMode(), out.available()))
                return false;

            FseCompressionTable literalLengthTable;

            switch (sequenceHeader.getLiteralsLengthCompressionMode()) {
                case RLE:
                    out.putByte(context.sequenceStore.literalLengthCodes[0]);
                    context.sequenceEncodingContext.literalLengthTable.initializeRleTable(maxSymbol);
                    literalLengthTable = context.sequenceEncodingContext.literalLengthTable;
                    break;
                case PREDEFINED:
                    literalLengthTable = DEFAULT_LITERAL_LENGTHS_TABLE;
                    break;
                case FSE_COMPRESSED:
                    buildCompressionTable(
                            context.sequenceEncodingContext.literalLengthTable,
                            out,
                            sequenceHeader.getSequenceCount(),
                            LITERAL_LENGTH_TABLE_LOG,
                            context.sequenceStore.literalLengthCodes,
                            histogram.getCounts(),
                            maxSymbol,
                            context.sequenceEncodingContext.normalizedCounts);
                    literalLengthTable = context.sequenceEncodingContext.literalLengthTable;
                    break;
                default:
                    throw new UnsupportedOperationException("not yet implemented");
            }

            // offsets
            histogram = new Histogram(MAX_SEQUENCES + 1);
            histogram.count(context.sequenceStore.offsetCodes, sequenceHeader.getSequenceCount());
            maxSymbol = histogram.findMaxSymbol(MAX_OFFSET_CODE_SYMBOL);
            largestCount = histogram.findLargestCount(maxSymbol);

            // We can only use the basic table if max <= DEFAULT_MAX_OFFSET_CODE_SYMBOL, otherwise the offsets are too large .
            boolean defaultAllowed = maxSymbol < DEFAULT_MAX_OFFSET_CODE_SYMBOL;

            // ---------- offsetCompressionMode ----------

            sequenceHeader.setOffsetCompressionMode(
                    selectCompressionMode(largestCount,
                                          sequenceHeader.getSequenceCount(),
                                          DEFAULT_OFFSET_NORMALIZED_COUNTS_LOG,
                                          defaultAllowed,
                                          context.parameters.getStrategy())
            );

            FseCompressionTable offsetCodeTable;

            if (!hasRoomForTable(sequenceHeader.getOffsetCompressionMode(), out.available()))
                return false;

            switch (sequenceHeader.getOffsetCompressionMode()) {
                case RLE:
                    out.putByte(context.sequenceStore.offsetCodes[0]);
                    context.sequenceEncodingContext.offsetCodeTable.initializeRleTable(maxSymbol);
                    offsetCodeTable = context.sequenceEncodingContext.offsetCodeTable;
                    break;
                case PREDEFINED:
                    offsetCodeTable = DEFAULT_OFFSETS_TABLE;
                    break;
                case FSE_COMPRESSED:
                    buildCompressionTable(
                            context.sequenceEncodingContext.offsetCodeTable,
                            out,
                            sequenceHeader.getSequenceCount(),
                            OFFSET_TABLE_LOG,
                            context.sequenceStore.offsetCodes,
                            histogram.getCounts(),
                            maxSymbol,
                            context.sequenceEncodingContext.normalizedCounts);
                    offsetCodeTable = context.sequenceEncodingContext.offsetCodeTable;
                    break;
                default:
                    throw new UnsupportedOperationException("not yet implemented");
            }

            // match lengths
            histogram = new Histogram(MAX_SEQUENCES + 1);
            histogram.count(context.sequenceStore.matchLengthCodes, sequenceHeader.getSequenceCount());
            maxSymbol = histogram.findMaxSymbol(MAX_MATCH_LENGTH_SYMBOL);
            largestCount = histogram.findLargestCount(maxSymbol);

            // ---------- matchLengthCompressionMode ----------

            sequenceHeader.setMatchLengthCompressionMode(
                    selectCompressionMode(largestCount,
                                          sequenceHeader.getSequenceCount(),
                                          DEFAULT_MATCH_LENGTH_NORMALIZED_COUNTS_LOG,
                                          true,
                                          context.parameters.getStrategy())
            );

            FseCompressionTable matchLengthTable;
            if (!hasRoomForTable(sequenceHeader.getMatchLengthCompressionMode(), out.available()))
                return false;

            switch (sequenceHeader.getMatchLengthCompressionMode()) {
                case RLE:
                    out.putByte(context.sequenceStore.matchLengthCodes[0]);
                    context.sequenceEncodingContext.matchLengthTable.initializeRleTable(maxSymbol);
                    matchLengthTable = context.sequenceEncodingContext.matchLengthTable;
                    break;
                case PREDEFINED:
                    matchLengthTable = DEFAULT_MATCH_LENGTHS_TABLE;
                    break;
                case FSE_COMPRESSED:
                    buildCompressionTable(
                            context.sequenceEncodingContext.matchLengthTable,
                            out,
                            sequenceHeader.getSequenceCount(),
                            MATCH_LENGTH_TABLE_LOG,
                            context.sequenceStore.matchLengthCodes,
                            histogram.getCounts(),
                            maxSymbol,
                            context.sequenceEncodingContext.normalizedCounts);

                    matchLengthTable = context.sequenceEncodingContext.matchLengthTable;
                    break;
                default:
                    throw new UnsupportedOperationException("not yet implemented");
            }

            int tmpOffs = out.getOffs();
            out.setOffs(headerAddress);
            sequenceHeader.encodeCompressionMode(out);
            out.setOffs(tmpOffs);
            encodeSequences(out, matchLengthTable, offsetCodeTable, literalLengthTable, context.sequenceStore);
            return true;
        }

        return true;
    }

    private static boolean hasRoomForTable(SequenceHeader.CompressionMode compressionMode, int availableBytes) {
        if (compressionMode == SequenceHeader.CompressionMode.RLE)
            return availableBytes >= 1;
        if (compressionMode == SequenceHeader.CompressionMode.FSE_COMPRESSED)
            return availableBytes >= MAX_FSE_TABLE_DESCRIPTION_SIZE;
        return true;
    }

    private static void buildCompressionTable(FseCompressionTable table,
                                              OutByteArrayWithOffs out,
                                              int sequenceCount,
                                              int maxTableLog,
                                              byte[] codes,
                                              int[] counts,
                                              int maxSymbol,
                                              short[] normalizedCounts) {
        FiniteStateEntropyForCompress fse = new FiniteStateEntropyForCompress();
        int tableLog = fse.optimalTableLog(maxTableLog, sequenceCount, maxSymbol);

        // this is a minor optimization. The last symbol is embedded in the initial FSE state, so it's not part of the bitstream. We can omit it from the
        // statistics (but only if its count is > 1). This makes the statistics a tiny bit more accurate.
        if (counts[codes[sequenceCount - 1]] > 1) {
            counts[codes[sequenceCount - 1]]--;
            sequenceCount--;
        }

        fse.normalizeCounts(normalizedCounts, tableLog, counts, sequenceCount, maxSymbol);
        table.initialize(normalizedCounts, maxSymbol, tableLog);

        // TODO: pass outputLimit directly
        fse.writeNormalizedCounts(out, normalizedCounts, maxSymbol, tableLog);
    }

    private static void encodeSequences(
            OutByteArrayWithOffs out,
            FseCompressionTable matchLengthTable,
            FseCompressionTable offsetsTable,
            FseCompressionTable literalLengthTable,
            SequenceStore sequences) {
        try (MaskBitOutputStream bos = new MaskBitOutputStream(out)) {
            // first symbols
            int matchLengthState = matchLengthTable.begin(sequences.matchLengthCodes[sequences.sequenceCount - 1]);
            int offsetState = offsetsTable.begin(sequences.offsetCodes[sequences.sequenceCount - 1]);
            int literalLengthState = literalLengthTable.begin(sequences.literalLengthCodes[sequences.sequenceCount -
                    1]);

            bos.addBits(sequences.literalLengths[sequences.sequenceCount - 1],
                        LITERALS_LENGTH_BITS[sequences.literalLengthCodes[sequences.sequenceCount - 1]]);
            bos.addBits(sequences.matchLengths[sequences.sequenceCount - 1],
                        MATCH_LENGTH_BITS[sequences.matchLengthCodes[sequences.sequenceCount - 1]]);
            bos.addBits(sequences.offsets[sequences.sequenceCount - 1],
                        sequences.offsetCodes[sequences.sequenceCount - 1]);

            if (sequences.sequenceCount >= 2) {
                for (int n = sequences.sequenceCount - 2; n >= 0; n--) {
                    byte literalLengthCode = sequences.literalLengthCodes[n];
                    byte offsetCode = sequences.offsetCodes[n];
                    byte matchLengthCode = sequences.matchLengthCodes[n];

                    int literalLengthBits = LITERALS_LENGTH_BITS[literalLengthCode];
                    int matchLengthBits = MATCH_LENGTH_BITS[matchLengthCode];

                    // (7)
                    offsetState = offsetsTable.encode(bos, offsetState, offsetCode); // 15
                    matchLengthState = matchLengthTable.encode(bos, matchLengthState, matchLengthCode); // 24
                    literalLengthState = literalLengthTable.encode(bos,
                                                                   literalLengthState,
                                                                   literalLengthCode); // 33

                    bos.addBits(sequences.literalLengths[n], literalLengthBits);
                    bos.addBits(sequences.matchLengths[n], matchLengthBits);
                    bos.addBits(sequences.offsets[n], offsetCode); // 31
                }
            }

            matchLengthTable.finish(bos, matchLengthState);
            offsetsTable.finish(bos, offsetState);
            literalLengthTable.finish(bos, literalLengthState);
        }
    }

    private static SequenceHeader.CompressionMode selectCompressionMode(
            int largestCount,
            int sequenceCount,
            int defaultNormalizedCountsLog,
            boolean isDefaultTableAllowed,
            CompressionParameters.Strategy strategy) {
        if (largestCount == sequenceCount) { // => all entries are equal
            if (isDefaultTableAllowed && sequenceCount <= 2) {
                /* Prefer set_basic over set_rle when there are 2 or fewer symbols,
                 * since RLE uses 1 byte, but set_basic uses 5-6 bits per symbol.
                 * If basic encoding isn't possible, always choose RLE.
                 */
                return SequenceHeader.CompressionMode.PREDEFINED;
            }

            return SequenceHeader.CompressionMode.RLE;
        }

        if (strategy.ordinal() <
                CompressionParameters.Strategy.LAZY.ordinal()) { // TODO: more robust check. Maybe encapsulate in strategy objects
            if (isDefaultTableAllowed) {
                int factor = 10 - strategy.ordinal(); // TODO more robust. Move it to strategy
                int baseLog = 3;
                long minNumberOfSequences = ((1L << defaultNormalizedCountsLog) * factor) >>
                        baseLog;  /* 28-36 for offset, 56-72 for lengths */

                if ((sequenceCount < minNumberOfSequences) ||
                        (largestCount < (sequenceCount >> (defaultNormalizedCountsLog - 1)))) {
                    /* The format allows default tables to be repeated, but it isn't useful.
                     * When using simple heuristics to select encoding type, we don't want
                     * to confuse these tables with dictionaries. When running more careful
                     * analysis, we don't need to waste time checking both repeating tables
                     * and default tables.
                     */
                    return SequenceHeader.CompressionMode.PREDEFINED;
                }
            }
        } else {
            // TODO implement when other strategies are supported
            throw new UnsupportedOperationException("not yet implemented");
        }

        return SequenceHeader.CompressionMode.FSE_COMPRESSED;
    }
}
