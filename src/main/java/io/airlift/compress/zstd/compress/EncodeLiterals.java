package io.airlift.compress.zstd.compress;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.Histogram;
import io.airlift.compress.zstd.In;
import io.airlift.compress.zstd.InputStreamToInAdapter;
import io.airlift.compress.zstd.OutByteArrayWithOffs;
import io.airlift.compress.zstd.bis.HeaderDataOutDecorator;
import io.airlift.compress.zstd.huffman.HuffmanCompressionTable;
import io.airlift.compress.zstd.huffman.HuffmanCompressor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static io.airlift.compress.zstd.Constants.COMPRESSED_LITERALS_BLOCK;
import static io.airlift.compress.zstd.Constants.RAW_LITERALS_BLOCK;
import static io.airlift.compress.zstd.Constants.RLE_LITERALS_BLOCK;
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

    public static void encodeLiterals(OutByteArrayWithOffs out, int outputSize, CompressionContext context)
            throws IOException {
        boolean bypassCompression = context.parameters.isBypassCompression();

        if (bypassCompression || context.sequenceStore.literalsBuffer.getOffs() <= MINIMUM_LITERALS_SIZE) {
            rawLiterals(out, outputSize, context.sequenceStore.literalsBuffer);
            return;
        }

        Histogram histogram = new Histogram(MAX_SYMBOL_COUNT);
        histogram.count(context.sequenceStore.literalsBuffer);

        int maxSymbol = histogram.findMaxSymbol(MAX_SYMBOL);
        int largestCount = histogram.findLargestCount(maxSymbol);

        // all bytes in input are equal
        if (largestCount == context.sequenceStore.literalsBuffer.getOffs()) {
            rleLiterals(out, outputSize, context.sequenceStore.literalsBuffer);
            return;
        }

        // heuristic: probably not compressible enough
        if (largestCount <= (context.sequenceStore.literalsBuffer.getOffs() >>> 7) + 4) {
            rawLiterals(out, outputSize, context.sequenceStore.literalsBuffer);
            return;
        }

        HuffmanCompressionTable previousTable = context.huffmanCompressionContext.getPreviousTable();

        HuffmanCompressionTable table;
        int serializedTableSize;
        boolean reuseTable;

        boolean canReuse = previousTable.isValid(histogram.getCounts(), maxSymbol);
        boolean preferReuse = context.isPreferReuse();

        HeaderDataOutDecorator hdod = new HeaderDataOutDecorator(out);
        hdod.setHeaderSize(getHeaderSize(context));

        OutByteArrayWithOffs dataOut = hdod.getOutForData();

        if (preferReuse && canReuse) {
            table = previousTable;
            reuseTable = true;
            serializedTableSize = 0;
        } else if (outputSize < hdod.getHeaderSize() + MAX_HUFFMAN_TABLE_DESCRIPTION_SIZE) {
            // no room for a new Huffman tree description
            hdod.reset();
            rawLiterals(out, outputSize, context.sequenceStore.literalsBuffer);
            return;
        } else {
            HuffmanCompressionTable newTable = context.huffmanCompressionContext.borrowTemporaryTable();

            newTable.init(
                    histogram.getCounts(),
                    maxSymbol,
                    HuffmanCompressionTable.optimalNumberOfBits(MAX_HUFFMAN_TABLE_LOG,
                                                                context.sequenceStore.literalsBuffer.getOffs(),
                                                                maxSymbol),
                    context.huffmanCompressionContext.getCompressionTableWorkspace());

            int loOffs = out.getOffs();
            newTable.write(dataOut, outputSize - hdod.getHeaderSize());
            serializedTableSize = out.getOffs() - loOffs;

            // Check if using previous huffman table is beneficial
            if (canReuse && previousTable.estimateCompressedSize(histogram.getCounts(), maxSymbol) <=
                    serializedTableSize + newTable.estimateCompressedSize(histogram.getCounts(), maxSymbol)) {
                table = previousTable;
                reuseTable = true;
                serializedTableSize = 0;
                context.huffmanCompressionContext.discardTemporaryTable();
                dataOut = hdod.getOutForData();
            } else {
                table = newTable;
                reuseTable = false;
            }
        }

        boolean singleStream = context.sequenceStore.literalsBuffer.getOffs() < 256;

        // Huffman streams do not check the output limit, so they are written only when there is room for them
        int maxStreamsSize = table.estimateCompressedSize(histogram.getCounts(), maxSymbol) + HUFFMAN_STREAMS_OVERHEAD;

        if (hdod.getHeaderSize() + serializedTableSize + maxStreamsSize > outputSize) {
            context.huffmanCompressionContext.discardTemporaryTable();
            hdod.reset();
            rawLiterals(out, outputSize, context.sequenceStore.literalsBuffer);
            return;
        }

        In in = new InputStreamToInAdapter(new ByteArrayInputStream(context.sequenceStore.literalsBuffer.buf));
        HuffmanCompressor huffmanCompressor = new HuffmanCompressor(in, table);

        int literalsSize = context.sequenceStore.literalsBuffer.getOffs();
        int compressedSize = singleStream ? huffmanCompressor.compressSingleStream(dataOut, literalsSize)
                                          : huffmanCompressor.compress4streams(dataOut, literalsSize);

        int totalSize = serializedTableSize + compressedSize;
        int minimumGain = context.calculateMinimumGain();

        if (compressedSize == 0 || totalSize >= context.sequenceStore.literalsBuffer.getOffs() - minimumGain) {
            // incompressible or no savings
            // discard any temporary table we might have borrowed above
            context.huffmanCompressionContext.discardTemporaryTable();
            hdod.reset();
            rawLiterals(out, outputSize, context.sequenceStore.literalsBuffer);
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
                        | (context.sequenceStore.literalsBuffer.getOffs() << 4) | (totalSize << 14);
                headerOut.putThreeBytes(header);
                break;
            }
            case 4: {  // 2 - 2 - 14 - 14
                int header = encodingType | (2 << 2) | (context.sequenceStore.literalsBuffer.getOffs() << 4) |
                        (totalSize << 18);
                headerOut.putInt(header);
                break;
            }
            case 5: {  // 2 - 2 - 18 - 18
                int header = encodingType | (3 << 2) | (context.sequenceStore.literalsBuffer.getOffs() << 4) |
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

    private static int getHeaderSize(CompressionContext context) {
        int headerSize = 3;

        if (context.sequenceStore.literalsBuffer.getOffs() >= 1024)
            headerSize++;
        if (context.sequenceStore.literalsBuffer.getOffs() >= 16384)
            headerSize++;

        return headerSize;
    }

    /**
     * @return size of the literals section or {@code 0} when it does not fit into {@code outputSize}
     */
    private static int rleLiterals(OutByteArrayWithOffs out, int outputSize, ByteArrayWithOffs in) {
        int headerSize = 1;

        if (in.getOffs() >= 31)
            headerSize++;
        if (in.getOffs() >= 4095)
            headerSize++;

        if (headerSize + 1 > outputSize)
            return 0;

        if (headerSize == 1)    // 2 - 1 - 5
            out.putByte((byte) (RLE_LITERALS_BLOCK | (in.getOffs() << 3)));
        else if (headerSize == 2)   // 2 - 2 - 12
            out.putShort((short) (RLE_LITERALS_BLOCK | (1 << 2) | (in.getOffs() << 4)));
        else    // 2 - 2 - 20
            out.putInt(RLE_LITERALS_BLOCK | 3 << 2 | in.getOffs() << 4);

        out.putByte(in.buf[0]);
        return headerSize + 1;
    }

    /**
     * @return size of the literals section or {@code 0} when it does not fit into {@code outputSize}
     */
    private static void rawLiterals(OutByteArrayWithOffs out, int outputSize, ByteArrayWithOffs in) {
        int headerSize = 1;

        if (in.getOffs() >= 32)
            headerSize++;
        if (in.getOffs() >= 4096)
            headerSize++;

        if (headerSize + in.getOffs() > outputSize)
            return;

        if (headerSize == 1)
            out.putByte((byte) (RAW_LITERALS_BLOCK | (in.getOffs() << 3)));
        else if (headerSize == 2)
            out.putShort((short) (RAW_LITERALS_BLOCK | (1 << 2) | (in.getOffs() << 4)));
        else
            out.putThreeBytes(RAW_LITERALS_BLOCK | (3 << 2) | (in.getOffs() << 4));

        out.copyMemory(in);
    }

}
