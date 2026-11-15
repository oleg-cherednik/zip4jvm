package io.airlift.compress.zstd.compress;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.Histogram;
import io.airlift.compress.zstd.In;
import io.airlift.compress.zstd.InputStreamToInAdapter;
import io.airlift.compress.zstd.LiteralsSectionHeader;
import io.airlift.compress.zstd.OutByteArrayWithOffs;
import io.airlift.compress.zstd.bis.HeaderDataOutDecorator;
import io.airlift.compress.zstd.huffman.HuffmanCompressionTable;
import io.airlift.compress.zstd.huffman.HuffmanCompressor;
import io.airlift.compress.zstd.huffman.HuffmanConst;
import io.airlift.compress.zstd.seq.SequenceStore;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 08.11.2026
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncodeLiterals {

    private static final int MINIMUM_LITERALS_SIZE = 63;
    // the maximum table log allowed for literal encoding per RFC 8478, section 4.2.1
    private static final int MAX_HUFFMAN_TABLE_LOG = 11;
    // the biggest Huffman tree description: FSE compressed weights of 255 symbols (a header + up to 6 bits per weight)
    // https://www.rfc-editor.org/rfc/rfc8878.html#section-4.2.1
    private static final int MAX_HUFFMAN_TABLE_DESCRIPTION_SIZE = 256;
    // Huffman streams in addition to the estimated size: a jump table (6), an end mark with rounding up to the byte
    // for each of 4 streams (4) and rounding down of the estimated size (1)
    private static final int HUFFMAN_STREAMS_OVERHEAD = 6 + 4 + 1;

    private static final RleLiteralsEncode rleLiteralsEncode = new RleLiteralsEncode();
    private static final RawLiteralsEncode rawLiteralsEncode = new RawLiteralsEncode();

    public static void encodeLiterals(OutByteArrayWithOffs out, int outputSize, CompressionContext context)
            throws IOException {
        boolean bypassCompression = context.parameters.isBypassCompression();
        ByteArrayWithOffs literalsBuf = context.sequenceStore.literalsBuf;
        int literalSize = literalsBuf.getOffs();

        if (bypassCompression || literalSize <= MINIMUM_LITERALS_SIZE)
            rawLiteralsEncode.encode(out, literalsBuf);
        else {
            Histogram histogram = new Histogram(HuffmanConst.MAX_SYMBOL_COUNT);
            histogram.count(literalsBuf);

            int maxSymbol = histogram.findMaxSymbol(HuffmanConst.MAX_SYMBOL);
            int largestCount = histogram.findLargestCount(maxSymbol);

            if (largestCount == literalsBuf.getOffs())
                // all bytes in input are equal
                rleLiteralsEncode.encode(out, literalsBuf);
            else if (largestCount <= (literalsBuf.getOffs() >>> 7) + 4)
                // heuristic: probably not compressible enough
                rawLiteralsEncode.encode(out, literalsBuf);
            else {
                int headerSize = getHeaderSize(context.sequenceStore);

                if (outputSize < headerSize + MAX_HUFFMAN_TABLE_DESCRIPTION_SIZE) {
                    // no room for a new Huffman tree description
                    rawLiteralsEncode.encode(out, literalsBuf);
                } else
                    huffmanCompressing(out, outputSize, context, histogram, maxSymbol);
            }
        }
    }

    private static void huffmanCompressing(OutByteArrayWithOffs out, int outputSize, CompressionContext context,
                                           Histogram histogram, int maxSymbol) throws IOException {
        HuffmanCompressionTable prevTable = context.huffmanCompressionContext.getPrevTable();
        ByteArrayWithOffs literalsBuf = context.sequenceStore.literalsBuf;

        HuffmanCompressionTable table;
        int serializedTableSize;
        boolean reuseTable;

        boolean canReuse = prevTable.isValid(histogram.getCounts(), maxSymbol);
        boolean preferReuse = context.isPreferReuse();

        HeaderDataOutDecorator hdod = new HeaderDataOutDecorator(out);
        hdod.setHeaderSize(getHeaderSize(context.sequenceStore));

        OutByteArrayWithOffs dataOut = hdod.getOutForData();

        if (preferReuse && canReuse) {
            table = prevTable;
            reuseTable = true;
            serializedTableSize = 0;
        } else {
            HuffmanCompressionTable newTable = context.huffmanCompressionContext.borrowTemporaryTable();

            newTable.init(
                    histogram.getCounts(),
                    maxSymbol,
                    HuffmanCompressionTable.optimalNumberOfBits(MAX_HUFFMAN_TABLE_LOG,
                                                                literalsBuf.getOffs(),
                                                                maxSymbol),
                    context.huffmanCompressionContext.getCompressionTableWorkspace());

            serializedTableSize = writeTable(dataOut, outputSize - hdod.getHeaderSize(), newTable);

            // Check if using previous huffman table is beneficial
            if (canReuse && prevTable.estimateCompressedSize(histogram.getCounts(), maxSymbol) <=
                    serializedTableSize + newTable.estimateCompressedSize(histogram.getCounts(), maxSymbol)) {
                table = prevTable;
                reuseTable = true;
                serializedTableSize = 0;
                context.huffmanCompressionContext.discardTemporaryTable();
                dataOut = hdod.getOutForData();
            } else {
                table = newTable;
                reuseTable = false;
            }
        }

        // Huffman streams do not check the output limit, so they are written only when there is room for them
        int maxStreamsSize = table.estimateCompressedSize(histogram.getCounts(), maxSymbol) + HUFFMAN_STREAMS_OVERHEAD;

        if (hdod.getHeaderSize() + serializedTableSize + maxStreamsSize > outputSize) {
            context.huffmanCompressionContext.discardTemporaryTable();
            hdod.reset();
            rawLiteralsEncode.encode(out, literalsBuf);
        } else {
            In in = new InputStreamToInAdapter(new ByteArrayInputStream(literalsBuf.buf));
            HuffmanCompressor huffmanCompressor = new HuffmanCompressor(in, table);
            boolean singleStream = literalsBuf.getOffs() <= 0xFF;

            int regeneratedSize = literalsBuf.getOffs();
            int compressedSize = singleStream ? huffmanCompressor.compressSingleStream(dataOut, regeneratedSize)
                                              : huffmanCompressor.compressFourStreams(dataOut, regeneratedSize);

            int dataSize = hdod.saveAndGetDataSize();
            int minimumGain = context.calculateMinimumGain();

            if (compressedSize == 0 || dataSize >= literalsBuf.getOffs() - minimumGain) {
                // incompressible or no savings
                // discard any temporary table we might have borrowed above
                context.huffmanCompressionContext.discardTemporaryTable();
                hdod.reset();
                rawLiteralsEncode.encode(out, literalsBuf);
            } else {
                LiteralsSectionHeader.CompressedTreeless header =
                        reuseTable ? LiteralsSectionHeader.createForTreeless()
                                   : LiteralsSectionHeader.createForCompressed();
                header.withSize(singleStream, regeneratedSize, dataSize);
                header.encode(hdod.getOutForHeader());
                hdod.close();
            }
        }
    }

    private static int writeTable(OutByteArrayWithOffs dataOut, int outputSize, HuffmanCompressionTable newTable) {
        int loOffs = dataOut.getOffs();
        newTable.write(dataOut, outputSize);
        return dataOut.getOffs() - loOffs;
    }

    private static int getHeaderSize(SequenceStore sequenceStore) {
        int headerSize = 3;

        if (sequenceStore.literalsBuf.getOffs() >= 1024)
            headerSize++;
        if (sequenceStore.literalsBuf.getOffs() >= 16384)
            headerSize++;

        return headerSize;
    }

}
