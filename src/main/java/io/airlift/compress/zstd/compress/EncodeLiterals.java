package io.airlift.compress.zstd.compress;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.Histogram;
import io.airlift.compress.zstd.In;
import io.airlift.compress.zstd.InputStreamToInAdapter;
import io.airlift.compress.zstd.OutByteArrayWithOffs;
import io.airlift.compress.zstd.bis.HeaderDataOutDecorator;
import io.airlift.compress.zstd.huffman.HuffmanCompressionTable;
import io.airlift.compress.zstd.huffman.HuffmanCompressor;
import io.airlift.compress.zstd.seq.SequenceStore;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static io.airlift.compress.zstd.Constants.COMPRESSED_LITERALS_BLOCK;
import static io.airlift.compress.zstd.Constants.TREELESS_LITERALS_BLOCK;
import static io.airlift.compress.zstd.huffman.HuffmanDecompressor.MAX_SYMBOL;
import static io.airlift.compress.zstd.huffman.HuffmanDecompressor.MAX_SYMBOL_COUNT;

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
            Histogram histogram = new Histogram(MAX_SYMBOL_COUNT);
            histogram.count(literalsBuf);

            int maxSymbol = histogram.findMaxSymbol(MAX_SYMBOL);
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

            int loOffs = out.getOffs();
            newTable.write(dataOut, outputSize - hdod.getHeaderSize());
            serializedTableSize = out.getOffs() - loOffs;

            // Check if using previous huffman table is beneficial
            if (canReuse && prevTable.estimateCompressedSize(histogram.getCounts(), maxSymbol) <=
                    serializedTableSize + newTable.estimateCompressedSize(histogram.getCounts(),
                                                                          maxSymbol)) {
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

        boolean singleStream = literalsBuf.getOffs() < 256;

        // Huffman streams do not check the output limit, so they are written only when there is room for them
        int maxStreamsSize = table.estimateCompressedSize(histogram.getCounts(), maxSymbol) +
                HUFFMAN_STREAMS_OVERHEAD;

        if (hdod.getHeaderSize() + serializedTableSize + maxStreamsSize > outputSize) {
            context.huffmanCompressionContext.discardTemporaryTable();
            hdod.reset();
            rawLiteralsEncode.encode(out, literalsBuf);
            return;
        }

        In in = new InputStreamToInAdapter(new ByteArrayInputStream(literalsBuf.buf));
        HuffmanCompressor huffmanCompressor = new HuffmanCompressor(in, table);

        int literalsSize = literalsBuf.getOffs();
        int compressedSize = singleStream ? huffmanCompressor.compressSingleStream(dataOut, literalsSize)
                                          : huffmanCompressor.compress4streams(dataOut, literalsSize);

        int totalSize = serializedTableSize + compressedSize;
        int minimumGain = context.calculateMinimumGain();

        if (compressedSize == 0 || totalSize >= literalsBuf.getOffs() - minimumGain) {
            // incompressible or no savings
            // discard any temporary table we might have borrowed above
            context.huffmanCompressionContext.discardTemporaryTable();
            hdod.reset();
            rawLiteralsEncode.encode(out, literalsBuf);
            return;
        }

        int encodingType = reuseTable ? TREELESS_LITERALS_BLOCK
                                      : COMPRESSED_LITERALS_BLOCK;

        hdod.saveDataSize();

        OutByteArrayWithOffs headerOut = hdod.getOutForHeader();

        // Build header
        switch (hdod.getHeaderSize()) {
            case 3: {  // 2 - 2 - 10 - 10
                int header = encodingType | ((singleStream ? 0 : 1) << 2)
                        | (literalsBuf.getOffs() << 4) | (totalSize << 14);
                headerOut.putThreeBytes(header);
                break;
            }
            case 4: {  // 2 - 2 - 14 - 14
                int header = encodingType | (2 << 2) | (literalsBuf.getOffs() << 4) |
                        (totalSize << 18);
                headerOut.putInt(header);
                break;
            }
            case 5: {  // 2 - 2 - 18 - 18
                int header = encodingType | (3 << 2) | (literalsBuf.getOffs() << 4) |
                        (totalSize << 22);
                headerOut.putInt(header);
                headerOut.putByte((byte) (totalSize >>> 10));
                break;
            }
            default:  // not possible : headerSize is {3,4,5}
                throw new IllegalStateException();
        }

        hdod.close();
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
