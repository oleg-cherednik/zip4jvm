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
package io.airlift.compress.zstd.compress;

import ru.olegcherednik.zip4jvm.utils.BitUtils;

import io.airlift.compress.MalformedInputException;
import io.airlift.compress.zstd.BackwardDecorator;
import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.FrameHeader;
import io.airlift.compress.zstd.bis.BackwardBitInputDecorator;
import io.airlift.compress.zstd.bis.SequencesInitializer;
import io.airlift.compress.zstd.fse.FiniteStateEntropy;
import io.airlift.compress.zstd.fse.FseTableReader;
import io.airlift.compress.zstd.huffman.Huffman;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.airlift.compress.zstd.Constants.COMPRESSED_BLOCK;
import static io.airlift.compress.zstd.Constants.COMPRESSED_LITERALS_BLOCK;
import static io.airlift.compress.zstd.Constants.DEFAULT_MAX_OFFSET_CODE_SYMBOL;
import static io.airlift.compress.zstd.Constants.LITERALS_LENGTH_BITS;
import static io.airlift.compress.zstd.Constants.LITERAL_LENGTH_TABLE_LOG;
import static io.airlift.compress.zstd.Constants.LONG_NUMBER_OF_SEQUENCES;
import static io.airlift.compress.zstd.Constants.MAGIC_NUMBER;
import static io.airlift.compress.zstd.Constants.MATCH_LENGTH_BITS;
import static io.airlift.compress.zstd.Constants.MATCH_LENGTH_TABLE_LOG;
import static io.airlift.compress.zstd.Constants.MAX_BLOCK_SIZE;
import static io.airlift.compress.zstd.Constants.MAX_LITERALS_LENGTH_SYMBOL;
import static io.airlift.compress.zstd.Constants.MAX_MATCH_LENGTH_SYMBOL;
import static io.airlift.compress.zstd.Constants.MIN_WINDOW_LOG;
import static io.airlift.compress.zstd.Constants.OFFSET_TABLE_LOG;
import static io.airlift.compress.zstd.Constants.RAW_BLOCK;
import static io.airlift.compress.zstd.Constants.RAW_LITERALS_BLOCK;
import static io.airlift.compress.zstd.Constants.RLE_BLOCK;
import static io.airlift.compress.zstd.Constants.RLE_LITERALS_BLOCK;
import static io.airlift.compress.zstd.Constants.SEQUENCE_ENCODING_BASIC;
import static io.airlift.compress.zstd.Constants.SEQUENCE_ENCODING_COMPRESSED;
import static io.airlift.compress.zstd.Constants.SEQUENCE_ENCODING_REPEAT;
import static io.airlift.compress.zstd.Constants.SEQUENCE_ENCODING_RLE;
import static io.airlift.compress.zstd.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;
import static io.airlift.compress.zstd.Constants.TREELESS_LITERALS_BLOCK;
import static io.airlift.compress.zstd.Util.fail;
import static io.airlift.compress.zstd.Util.verify;
import static io.airlift.compress.zstd.bis.BackwardBitInputDecorator.peekBits;

@RequiredArgsConstructor
public class ZstdFrameDecompressor {

    private static final int[] DEC_32_TABLE = { 4, 1, 2, 1, 4, 4, 4, 4 };
    private static final int[] DEC_64_TABLE = { 0, 0, 0, -1, 0, 1, 2, 3 };

    private static final int V07_MAGIC_NUMBER = 0xFD2FB527;

