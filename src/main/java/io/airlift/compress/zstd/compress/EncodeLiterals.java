package io.airlift.compress.zstd.compress;

import io.airlift.compress.zstd.huffman.HuffmanCompressionTable;
import io.airlift.compress.zstd.huffman.HuffmanCompressor;
import io.airlift.compress.zstd.huffman.HuffmanConst;
import io.airlift.compress.zstd.io.HeaderDataOutDecorator;
import io.airlift.compress.zstd.io.RandomAccess;
import io.airlift.compress.zstd.model.Histogram;
import io.airlift.compress.zstd.model.LiteralsSectionHeader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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

    public static boolean encodeLiterals(RandomAccess rao, CompressionContext context) throws IOException {
        final int offs = rao.getOffs();

        RandomAccess literals = context.sequenceStore.getLiterals();
        int literalsSize = context.sequenceStore.getLiteralsSize();

        if (context.parameters.isBypassCompression() || literalsSize <= MINIMUM_LITERALS_SIZE)
            RawLiteralsEncode.encode(rao, literals);
        else {
            Histogram histogram = new Histogram(HuffmanConst.MAX_SYMBOL_COUNT);
            histogram.count(literals.slice(0, literalsSize));

            int maxSymbol = histogram.findMaxSymbol(HuffmanConst.MAX_SYMBOL);
            int largestCount = histogram.findLargestCount(maxSymbol);

            if (largestCount == literalsSize)
                // all bytes in input are equal
                RleLiteralsEncode.encode(rao, literals);
            else if (largestCount <= (literalsSize >>> 7) + 4)
                // heuristic: probably not compressible enough
                RawLiteralsEncode.encode(rao, literals);
            else {
                int headerSize = LiteralsSectionHeader.CompressedTreeless.calcHeaderSize(literalsSize);

                if (rao.available() < headerSize + MAX_HUFFMAN_TABLE_DESCRIPTION_SIZE)
                    // no room for a new Huffman tree description
                    RawLiteralsEncode.encode(rao, literals);
                else
                    huffmanCompressing(rao, context, histogram, maxSymbol);
            }
        }

        return rao.getOffs() > offs;
    }

    private static void huffmanCompressing(RandomAccess rao, CompressionContext context,
                                           Histogram histogram, int maxSymbol) throws IOException {
        HuffmanCompressionTable prevTable = context.huffmanCompressionContext.getPrevTable();
        int literalsSize = context.sequenceStore.getLiteralsSize();
        RandomAccess literals = context.sequenceStore.getLiterals();

        HuffmanCompressionTable table;
        int serializedTableSize;
        boolean reuseTable;

        boolean canReuse = prevTable.isValid(histogram.getCounts(), maxSymbol);
        boolean preferReuse = context.isPreferReuse();

        HeaderDataOutDecorator hdod = new HeaderDataOutDecorator(rao);
        hdod.setHeaderSize(LiteralsSectionHeader.CompressedTreeless.calcHeaderSize(literalsSize));

        RandomAccess dataOut = hdod.getOutForData();

        if (preferReuse && canReuse) {
            table = prevTable;
            reuseTable = true;
            serializedTableSize = 0;
        } else {
            HuffmanCompressionTable newTable = context.huffmanCompressionContext.borrowTemporaryTable();

            newTable.init(
                    histogram.getCounts(),
                    maxSymbol,
                    HuffmanCompressionTable.optimalNumberOfBits(MAX_HUFFMAN_TABLE_LOG, literalsSize, maxSymbol),
                    context.huffmanCompressionContext.getCompressionTableWorkspace());

            serializedTableSize = writeTable(dataOut, newTable);

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

        if (hdod.getHeaderSize() + serializedTableSize + maxStreamsSize > rao.available()) {
            context.huffmanCompressionContext.discardTemporaryTable();
            hdod.reset();
            RawLiteralsEncode.encode(rao, literals);
        } else {
            HuffmanCompressor huffmanCompressor = new HuffmanCompressor(literals.slice(0, literalsSize), table);
            boolean singleStream = literalsSize <= 0xFF;
            int compressedSize = compressStream(dataOut, singleStream, huffmanCompressor, literalsSize);

            int dataSize = hdod.saveAndGetDataSize();
            int minimumGain = context.calculateMinimumGain();

            if (compressedSize == 0 || dataSize >= literalsSize - minimumGain) {
                // incompressible or no savings
                // discard any temporary table we might have borrowed above
                context.huffmanCompressionContext.discardTemporaryTable();
                hdod.reset();
                RawLiteralsEncode.encode(rao, literals);
            } else {
                LiteralsSectionHeader.CompressedTreeless header =
                        reuseTable ? LiteralsSectionHeader.createForTreeless()
                                   : LiteralsSectionHeader.createForCompressed();
                header.withSize(singleStream, literalsSize, dataSize);
                header.encode(hdod.getOutForHeader());
                hdod.close();
            }
        }
    }

    private static int writeTable(RandomAccess rao, HuffmanCompressionTable newTable) {
        int loOffs = rao.getOffs();
        newTable.write(rao);
        return rao.getOffs() - loOffs;
    }

    private static int compressStream(RandomAccess rao,
                                      boolean singleStream,
                                      HuffmanCompressor huffmanCompressor,
                                      int regeneratedSize) throws IOException {
        int offs = rao.getOffs();

        if (singleStream)
            huffmanCompressor.compressSingleStream(rao, regeneratedSize);
        else
            huffmanCompressor.compressFourStreams(rao, regeneratedSize);

        return rao.getOffs() - offs;
    }

}
