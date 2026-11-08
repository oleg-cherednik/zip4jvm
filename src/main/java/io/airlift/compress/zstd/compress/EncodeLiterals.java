package io.airlift.compress.zstd.compress;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.Histogram;
import io.airlift.compress.zstd.In;
import io.airlift.compress.zstd.InputStreamToInAdapter;
import io.airlift.compress.zstd.OutByteArrayWithOffs;
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

    public static int encodeLiterals(OutByteArrayWithOffs out, int outputSize, CompressionContext context)
            throws IOException {
        int outOffs = out.getOffs();
        boolean bypassCompression = context.parameters.getStrategy()
                                                      .getBypassCompression(context.parameters.getTargetLength());

        if (bypassCompression || context.sequenceStore.literalsBuffer.getOffs() <= MINIMUM_LITERALS_SIZE)
            return rawLiterals(out, context.sequenceStore.literalsBuffer);

        int headerSize = 3;

        if (context.sequenceStore.literalsBuffer.getOffs() >= 1024)
            headerSize++;
        if (context.sequenceStore.literalsBuffer.getOffs() >= 16384)
            headerSize++;

        Histogram histogram = new Histogram(MAX_SYMBOL_COUNT);
        histogram.count(context.sequenceStore.literalsBuffer);

        int maxSymbol = histogram.findMaxSymbol(MAX_SYMBOL);
        int largestCount = histogram.findLargestCount(maxSymbol);
        int literalsAddress = 0;

        // all bytes in input are equal
        if (largestCount == context.sequenceStore.literalsBuffer.getOffs())
            return rleLiterals(out, context.sequenceStore.literalsBuffer);

        // heuristic: probably not compressible enough
        if (largestCount <= (context.sequenceStore.literalsBuffer.getOffs() >>> 7) + 4)
            return rawLiterals(out, context.sequenceStore.literalsBuffer);

        HuffmanCompressionTable previousTable = context.huffmanCompressionContext.getPreviousTable();

        HuffmanCompressionTable table;
        int serializedTableSize;
        boolean reuseTable;

        boolean canReuse = previousTable.isValid(histogram.getCounts(), maxSymbol);

        // heuristic: use existing table for small inputs if valid
        // TODO: move to Strategy
        boolean preferReuse =
                context.parameters.getStrategy().ordinal() < CompressionParameters.Strategy.LAZY.ordinal() &&
                        context.sequenceStore.literalsBuffer.getOffs() <= 1024;

        if (preferReuse && canReuse) {
            table = previousTable;
            reuseTable = true;
            serializedTableSize = 0;
        } else {
            HuffmanCompressionTable newTable = context.huffmanCompressionContext.borrowTemporaryTable();

            newTable.init(
                    histogram.getCounts(),
                    maxSymbol,
                    HuffmanCompressionTable.optimalNumberOfBits(MAX_HUFFMAN_TABLE_LOG,
                                                                context.sequenceStore.literalsBuffer.getOffs(),
                                                                maxSymbol),
                    context.huffmanCompressionContext.getCompressionTableWorkspace());

            out.setOffs(out.getOffs() + headerSize);
            serializedTableSize = newTable.write(out, outputSize - headerSize);

            // Check if using previous huffman table is beneficial
            if (canReuse && previousTable.estimateCompressedSize(histogram.getCounts(), maxSymbol) <=
                    serializedTableSize + newTable.estimateCompressedSize(histogram.getCounts(), maxSymbol)) {
                table = previousTable;
                reuseTable = true;
                serializedTableSize = 0;
                context.huffmanCompressionContext.discardTemporaryTable();
            } else {
                table = newTable;
                reuseTable = false;
            }
        }

        boolean singleStream = context.sequenceStore.literalsBuffer.getOffs() < 256;
        out.setOffs(outOffs + headerSize + serializedTableSize);

        In in = new InputStreamToInAdapter(new ByteArrayInputStream(context.sequenceStore.literalsBuffer.buf));
        HuffmanCompressor huffmanCompressor = new HuffmanCompressor(in, table);

        int compressedSize = singleStream ? huffmanCompressor.compressSingleStream(out, literalsAddress)
                                          : huffmanCompressor.compress4streams(out,
                                                                               context.sequenceStore.literalsBuffer.getOffs());

        int totalSize = serializedTableSize + compressedSize;
        int minimumGain = calculateMinimumGain(context.sequenceStore.literalsBuffer.getOffs(),
                                               context.parameters.getStrategy());

        if (compressedSize == 0 || totalSize >= context.sequenceStore.literalsBuffer.getOffs() - minimumGain) {
            // incompressible or no savings
            // discard any temporary table we might have borrowed above
            context.huffmanCompressionContext.discardTemporaryTable();
            out.setOffs(outOffs);
            return rawLiterals(out, context.sequenceStore.literalsBuffer);
        }

        int encodingType = reuseTable ? TREELESS_LITERALS_BLOCK
                                      : COMPRESSED_LITERALS_BLOCK;

        out.setOffs(outOffs);

        // Build header
        switch (headerSize) {
            case 3: {  // 2 - 2 - 10 - 10
                int header = encodingType | ((singleStream ? 0 : 1) << 2)
                        | (context.sequenceStore.literalsBuffer.getOffs() << 4) | (totalSize << 14);
                out.putThreeBytes(header);
                break;
            }
            case 4: {  // 2 - 2 - 14 - 14
                int header = encodingType | (2 << 2) | (context.sequenceStore.literalsBuffer.getOffs() << 4) |
                        (totalSize << 18);
                out.putInt(header);
                break;
            }
            case 5: {  // 2 - 2 - 18 - 18
                int header = encodingType | (3 << 2) | (context.sequenceStore.literalsBuffer.getOffs() << 4) |
                        (totalSize << 22);
                out.putInt(header);
                out.putByte((byte) (totalSize >>> 10));
                break;
            }
            default:  // not possible : headerSize is {3,4,5}
                throw new IllegalStateException();
        }

        return headerSize + totalSize;
    }

    private static int rleLiterals(OutByteArrayWithOffs out, ByteArrayWithOffs in) {
        int headerSize = 1;

        if (in.getOffs() >= 31)
            headerSize++;
        if (in.getOffs() >= 4095)
            headerSize++;

        if (headerSize == 1)    // 2 - 1 - 5
            out.putByte((byte) (RLE_LITERALS_BLOCK | (in.getOffs() << 3)));
        else if (headerSize == 2)   // 2 - 2 - 12
            out.putShort((short) (RLE_LITERALS_BLOCK | (1 << 2) | (in.getOffs() << 4)));
        else    // 2 - 2 - 20
            out.putInt(RLE_LITERALS_BLOCK | 3 << 2 | in.getOffs() << 4);

        out.putByte(in.buf[0]);
        return headerSize + 1;
    }

    private static int rawLiterals(OutByteArrayWithOffs out, ByteArrayWithOffs in) {
        int headerSize = 1;

        if (in.getOffs() >= 32)
            headerSize++;
        if (in.getOffs() >= 4096)
            headerSize++;

        if (headerSize == 1)
            out.putByte((byte) (RAW_LITERALS_BLOCK | (in.getOffs() << 3)));
        else if (headerSize == 2)
            out.putShort((short) (RAW_LITERALS_BLOCK | (1 << 2) | (in.getOffs() << 4)));
        else
            out.putThreeBytes(RAW_LITERALS_BLOCK | (3 << 2) | (in.getOffs() << 4));

        in.copyMemory(out);
        return headerSize + in.getOffs();
    }

    public static int calculateMinimumGain(int inputSize, CompressionParameters.Strategy strategy) {
        return (inputSize >>> strategy.getMinLog()) + 2;
    }

}