    private static final int[] LITERALS_LENGTH_BASE = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
            16, 18, 20, 22, 24, 28, 32, 40, 48, 64, 0x80, 0x100, 0x200, 0x400, 0x800, 0x1000,
            0x2000, 0x4000, 0x8000, 0x10000 };

    private static final int[] MATCH_LENGTH_BASE = {
            3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18,
            19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34,
            35, 37, 39, 41, 43, 47, 51, 59, 67, 83, 99, 0x83, 0x103, 0x203, 0x403, 0x803,
            0x1003, 0x2003, 0x4003, 0x8003, 0x10003 };

    private static final int[] OFFSET_CODES_BASE = {
            0, 1, 1, 5, 0xD, 0x1D, 0x3D, 0x7D,
            0xFD, 0x1FD, 0x3FD, 0x7FD, 0xFFD, 0x1FFD, 0x3FFD, 0x7FFD,
            0xFFFD, 0x1FFFD, 0x3FFFD, 0x7FFFD, 0xFFFFD, 0x1FFFFD, 0x3FFFFD, 0x7FFFFD,
            0xFFFFFD, 0x1FFFFFD, 0x3FFFFFD, 0x7FFFFFD, 0xFFFFFFD };

    private static final FiniteStateEntropy.Table DEFAULT_LITERALS_LENGTH_TABLE = new FiniteStateEntropy.Table(
            6,
            new int[] {
                    0, 16, 32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 32, 0, 0, 32, 0, 32, 0, 32, 0,
                    0, 32, 0, 32, 0, 32, 0, 0, 16, 32, 0, 0, 48, 16, 32, 32, 32,
                    32, 32, 32, 32, 32, 0, 32, 32, 32, 32, 32, 32, 0, 0, 0, 0 },
            new byte[] {
                    0, 0, 1, 3, 4, 6, 7, 9, 10, 12, 14, 16, 18, 19, 21, 22, 24, 25, 26, 27, 29, 31, 0, 1, 2, 4, 5, 7, 8,
                    10, 11, 13, 16, 17, 19, 20, 22, 23, 25, 25, 26, 28, 30, 0,
                    1, 2, 3, 5, 6, 8, 9, 11, 12, 15, 17, 18, 20, 21, 23, 24, 35, 34, 33, 32 },
            new byte[] {
                    4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 6, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 4, 4, 5, 5, 5, 5, 5, 5, 5, 6, 5,
                    5, 5, 5, 5, 5, 4, 4, 5, 6, 6, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5,
                    6, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6 });

    private static final FiniteStateEntropy.Table DEFAULT_OFFSET_CODES_TABLE = new FiniteStateEntropy.Table(
            5,
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0,
                    0 },
            new byte[] { 0, 6, 9, 15, 21, 3, 7, 12, 18, 23, 5, 8, 14, 20, 2, 7, 11, 17, 22, 4, 8, 13, 19, 1, 6, 10, 16,
                    28, 27, 26, 25, 24 },
            new byte[] { 5, 4, 5, 5, 5, 5, 4, 5, 5, 5, 5, 4, 5, 5, 5, 4, 5, 5, 5, 5, 4, 5, 5, 5, 4, 5, 5, 5, 5, 5, 5,
                    5 });

    private static final FiniteStateEntropy.Table DEFAULT_MATCH_LENGTH_TABLE = new FiniteStateEntropy.Table(
            6,
            new int[] {
                    0, 0, 32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 32, 0, 32, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 48, 16, 32, 32, 32, 32,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            new byte[] {
                    0, 1, 2, 3, 5, 6, 8, 10, 13, 16, 19, 22, 25, 28, 31, 33, 35, 37, 39, 41, 43, 45, 1, 2, 3, 4, 6, 7,
                    9, 12, 15, 18, 21, 24, 27, 30, 32, 34, 36, 38, 40, 42, 44, 1,
                    1, 2, 4, 5, 7, 8, 11, 14, 17, 20, 23, 26, 29, 52, 51, 50, 49, 48, 47, 46 },
            new byte[] {
                    6, 4, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 6,
                    6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6,
                    6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6 });

    private final ByteArrayWithOffs in;

    // extra space to allow for long-at-a-time copy
    private final byte[] literals = new byte[MAX_BLOCK_SIZE + SIZE_OF_LONG];

    // current buffer containing literals
    private byte[] literalsBase;
    private int literalsAddress;
    private int literalsLimit;
    private ByteArrayWithOffs literalsWithOffs;

    private final int[] prevOffs = new int[3];

    private final FiniteStateEntropy.Table literalsLengthTable = new FiniteStateEntropy.Table(LITERAL_LENGTH_TABLE_LOG);
    private final FiniteStateEntropy.Table offsetCodesTable = new FiniteStateEntropy.Table(OFFSET_TABLE_LOG);
    private final FiniteStateEntropy.Table matchLengthTable = new FiniteStateEntropy.Table(MATCH_LENGTH_TABLE_LOG);

    private FiniteStateEntropy.Table currentLiteralsLengthTable;
    private FiniteStateEntropy.Table currentOffsetCodesTable;
    private FiniteStateEntropy.Table currentMatchLengthTable;

    private final Huffman huffman = new Huffman();

    public int decompress(ByteArrayWithOffs out) {
        if (in.available() == 0)
            return 0;

        while (in.available() > 0) {
            reset();
            int offs = readFrame(out);
            out.setOffs(offs);
        }

        return out.getOffs();
    }

    private int readFrame(ByteArrayWithOffs out) {
        /*
         * Magic_Number
         * Frame_Header
         * Data_Block
         * [More data blocks]
         * [Content_Checksum]
         */
        int outOffs = out.getOffs();
        int outputStart = outOffs;

        verifyMagic();
        FrameHeader frameHeader = readFrameHeader();
        AtomicBoolean lastBlock = new AtomicBoolean(false);

        do {
            outOffs += readDataBlock(out, lastBlock);
            out.setOffs(outOffs);
        } while (!lastBlock.get());

        if (frameHeader.isHasChecksum()) {
            int decodedFrameSize = outOffs - outputStart;

            long hash = XxHash64.hash(0, out, outputStart, decodedFrameSize);

            int checksum = in.getInt();
            if (checksum != (int) hash) {
                throw new MalformedInputException(0, String.format("Bad checksum. Expected: %s, actual: %s",
                                                                   Integer.toHexString(checksum),
                                                                   Integer.toHexString((int) hash)));
            }
        }

        return outOffs;
    }

    private int readDataBlock(ByteArrayWithOffs out, AtomicBoolean lastBlock) {
        /*
         * Block_Header (3 bytes)
         * Block_Content (n bytes)
         */
        // read block blockHeader
        int b0 = in.getByte();
        int b1 = in.getByte();
        int b2 = in.getByte();
        int blockHeader = b2 << 16 | b1 << 8 | b0;

        lastBlock.set(isLastBlock(blockHeader));
        int blockType = getBlockType(blockHeader);
        int blockSize = getBlockSize(blockHeader);

        if (blockType == RAW_BLOCK)
            // this is an uncompressed block. Block_Content contains Block_Size bytes
            return decodeRawBlock(out, blockSize);

        if (blockType == RLE_BLOCK)
            /*
             * this is a single byte, repeated Block_Size times. Block_Content
             * consists of a single byte. On the decompression side, this byte
             * must be repeated Block_Size times
             */
            return decodeRleBlock(out, blockSize);

        if (blockType == COMPRESSED_BLOCK)
            /*
             * this is a Zstandard compressed block, explained later on.
             * Block_Size is the length of Block_Content, the compressed data.
             * The decompressed size is not known, but its maximum possible
             * value is guaranteed
             */
            return decodeCompressedBlock(out, blockSize);

        throw fail(0, "Invalid block type");
    }

    private static boolean isLastBlock(int blockHeader) {
        // bit0
        return BitUtils.isBitSet(blockHeader, BitUtils.BIT0);
    }

    private static int getBlockType(int blockHeader) {
        // bit1_2
        return (blockHeader >> 1) & 0b11;
    }

    // 3.1.1.2.3
    private static int getBlockSize(int blockHeader) {
        // bit3_23
        return (blockHeader >> 3) & 0x1F_FFFF; // 21 bits
    }

    private void reset() {
        prevOffs[0] = 1;
        prevOffs[1] = 4;
        prevOffs[2] = 8;

        currentLiteralsLengthTable = null;
        currentOffsetCodesTable = null;
        currentMatchLengthTable = null;
    }

    private int decodeRawBlock(ByteArrayWithOffs out, int blockSize) {
        in.copyMemory(out, blockSize);
        return blockSize;
    }

    private int decodeRleBlock(ByteArrayWithOffs out, int blockSize) {
        long b = in.getByte();
        int remaining = blockSize;

        if (remaining > SIZE_OF_LONG) {
            long packed = 0;

            for (int i = 0; i < SIZE_OF_LONG; i++)
                packed = packed << 8 | b;

            while (remaining > SIZE_OF_LONG) {
                out.putLong(packed);
                remaining -= SIZE_OF_LONG;
            }
        }

        while (remaining > 0) {
            out.putByte((byte) b);
            remaining -= SIZE_OF_BYTE;
        }

        return blockSize;
    }

    private int decodeCompressedBlock(ByteArrayWithOffs out, int blockSize) {
        in.setInputLimit(in.getOffs() + blockSize);

        // decode literals
        int b1 = in.getByte();
        int literalsBlockType = b1 & 0b11;

        if (literalsBlockType == RAW_LITERALS_BLOCK)
            decodeRawLiteralsBlock(b1);
        else if (literalsBlockType == RLE_LITERALS_BLOCK)
            decodeRleLiteralsBlock(b1);
        else {
            if (literalsBlockType == TREELESS_LITERALS_BLOCK)
                verify(huffman.isLoaded(), 0x0, "Dictionary is corrupted");

            decodeCompressedLiteralsBlock(b1, literalsBlockType);
        }

        return decompressSequences(out);
    }

    /**
     * Decodes the Sequences_Section of a compressed block and executes every sequence, i.e. combines the literals
     * (already decoded into {@link #literalsBase}) with the match copies into {@code out}.
     * <pre>
     *     Sequences_Section_Header
     *         [Literals_Length_Table]
     *         [Offset_Table]
     *         [Match_Length_Table]
     *         bitStream
     * </pre>
     * Before the call {@code in} must point to the first byte of the Sequences_Section and its input limit must be
     * set to the end of the block (Sequences_Section_Size = Block_Size - Literals_Section_Header -
     * Literals_Section_Content).
     *
     * @param out output buffer; decoded data is written starting from {@code out.getOffs()}
     * @return number of bytes written to {@code out} (decompressed size of the block)
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.3.2">RFC 8878, 3.1.1.3.2.
     *         Sequences_Section</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.4">RFC 8878, 3.1.1.4. Sequence
     *         Execution</a>
     */
    private int decompressSequences(ByteArrayWithOffs out) {
        final int startOutOffs = out.getOffs();
        // "fast" limits: while the output position is below them it is safe to write whole longs (8 bytes) without
        // checking the buffer boundary (wild copy with over-copy); not a part of the spec, just an optimization
        final int fastOutputLimit = out.buf.length - SIZE_OF_LONG;
        final long fastMatchOutputLimit = fastOutputLimit - SIZE_OF_LONG;

        int curInOffs = in.getOffs();
        int curOutOffs = out.getOffs();
        // current read position in the decoded literals (Literals_Section content)
        int literalsInput = literalsAddress;

        // Sequences_Section_Header: Number_of_Sequences (1-3 bytes)
        // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.3.2.1
        int sequenceCount = in.getByte();

        // byte0 == 0: there are no sequences; the block content is defined entirely by the Literals_Section
        // and the FSE tables used in Repeat_Mode are not updated
        if (sequenceCount != 0) {
            if (sequenceCount == 255)
                // byte0 == 255: Number_of_Sequences = byte1 + (byte2 << 8) + 0x7F00 (3 bytes)
                sequenceCount = in.getShort() + LONG_NUMBER_OF_SEQUENCES;
            else if (sequenceCount > 127)
                // byte0 < 255: Number_of_Sequences = ((byte0 - 128) << 8) + byte1 (2 bytes)
                sequenceCount = ((sequenceCount - 128) << 8) + in.getByte();
            // byte0 < 128: Number_of_Sequences = byte0 (1 byte)

            // Symbol_Compression_Modes (1 byte)
            // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.3.2.1 (Table 14, Table 15)
            // bit 7-6 - Literal_Lengths_Mode
            // bit 5-4 - Offsets_Mode
            // bit 3-2 - Match_Lengths_Mode
            // bit 1-0 - Reserved, must be all zeroes (not verified here)
            // Mode: 0 - Predefined_Mode, 1 - RLE_Mode, 2 - FSE_Compressed_Mode, 3 - Repeat_Mode
            int type = in.getByte();
            int literalsLengthType = (type & 0xFF) >>> 6;
            int offsetCodesType = (type >>> 4) & 0b11;
            int matchLengthType = (type >>> 2) & 0b11;

            // optional FSE tables follow the header in exactly this order:
            // Literals_Length_Table, Offset_Table, Match_Length_Table
            // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.3.2
            // Predefined_Mode uses default distributions:
            // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.3.2.2
            // FSE_Compressed_Mode reads the distribution table:
            // https://www.rfc-editor.org/rfc/rfc8878.html#section-4.1.1
            computeLiteralsTable(in, literalsLengthType);
            computeOffsetsTable(in, offsetCodesType);
            computeMatchLengthTable(in, matchLengthType);

            // bitStream: it is read backward, i.e. from the last byte of the block towards the tables. The last byte
            // contains the padding: up to 7 zero bits followed by a single 1 bit that must be skipped.
            // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.3.2.1.2
            // https://www.rfc-editor.org/rfc/rfc8878.html#section-4.1
            BackwardDecorator bbis = new BackwardDecorator(in, in.getInputLimit() - in.getOffs());
            SequencesInitializer sequenceInitializer = new SequencesInitializer(bbis);
            int bitsConsumed = sequenceInitializer.getBitsConsumed();
            long bits = sequenceInitializer.getBits();

            // initial FSE states, each uses Accuracy_Log bits of its table, in order:
            // Literals_Length_State, Offset_State, Match_Length_State
            // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.3.2.1.2
            int literalsLengthState = (int) peekBits(bitsConsumed, bits, currentLiteralsLengthTable.log2Size);
            bitsConsumed += currentLiteralsLengthTable.log2Size;

            int offsetCodesState = (int) peekBits(bitsConsumed, bits, currentOffsetCodesTable.log2Size);
            bitsConsumed += currentOffsetCodesTable.log2Size;

            int matchLengthState = (int) peekBits(bitsConsumed, bits, currentMatchLengthTable.log2Size);
            bitsConsumed += currentMatchLengthTable.log2Size;

            byte[] literalsLengthNumbersOfBits = currentLiteralsLengthTable.numberOfBits;
            int[] literalsLengthNewStates = currentLiteralsLengthTable.newState;
            byte[] literalsLengthSymbols = currentLiteralsLengthTable.symbol;

            byte[] matchLengthNumbersOfBits = currentMatchLengthTable.numberOfBits;
            int[] matchLengthNewStates = currentMatchLengthTable.newState;
            byte[] matchLengthSymbols = currentMatchLengthTable.symbol;

            byte[] offsetCodesNumbersOfBits = currentOffsetCodesTable.numberOfBits;
            int[] offsetCodesNewStates = currentOffsetCodesTable.newState;
            byte[] offsetCodesSymbols = currentOffsetCodesTable.symbol;

            // sequences are decoded in order from first to last, Number_of_Sequences times
            // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.3.2.1.2
            while (sequenceCount > 0) {
                sequenceCount--;

                // refill the 64-bit container; after this at least 57 (64 - 7) bits are available
                BackwardBitInputDecorator bbid1 = new BackwardBitInputDecorator(bbis, bits, bitsConsumed);
                bitsConsumed = bbid1.getBitsConsumed();
                bits = bbid1.getBits();

                // more bits were consumed than the bitstream contains: this is only acceptable after the last
                // sequence, otherwise the stream is corrupted
                if (bbid1.isOverflow()) {
                    verify(sequenceCount == 0, bbis.getFromOffs(), "Not all sequences were consumed");
                    break;
                }

                // current FSE states give the codes (symbols) of the sequence
                // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.3.2.1.1
                int literalsLengthCode = literalsLengthSymbols[literalsLengthState];
                int matchLengthCode = matchLengthSymbols[matchLengthState];
                int offsetCode = offsetCodesSymbols[offsetCodesState];

                // Number_of_Bits for the code (Table 16, Table 17)
                int literalsLengthBits = LITERALS_LENGTH_BITS[literalsLengthCode];
                int matchLengthBits = MATCH_LENGTH_BITS[matchLengthCode];

                // additional bits are read in order: Offset, Match_Length, Literals_Length
                // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.3.2.1.2

                // Offset: offsetCode is also the number of additional bits
                //   Offset_Value = (1 << offsetCode) + readNBits(offsetCode)
                //   if (Offset_Value > 3) Offset = Offset_Value - 3
                // OFFSET_CODES_BASE[offsetCode] already includes '-3' for offsetCode >= 2, so 'offset' is the real
                // offset for them. For offsetCode 0 and 1 (Offset_Value 1..3 - repeat codes) 'offset' is
                // Offset_Value - 1, i.e. the index of Repeated_Offset: 0, 1 or 2.
                // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.3.2.1.1
                int offset = OFFSET_CODES_BASE[offsetCode];
                if (offsetCode > 0) {
                    offset += peekBits(bitsConsumed, bits, offsetCode);
                    bitsConsumed += offsetCode;
                }

                // Repeat Offsets; prevOffs = {Repeated_Offset1, Repeated_Offset2, Repeated_Offset3}
                // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.5
                if (offsetCode <= 1) {
                    // literals_length == 0 (Literals_Length_Code 0 is the only code for length 0): repeated offsets
                    // are shifted by 1: 1 -> Repeated_Offset2, 2 -> Repeated_Offset3, 3 -> Repeated_Offset1 - 1
                    if (literalsLengthCode == 0) {
                        offset++;
                    }

                    if (offset != 0) {
                        int temp;
                        if (offset == 3) {
                            // Repeated_Offset1 - 1_byte
                            temp = prevOffs[0] - 1;
                        } else {
                            temp = prevOffs[offset];
                        }

                        // offset 0 is invalid (corrupted data); the same guard as in the reference implementation
                        if (temp == 0) {
                            temp = 1;
                        }

                        // update the offset history:
                        // offset == 1 (Repeated_Offset2): swap Repeated_Offset1 and Repeated_Offset2
                        // offset == 2 (Repeated_Offset3): rotate, i.e. Repeated_Offset3 becomes the first one
                        // offset == 3 (Repeated_Offset1 - 1): not a repeat offset; shift all back and push new value
                        if (offset != 1) {
                            prevOffs[2] = prevOffs[1];
                        }
                        prevOffs[1] = prevOffs[0];
                        prevOffs[0] = temp;

                        offset = temp;
                    } else {
                        // Repeated_Offset1 is used; the offset history does not change
                        offset = prevOffs[0];
                    }
                } else {
                    // Offset_Value > 3 is not a repeat offset: Repeated_Offsets are shifted back one and
                    // Repeated_Offset1 takes the value of the offset that was just used
                    prevOffs[2] = prevOffs[1];
                    prevOffs[1] = prevOffs[0];
                    prevOffs[0] = offset;
                }

                // Match_Length = Baseline + readNBits(Number_of_Bits); codes 0-31 have no additional bits (Table 17)
                // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.3.2.1.1
                int matchLength = MATCH_LENGTH_BASE[matchLengthCode];
                if (matchLengthCode > 31) {
                    matchLength += peekBits(bitsConsumed, bits, matchLengthBits);
                    bitsConsumed += matchLengthBits;
                }

                // not a part of the spec: reload the bit container if there could be not enough bits left for the
                // Literals_Length bits (up to 16) and the state updates below (up to 9 + 9 + 8 bits). After a reload
                // at least 64 - 7 bits are available, and 16 + 26 < 57. Offset + Match_Length bits read above are at
                // most 28 + 16 = 44 < 57, so they always fit. The same as in the reference implementation.
                int totalBits = literalsLengthBits + matchLengthBits + offsetCode;
                if (totalBits > 64 - 7 - (LITERAL_LENGTH_TABLE_LOG + MATCH_LENGTH_TABLE_LOG + OFFSET_TABLE_LOG)) {
                    BackwardBitInputDecorator bbid2 = new BackwardBitInputDecorator(bbis, bits, bitsConsumed);
                    bitsConsumed = bbid2.getBitsConsumed();
                    bits = bbid2.getBits();
                }

                // Literals_Length = Baseline + readNBits(Number_of_Bits); codes 0-15 have no additional bits (Table 16)
                // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.3.2.1.1
                int literalsLength = LITERALS_LENGTH_BASE[literalsLengthCode];
                if (literalsLengthCode > 15) {
                    literalsLength += peekBits(bitsConsumed, bits, literalsLengthBits);
                    bitsConsumed += literalsLengthBits;
                }

                // update FSE states in order: Literals_Length_State, Match_Length_State, Offset_State
                // newState = Baseline (newStates[state]) + readNBits(Number_of_Bits)
                // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.3.2.1.2
                // https://www.rfc-editor.org/rfc/rfc8878.html#section-4.1
                // NOTE: the spec does it only if it is not the last sequence; here states are updated always (values
                // are not used after the last sequence), and it is not verified that the bitstream is fully consumed
                int numberOfBits;

                numberOfBits = literalsLengthNumbersOfBits[literalsLengthState];
                literalsLengthState = (int) (literalsLengthNewStates[literalsLengthState]
                        + peekBits(bitsConsumed, bits, numberOfBits)); // <= 9 bits
                bitsConsumed += numberOfBits;

                numberOfBits = matchLengthNumbersOfBits[matchLengthState];
                matchLengthState = (int) (matchLengthNewStates[matchLengthState]
                        + peekBits(bitsConsumed, bits, numberOfBits)); // <= 9 bits
                bitsConsumed += numberOfBits;

                numberOfBits = offsetCodesNumbersOfBits[offsetCodesState];
                offsetCodesState = (int) (offsetCodesNewStates[offsetCodesState]
                        + peekBits(bitsConsumed, bits, numberOfBits)); // <= 8 bits
                bitsConsumed += numberOfBits;

                // Sequence Execution: (literals_length, offset, match_length)
                // 1. copy literals_length bytes from the decoded literals to the output
                // 2. copy match_length bytes from 'offset' bytes back (counted from the position after literals);
                //    the source may overlap the destination when offset < match_length
                // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.4
                final int literalOutputLimit = curOutOffs + literalsLength;
                final int matchOutputLimit = literalOutputLimit + matchLength;

                int literalEnd = literalsInput + literalsLength;
                verify(literalEnd <= literalsLimit, curInOffs, "Input is corrupted");

                // NOTE: offset is checked only against the start of the output buffer, not against Window_Size
                // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.1.1
                int matchAddress = literalOutputLimit - offset;
                verify(matchAddress >= 0, curInOffs, "Input is corrupted");

                if (literalOutputLimit > fastOutputLimit) {
                    // close to the end of the output buffer: safe (byte by byte) copy
                    executeLastSequence(out,
                                        curOutOffs,
                                        literalOutputLimit,
                                        matchOutputLimit,
                                        fastOutputLimit,
                                        literalsInput,
                                        matchAddress);
                } else {
                    // copy literals. literalOutputLimit <= fastOutputLimit, so we can copy
                    // long at a time with over-copy
                    curOutOffs = copyLiterals(out, literalsBase, curOutOffs, literalsInput, literalOutputLimit);
                    copyMatch(out,
                              fastOutputLimit,
                              curOutOffs,
                              offset,
                              matchOutputLimit,
                              matchAddress,
                              matchLength,
                              fastMatchOutputLimit);
                }
                curOutOffs = matchOutputLimit;
                literalsInput = literalEnd;
            }
        }

        // when all sequences are decoded, the literals left in the Literals_Section are added at the end of the block
        // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.3.2
        // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.4
        curOutOffs = copyLastLiteral(out.buf, literalsBase, literalsLimit, curOutOffs, literalsInput);
        return curOutOffs - startOutOffs;
    }

    private static int copyLastLiteral(byte[] out,
                                       byte[] in,
                                       int literalsLimit,
                                       int oufOffs,
                                       int inOffs) {
        int lastLiteralsSize = literalsLimit - inOffs;
        System.arraycopy(in, inOffs, out, oufOffs, lastLiteralsSize);
        oufOffs += lastLiteralsSize;
        return oufOffs;
    }

    private static void copyMatch(ByteArrayWithOffs out,
                                  long fastOutputLimit,
                                  int outOffs,
                                  int offset,
                                  long matchOutputLimit,
                                  int matchAddress,
                                  int matchLength,
                                  long fastMatchOutputLimit) {
        matchAddress = copyMatchHead(out, outOffs, offset, matchAddress);
        outOffs += SIZE_OF_LONG;
        matchLength -= SIZE_OF_LONG; // first 8 bytes copied above

        copyMatchTail(out,
                      fastOutputLimit,
                      outOffs,
                      matchOutputLimit,
                      matchAddress,
                      matchLength,
                      fastMatchOutputLimit);
    }

    private static void copyMatchTail(ByteArrayWithOffs out,
                                      long fastOutputLimit,
                                      int outOffs,
                                      long matchOutputLimit,
                                      int matchAddress,
                                      int matchLength,
                                      long fastMatchOutputLimit) {
        // fastMatchOutputLimit is just fastOutputLimit - SIZE_OF_LONG. It needs to be passed in so that it can be computed once for the
        // whole invocation to decompressSequences. Otherwise, we'd just compute it here.
        // If matchOutputLimit is < fastMatchOutputLimit, we know that even after the head (8 bytes) has been copied, the outOffs pointer
        // will be within fastOutputLimit, so it's safe to copy blindly before checking the limit condition
        if (matchOutputLimit < fastMatchOutputLimit) {
            int copied = 0;
            do {
                outOffs += out.putLong(outOffs, out.getLong(matchAddress));
                matchAddress += SIZE_OF_LONG;
                copied += SIZE_OF_LONG;
            }
            while (copied < matchLength);
        } else {
            while (outOffs < fastOutputLimit) {
                outOffs += out.putLong(outOffs, out.getLong(matchAddress));
                matchAddress += SIZE_OF_LONG;
            }

            while (outOffs < matchOutputLimit) {
                outOffs += out.putByte(outOffs, out.getByte(matchAddress++));
            }
        }
    }

    private static int copyMatchHead(ByteArrayWithOffs out, int outOffs, int offset, int matchAddress) {
        // copy match
        if (offset < 8) {
            // 8 bytes apart so that we can copy long-at-a-time below
            int increment32 = DEC_32_TABLE[offset];
            int decrement64 = DEC_64_TABLE[offset];

            outOffs += out.putByte(outOffs, out.getByte(matchAddress));
            outOffs += out.putByte(outOffs, out.getByte(matchAddress + 1));
            outOffs += out.putByte(outOffs, out.getByte(matchAddress + 2));
            outOffs += out.putByte(outOffs, out.getByte(matchAddress + 3));

            matchAddress += increment32;

            out.putInt(outOffs, out.getInt(matchAddress));
            matchAddress -= decrement64;
        } else {
            matchAddress += out.putLong(outOffs, out.getLong(matchAddress));
        }

        return matchAddress;
    }

    private static int copyLiterals(ByteArrayWithOffs out,
                                    byte[] literalsBase,
                                    int output,
                                    int literalsInput,
                                    int literalOutputLimit) {
        int literalInput = literalsInput;
        do {
            output += out.putLong(output, new ByteArrayWithOffs(literalsBase).getLong(literalInput));
            literalInput += SIZE_OF_LONG;
        }
        while (output < literalOutputLimit);
        output = literalOutputLimit; // correction in case we over-copied
        return output;
    }

    private void computeMatchLengthTable(ByteArrayWithOffs in, int matchLengthType) {
        final int offs = in.getOffs();

        if (matchLengthType == SEQUENCE_ENCODING_RLE) {
            int value = in.getByte();
            verify(value <= MAX_MATCH_LENGTH_SYMBOL, offs, "Value exceeds expected maximum value");

            matchLengthTable.init(value);
            currentMatchLengthTable = matchLengthTable;
        } else if (matchLengthType == SEQUENCE_ENCODING_BASIC)
            currentMatchLengthTable = DEFAULT_MATCH_LENGTH_TABLE;
        else if (matchLengthType == SEQUENCE_ENCODING_REPEAT)
            verify(currentMatchLengthTable != null, offs, "Expected match length table to be present");
        else if (matchLengthType == SEQUENCE_ENCODING_COMPRESSED) {
            new FseTableReader(matchLengthTable, MATCH_LENGTH_TABLE_LOG)
                    .readFseTable(in, in.getInputLimit() - in.getOffs(), MAX_MATCH_LENGTH_SYMBOL);
            currentMatchLengthTable = matchLengthTable;
        } else
            throw fail(offs, "Invalid match length encoding type");
    }

    private void computeOffsetsTable(ByteArrayWithOffs in, int offsetCodesType) {
        final int offs = in.getOffs();

        if (offsetCodesType == SEQUENCE_ENCODING_RLE) {
            int value = in.getByte();
            verify(value <= DEFAULT_MAX_OFFSET_CODE_SYMBOL, offs, "Value exceeds expected maximum value");
            offsetCodesTable.init(value);
            currentOffsetCodesTable = offsetCodesTable;
        } else if (offsetCodesType == SEQUENCE_ENCODING_BASIC)
            currentOffsetCodesTable = DEFAULT_OFFSET_CODES_TABLE;
        else if (offsetCodesType == SEQUENCE_ENCODING_REPEAT)
            verify(currentOffsetCodesTable != null, offs, "Expected match length table to be present");
        else if (offsetCodesType == SEQUENCE_ENCODING_COMPRESSED) {
            new FseTableReader(offsetCodesTable, OFFSET_TABLE_LOG)
                    .readFseTable(in, in.getInputLimit() - in.getOffs(), DEFAULT_MAX_OFFSET_CODE_SYMBOL);
            currentOffsetCodesTable = offsetCodesTable;
        } else
            throw fail(offs, "Invalid offset code encoding type");
    }

    private void computeLiteralsTable(ByteArrayWithOffs in, int literalsLengthType) {
        final int offs = in.getOffs();

        if (literalsLengthType == SEQUENCE_ENCODING_RLE) {
            byte value = (byte) in.getByte();
            literalsLengthTable.init(value);
            currentLiteralsLengthTable = literalsLengthTable;
        } else if (literalsLengthType == SEQUENCE_ENCODING_BASIC)
            currentLiteralsLengthTable = DEFAULT_LITERALS_LENGTH_TABLE;
        else if (literalsLengthType == SEQUENCE_ENCODING_REPEAT)
            verify(currentLiteralsLengthTable != null, offs, "Expected match length table to be present");
        else if (literalsLengthType == SEQUENCE_ENCODING_COMPRESSED) {
            new FseTableReader(literalsLengthTable, LITERAL_LENGTH_TABLE_LOG)
                    .readFseTable(in, in.getInputLimit() - in.getOffs(), MAX_LITERALS_LENGTH_SYMBOL);
            currentLiteralsLengthTable = literalsLengthTable;
        } else
            throw fail(offs, "Invalid literals length encoding type");
    }

    private void executeLastSequence(ByteArrayWithOffs out,
                                     int outOffs,
                                     long literalOutputLimit,
                                     long matchOutputLimit,
                                     int fastOutputLimit,
                                     int literalInput,
                                     int matchAddress) {
        // copy literals
        if (outOffs < fastOutputLimit) {
            // wild copy
            do {
                outOffs += out.putLong(outOffs, new ByteArrayWithOffs(literalsBase).getLong(literalInput));
                literalInput += SIZE_OF_LONG;
            }
            while (outOffs < fastOutputLimit);

            literalInput -= outOffs - fastOutputLimit;
            outOffs = fastOutputLimit;
        }

        while (outOffs < literalOutputLimit) {
            outOffs += out.putByte(outOffs, literalsBase[literalInput]);
            literalInput++;
        }

        // copy match
        while (outOffs < matchOutputLimit) {
            outOffs += out.putByte(outOffs, out.getByte(matchAddress));
            matchAddress++;
        }
    }

    @RequiredArgsConstructor
    private static final class SizeData {

        private final int regeneratedSize;
        private final int compressedSize;

    }

    private void decodeCompressedLiteralsBlock(int b1, int literalsBlockType) {
        assert literalsBlockType == COMPRESSED_LITERALS_BLOCK ||
                literalsBlockType == TREELESS_LITERALS_BLOCK;

        int sizeFormat = (b1 >> 2) & 0b11;

        SizeData sizeData;
        boolean singleStream = false;

        if (sizeFormat == 0b00)
            singleStream = true;

        if (sizeFormat == 0b00 || sizeFormat == 0b01) {
            // sizeFormat - 1bits
            // regeneratedSize - 5bits
            int b2 = in.getByte();
            int b3 = in.getByte();
            int header = b3 << 16 | b2 << 8 | b1;
            int regeneratedSize = (header >> 4) & 0b11_11111111;
            int compressedSize = (header >> 14) & 0b11_11111111;
            sizeData = new SizeData(regeneratedSize, compressedSize);
        } else if (sizeFormat == 0b10) {
            int b2 = in.getByte();
            int b3 = in.getByte();
            int b4 = in.getByte();
            int header = b4 << 24 | b3 << 16 | b2 << 8 | b1;
            int regeneratedSize = (header >>> 4) & 0b111111_11111111;
            int compressedSize = (header >>> 18) & 0b111111_11111111;
            sizeData = new SizeData(regeneratedSize, compressedSize);
        } else {    // sizeFormat == 0b11
            long b2 = in.getByte();
            long b3 = in.getByte();
            long b4 = in.getByte();
            long b5 = in.getByte();
            long header = b5 << 32 | b4 << 24 | b3 << 16 | b2 << 8 | b1;
            int regeneratedSize = (int) ((header >> 4) & 0b11_11111111_11111111);
            int compressedSize = (int) ((header >> 22) & 0b11_11111111_11111111);
            sizeData = new SizeData(regeneratedSize, compressedSize);
        }

        int inputLimit = in.getOffs() + sizeData.compressedSize;

        // 3.1.1.3.1.5. Huffman_Tree_Description
        if (literalsBlockType != TREELESS_LITERALS_BLOCK)
            huffman.readTable(in);

        literalsBase = literals;
        literalsAddress = 0;
        literalsLimit = sizeData.regeneratedSize;

        ByteArrayWithOffs out = new ByteArrayWithOffs(literals);

        if (singleStream) {
            huffman.decodeSingleStream(in, inputLimit, out, literalsAddress, literalsLimit);
        } else
            huffman.decode4Streams(in, inputLimit, out, literalsAddress, literalsLimit);
    }

    private void decodeRleLiteralsBlock(int b1) {
        int regeneratedSize = getRegeneratedSizeForRawOrRleLiteralsBlock(b1);

        byte b = (byte) in.getByte();
        byte[] buf = new byte[regeneratedSize];
        Arrays.fill(buf, b);
        literalsWithOffs = new ByteArrayWithOffs(buf);

        Arrays.fill(literals, 0, regeneratedSize + SIZE_OF_LONG, b);

        literalsBase = literals;
        literalsAddress = 0;
        literalsLimit = regeneratedSize;
    }

    private void decodeRawLiteralsBlock(int b1) {
        int regeneratedSize = getRegeneratedSizeForRawOrRleLiteralsBlock(b1);
        int input = in.getOffs();

        literalsWithOffs = new ByteArrayWithOffs(new byte[regeneratedSize]);
        in.copyMemory(literalsWithOffs, regeneratedSize);

        // Set literals pointer to [input, regeneratedSize], but only if we can copy 8 bytes at a time during sequence decoding
        // Otherwise, copy literals into buffer that's big enough to guarantee that
        if (regeneratedSize > in.getInputLimit() - input - SIZE_OF_LONG) {
            literalsBase = literals;
            literalsAddress = 0;
            literalsLimit = regeneratedSize;

            System.arraycopy(in.buf, input, literals, 0, regeneratedSize);
            Arrays.fill(literals, regeneratedSize, regeneratedSize + SIZE_OF_LONG, (byte) 0);
        } else {
            literalsBase = in.buf;
            literalsAddress = input;
            literalsLimit = literalsAddress + regeneratedSize;
        }
    }

    private int getRegeneratedSizeForRawOrRleLiteralsBlock(int b1) {
        int sizeFormat = (b1 >> 2) & 0b11;

        if (sizeFormat == 0b00 || sizeFormat == 0b10)
            // sizeFormat - 1bits
            // regeneratedSize - 5bits
            return b1 >> 3;

        if (sizeFormat == 0b01) {
            // sizeFormat - 2bits
            // regeneratedSize - 12 bits
            int b2 = in.getByte();
            return (b2 << 8 | b1) >> 4;
        }

        // sizeFormat = 0b11
        // sizeFormat - 2bits
        // regeneratedSize - 20bits
        int b2 = in.getByte();
        int b3 = in.getByte();
        return (b3 << 16 | b2 << 8 | b1) >> 4;
    }

    private FrameHeader readFrameHeader() {
        /*
         * Frame_Header_Descriptor
         * [Window_Descriptor]
         * [Dictionary_ID]
         * [Frame_Content_Size]
         */

        int frameHeaderDescriptor = in.getByte();

        int windowSize = readWindowDescriptorAndGetWindowSize(frameHeaderDescriptor);
        long dictionaryId = readDictionaryId(frameHeaderDescriptor);
        long frameContentSize = readFrameContentSize(frameHeaderDescriptor);
        // if Single_Segment_flag == true => windowSize = frameContentSize
        boolean hasChecksum = BitUtils.isBitSet(frameHeaderDescriptor, BitUtils.BIT2);
        return new FrameHeader(windowSize, frameContentSize, dictionaryId, hasChecksum);
    }

    private int readWindowDescriptorAndGetWindowSize(int frameHeaderDescriptor) {
        // bit5 - Single_Segment_flag
        boolean singleSegment = BitUtils.isBitSet(frameHeaderDescriptor, BitUtils.BIT5);

        // singleSegment == true => windowSize == Frame_Content_Size
        if (singleSegment)
            return -1;

        int windowDescriptor = in.getByte();
        // bit7_3 - Exponent
        int exponent = (windowDescriptor >> 3) & 0b11111;
        // bit2-0 - Mantissa
        int mantissa = windowDescriptor & 0b111;

        int base = 1 << (MIN_WINDOW_LOG + exponent);
        return base + (base / 8) * mantissa;
    }

    private long readDictionaryId(int frameHeaderDescriptor) {
        // bit1_0 - Dictionary_ID_flag
        int dictionaryIdFlag = frameHeaderDescriptor & 0b11;

        if (dictionaryIdFlag == 0)
            return 0;
        if (dictionaryIdFlag == 1)
            return in.getByte();
        if (dictionaryIdFlag == 2)
            return in.getShort() & 0xFFFF;
        // dictionaryIdFlag == 3
        return in.getInt() & 0xFFFF_FFFFL;
    }

    private long readFrameContentSize(int frameHeaderDescriptor) {
        // bit7_6 - Frame_Content_Size_flag
        int frameContentSizeFlag = (frameHeaderDescriptor >> 6) & 0b11;
        // bit5 - Single_Segment_flag
        boolean singleSegment = BitUtils.isBitSet(frameHeaderDescriptor, BitUtils.BIT5);

        if (frameContentSizeFlag == 0)
            return singleSegment ? in.getByte() : 0;
        if (frameContentSizeFlag == 1)
            return (in.getShort() & 0xFFFF) + 256;
        if (frameContentSizeFlag == 2)
            return in.getInt() & 0xFFFF_FFFFL;
        // frameContentSizeFlag == e
        return in.getLong();
    }

    private void verifyMagic() {
        final int lo = in.getOffs();
        int magic = in.getInt();
        if (magic != MAGIC_NUMBER) {
            if (magic == V07_MAGIC_NUMBER) {
                throw new MalformedInputException(lo, "Data encoded in unsupported ZSTD v0.7 format");
            }
            throw new MalformedInputException(lo, "Invalid magic prefix: " + Integer.toHexString(magic));
        }
    }

}
