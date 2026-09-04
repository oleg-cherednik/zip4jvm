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
import io.airlift.compress.zstd.BitInputStream;
import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.Constants;
import io.airlift.compress.zstd.FrameHeader;
import io.airlift.compress.zstd.LiteralsSectionHeader;
import io.airlift.compress.zstd.fse.FiniteStateEntropy;
import io.airlift.compress.zstd.fse.FseTableReader;
import io.airlift.compress.zstd.huffman.Huffman;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.airlift.compress.zstd.BitInputStream.peekBits;
import static io.airlift.compress.zstd.Constants.COMPRESSED_BLOCK;
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
import static io.airlift.compress.zstd.Constants.SIZE_OF_INT;
import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;
import static io.airlift.compress.zstd.Constants.TREELESS_LITERALS_BLOCK;
import static io.airlift.compress.zstd.Util.fail;
import static io.airlift.compress.zstd.Util.mask;
import static io.airlift.compress.zstd.Util.verify;

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
    private int inOffs;

    private final byte[] literals = new byte[MAX_BLOCK_SIZE +
            SIZE_OF_LONG]; // extra space to allow for long-at-a-time copy

    // current buffer containing literals
    private byte[] literalsBase;
    private int literalsAddress;
    private int literalsLimit;

    private final int[] previousOffsets = new int[3];

    private final FiniteStateEntropy.Table literalsLengthTable = new FiniteStateEntropy.Table(LITERAL_LENGTH_TABLE_LOG);
    private final FiniteStateEntropy.Table offsetCodesTable = new FiniteStateEntropy.Table(OFFSET_TABLE_LOG);
    private final FiniteStateEntropy.Table matchLengthTable = new FiniteStateEntropy.Table(MATCH_LENGTH_TABLE_LOG);

    private FiniteStateEntropy.Table currentLiteralsLengthTable;
    private FiniteStateEntropy.Table currentOffsetCodesTable;
    private FiniteStateEntropy.Table currentMatchLengthTable;

    private final Huffman huffman = new Huffman();
    private final FseTableReader fse = new FseTableReader();

    public int decompress(ByteArrayWithOffs out) {
        if (in.buf.length == 0) {
            return 0;
        }

        int outOffs = 0;

        while (inOffs < in.buf.length) {
            reset();
            outOffs = readFrame(out, outOffs);
        }

        return outOffs;
    }

    private int readFrame(ByteArrayWithOffs out, int outOffs) {
        /*
         * Magic_Number
         * Frame_Header
         * Data_Block
         * [More data blocks]
         * [Content_Checksum]
         */
        int outputStart = outOffs;

        verifyMagic();
        FrameHeader frameHeader = readFrameHeader();
        inOffs = in.getOffs();

        AtomicBoolean lastBlock = new AtomicBoolean(false);

        do {
            outOffs += readDataBlock(out, outOffs, lastBlock);
            in.setOffs(inOffs);
        }
        while (!lastBlock.get());

        if (frameHeader.isHasChecksum()) {
            int decodedFrameSize = outOffs - outputStart;

            long hash = XxHash64.hash(0, out, outputStart, decodedFrameSize);

            int checksum = in.getInt(inOffs);
            if (checksum != (int) hash) {
                throw new MalformedInputException(inOffs,
                                                  String.format("Bad checksum. Expected: %s, actual: %s",
                                                                Integer.toHexString(checksum),
                                                                Integer.toHexString((int) hash)));
            }

            inOffs += SIZE_OF_INT;
        }

        return outOffs;
    }

    private int readDataBlock(ByteArrayWithOffs out, int outOffs, AtomicBoolean lastBlock) {
        /*
         * Block_Header (3 bytes)
         * Block_Content (n bytes)
         */
        // read block blockHeader
        int b0 = in.getByte();
        int b1 = in.getByte();
        int b2 = in.getByte();
        int blockHeader = b2 << 16 | b1 << 8 | b0;
        inOffs = in.getOffs();

        lastBlock.set(isLastBlock(blockHeader));
        int blockType = getBlockType(blockHeader);
        int blockSize = getBlockSize(blockHeader);

        if (blockType == RAW_BLOCK) {
            // this is an uncompressed block. Block_Content contains Block_Size bytes
            decodeRawBlock(out, outOffs, blockSize);
            inOffs += blockSize;
            return blockSize;
        }

        if (blockType == RLE_BLOCK) {
            /*
             * this is a single byte, repeated Block_Size times. Block_Content
             * consists of a single byte. On the decompression side, this byte
             * must be repeated Block_Size times
             */
            int decodedSize = decodeRleBlock(in, out, outOffs, blockSize);
            inOffs += Constants.SIZE_OF_BYTE;
            return decodedSize;
        }

        if (blockType == COMPRESSED_BLOCK) {
            /*
             * this is a Zstandard compressed block, explained later on.
             * Block_Size is the length of Block_Content, the compressed data.
             * The decompressed size is not known, but its maximum possible
             * value is guaranteed
             */
            int decodedSize = decodeCompressedBlock(in, out, outOffs, blockSize);
            inOffs += blockSize;
            return decodedSize;
        }

        throw fail(inOffs, "Invalid block type");
    }

    private static boolean isLastBlock(int blockHeader) {
        // bit0
        return BitUtils.isBitSet(blockHeader, BitUtils.BIT0);
    }

    private static int getBlockType(int blockHeader) {
        // bit1_2
        return (blockHeader >> 1) & 0b11;
    }

    private static int getBlockSize(int blockHeader) {
        // bit3_23
        return (blockHeader >> 3) & 0x1F_FFFF; // 21 bits
    }

    private void reset() {
        previousOffsets[0] = 1;
        previousOffsets[1] = 4;
        previousOffsets[2] = 8;

        currentLiteralsLengthTable = null;
        currentOffsetCodesTable = null;
        currentMatchLengthTable = null;
    }

    private void decodeRawBlock(ByteArrayWithOffs out, int outOffs, int blockSize) {
        in.copyMemory(out.buf, outOffs, blockSize);
    }

    private static int decodeRleBlock(ByteArrayWithOffs in, ByteArrayWithOffs out, int outOffs, int size) {
        int output = outOffs;
        long value = in.getByte();

        int remaining = size;
        if (remaining >= SIZE_OF_LONG) {
            long packed = value
                    | (value << 8)
                    | (value << 16)
                    | (value << 24)
                    | (value << 32)
                    | (value << 40)
                    | (value << 48)
                    | (value << 56);

            do {
                output += out.putLong(output, packed);
                remaining -= SIZE_OF_LONG;
            }
            while (remaining >= SIZE_OF_LONG);
        }

        for (int i = 0; i < remaining; i++) {
            output += out.putByte(output, (byte) value);
        }

        return size;
    }

    private int decodeCompressedBlock(ByteArrayWithOffs in,
                                      ByteArrayWithOffs out, int outOffs,
                                      int blockSize) {
        final int startInOffs = in.getOffs();
        long inputLimit = in.getOffs() + blockSize;
        int offs = in.getOffs();

//        LiteralsSectionHeader literalsSectionHeader = readLiteralsSectionHeader(in);

        // decode literals
        int b1 = in.getByte();
        int literalsBlockType = b1 & 0b11;

        if (literalsBlockType == RAW_LITERALS_BLOCK)
            offs += decodeRawLiterals(in, offs, inputLimit);
        else if (literalsBlockType == RLE_LITERALS_BLOCK)
            offs += decodeRleLiterals(in, offs);
        else {
            if (literalsBlockType == TREELESS_LITERALS_BLOCK)
                verify(huffman.isLoaded(), offs, "Dictionary is corrupted");

            offs += decodeCompressedLiterals(in, b1, literalsBlockType);
        }

        in.setOffs(offs);
        return decompressSequences(
                in, startInOffs + blockSize,
                out, outOffs);
    }

    private LiteralsSectionHeader readLiteralsSectionHeader(ByteArrayWithOffs in) {
        int b1 = in.getByte();

        // bit1_0 - Literals_Block_Type
        int literalsBlockType = b1 & 0b11;

        if (literalsBlockType == RAW_LITERALS_BLOCK
                || literalsBlockType == RLE_LITERALS_BLOCK) {
            int type = (b1 >> 2) & 0b11;


        } else {
            // COMPRESSED_LITERALS_BLOCK || TREELESS_LITERALS_BLOCK

        }

        return new LiteralsSectionHeader();
    }

    private int decompressSequences(
            ByteArrayWithOffs in, final int inputLimit,
            ByteArrayWithOffs out, final int outOffs) {
        final int fastOutputLimit = out.buf.length - SIZE_OF_LONG;
        final long fastMatchOutputLimit = fastOutputLimit - SIZE_OF_LONG;

        int curInOffs = in.getOffs();
        int curOutOffs = outOffs;

        int literalsInput = literalsAddress;

        // decode header
        int sequenceCount = in.getByte();
        if (sequenceCount != 0) {
            if (sequenceCount == 255)
                sequenceCount = in.getShort() + LONG_NUMBER_OF_SEQUENCES;
            else if (sequenceCount > 127)
                sequenceCount = ((sequenceCount - 128) << 8) + in.getByte();

            int type = in.getByte();
            int literalsLengthType = (type & 0xFF) >>> 6;
            int offsetCodesType = (type >>> 4) & 0b11;
            int matchLengthType = (type >>> 2) & 0b11;

            computeLiteralsTable(literalsLengthType, in, inputLimit);
            computeOffsetsTable(offsetCodesType, in, inputLimit);
            computeMatchLengthTable(matchLengthType, in, inputLimit);

            // decompress sequences
            BitInputStream.Initializer initializer = new BitInputStream.Initializer(in, in.getOffs(), inputLimit);
            initializer.initialize();
            int bitsConsumed = initializer.getBitsConsumed();
            long bits = initializer.getBits();
            int curOffs = initializer.getCurOffs();

            FiniteStateEntropy.Table currentLiteralsLengthTable = this.currentLiteralsLengthTable;
            FiniteStateEntropy.Table currentOffsetCodesTable = this.currentOffsetCodesTable;
            FiniteStateEntropy.Table currentMatchLengthTable = this.currentMatchLengthTable;

            int literalsLengthState = (int) peekBits(bitsConsumed, bits, currentLiteralsLengthTable.log2Size);
            bitsConsumed += currentLiteralsLengthTable.log2Size;

            int offsetCodesState = (int) peekBits(bitsConsumed, bits, currentOffsetCodesTable.log2Size);
            bitsConsumed += currentOffsetCodesTable.log2Size;

            int matchLengthState = (int) peekBits(bitsConsumed, bits, currentMatchLengthTable.log2Size);
            bitsConsumed += currentMatchLengthTable.log2Size;

            int[] previousOffsets = this.previousOffsets;

            byte[] literalsLengthNumbersOfBits = currentLiteralsLengthTable.numberOfBits;
            int[] literalsLengthNewStates = currentLiteralsLengthTable.newState;
            byte[] literalsLengthSymbols = currentLiteralsLengthTable.symbol;

            byte[] matchLengthNumbersOfBits = currentMatchLengthTable.numberOfBits;
            int[] matchLengthNewStates = currentMatchLengthTable.newState;
            byte[] matchLengthSymbols = currentMatchLengthTable.symbol;

            byte[] offsetCodesNumbersOfBits = currentOffsetCodesTable.numberOfBits;
            int[] offsetCodesNewStates = currentOffsetCodesTable.newState;
            byte[] offsetCodesSymbols = currentOffsetCodesTable.symbol;

            while (sequenceCount > 0) {
                sequenceCount--;

                BitInputStream.Loader loader = new BitInputStream.Loader(in, curInOffs, curOffs, bits, bitsConsumed);
                loader.load();
                bitsConsumed = loader.getBitsConsumed();
                bits = loader.getBits();
                curOffs = loader.getCurOffs();
                if (loader.isOverflow()) {
                    verify(sequenceCount == 0, curInOffs, "Not all sequences were consumed");
                    break;
                }

                // decode sequence
                int literalsLengthCode = literalsLengthSymbols[literalsLengthState];
                int matchLengthCode = matchLengthSymbols[matchLengthState];
                int offsetCode = offsetCodesSymbols[offsetCodesState];

                int literalsLengthBits = LITERALS_LENGTH_BITS[literalsLengthCode];
                int matchLengthBits = MATCH_LENGTH_BITS[matchLengthCode];

                int offset = OFFSET_CODES_BASE[offsetCode];
                if (offsetCode > 0) {
                    offset += peekBits(bitsConsumed, bits, offsetCode);
                    bitsConsumed += offsetCode;
                }

                if (offsetCode <= 1) {
                    if (literalsLengthCode == 0) {
                        offset++;
                    }

                    if (offset != 0) {
                        int temp;
                        if (offset == 3) {
                            temp = previousOffsets[0] - 1;
                        } else {
                            temp = previousOffsets[offset];
                        }

                        if (temp == 0) {
                            temp = 1;
                        }

                        if (offset != 1) {
                            previousOffsets[2] = previousOffsets[1];
                        }
                        previousOffsets[1] = previousOffsets[0];
                        previousOffsets[0] = temp;

                        offset = temp;
                    } else {
                        offset = previousOffsets[0];
                    }
                } else {
                    previousOffsets[2] = previousOffsets[1];
                    previousOffsets[1] = previousOffsets[0];
                    previousOffsets[0] = offset;
                }

                int matchLength = MATCH_LENGTH_BASE[matchLengthCode];
                if (matchLengthCode > 31) {
                    matchLength += peekBits(bitsConsumed, bits, matchLengthBits);
                    bitsConsumed += matchLengthBits;
                }

                int literalsLength = LITERALS_LENGTH_BASE[literalsLengthCode];
                if (literalsLengthCode > 15) {
                    literalsLength += peekBits(bitsConsumed, bits, literalsLengthBits);
                    bitsConsumed += literalsLengthBits;
                }

                int totalBits = literalsLengthBits + matchLengthBits + offsetCode;
                if (totalBits > 64 - 7 - (LITERAL_LENGTH_TABLE_LOG + MATCH_LENGTH_TABLE_LOG + OFFSET_TABLE_LOG)) {
                    BitInputStream.Loader loader1 = new BitInputStream.Loader(in,
                                                                              curInOffs,
                                                                              curOffs,
                                                                              bits,
                                                                              bitsConsumed);
                    loader1.load();

                    bitsConsumed = loader1.getBitsConsumed();
                    bits = loader1.getBits();
                    curOffs = loader1.getCurOffs();
                }

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

                final int literalOutputLimit = curOutOffs + literalsLength;
                final int matchOutputLimit = literalOutputLimit + matchLength;

                int literalEnd = literalsInput + literalsLength;
                verify(literalEnd <= literalsLimit, curInOffs, "Input is corrupted");

                int matchAddress = literalOutputLimit - offset;
                verify(matchAddress >= 0, curInOffs, "Input is corrupted");

                if (literalOutputLimit > fastOutputLimit) {
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

        // last literal segment
        curOutOffs = copyLastLiteral(out.buf, literalsBase, literalsLimit, curOutOffs, literalsInput);

        return curOutOffs - outOffs;
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
                outOffs += SIZE_OF_LONG;
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

    private void computeMatchLengthTable(int matchLengthType, ByteArrayWithOffs in, int inputLimit) {
        int offs = in.getOffs();

        if (matchLengthType == SEQUENCE_ENCODING_RLE) {
            byte value = in.getByte(offs++);
            verify(value <= MAX_MATCH_LENGTH_SYMBOL, offs, "Value exceeds expected maximum value");

            FseTableReader.initializeRleTable(matchLengthTable, value);
            currentMatchLengthTable = matchLengthTable;
        } else if (matchLengthType == SEQUENCE_ENCODING_BASIC)
            currentMatchLengthTable = DEFAULT_MATCH_LENGTH_TABLE;
        else if (matchLengthType == SEQUENCE_ENCODING_REPEAT)
            verify(currentMatchLengthTable != null, offs, "Expected match length table to be present");
        else if (matchLengthType == SEQUENCE_ENCODING_COMPRESSED) {
            int read = fse.readFseTable(matchLengthTable,
                                        in,
                                        offs,
                                        inputLimit,
                                        MAX_MATCH_LENGTH_SYMBOL,
                                        MATCH_LENGTH_TABLE_LOG);
            in.setOffs(in.getOffs() + read);
            currentMatchLengthTable = matchLengthTable;
        } else
            throw fail(offs, "Invalid match length encoding type");
    }

    private void computeOffsetsTable(int offsetCodesType, ByteArrayWithOffs in, int inputLimit) {
        final int offs = in.getOffs();

        if (offsetCodesType == SEQUENCE_ENCODING_RLE) {
            byte value = (byte) in.getByte();
            verify(value <= DEFAULT_MAX_OFFSET_CODE_SYMBOL, offs, "Value exceeds expected maximum value");
            FseTableReader.initializeRleTable(offsetCodesTable, value);
            currentOffsetCodesTable = offsetCodesTable;
        } else if (offsetCodesType == SEQUENCE_ENCODING_BASIC)
            currentOffsetCodesTable = DEFAULT_OFFSET_CODES_TABLE;
        else if (offsetCodesType == SEQUENCE_ENCODING_REPEAT)
            verify(currentOffsetCodesTable != null, offs, "Expected match length table to be present");
        else if (offsetCodesType == SEQUENCE_ENCODING_COMPRESSED) {
            int read = fse.readFseTable(offsetCodesTable,
                                        in, in.getOffs(), inputLimit,
                                        DEFAULT_MAX_OFFSET_CODE_SYMBOL,
                                        OFFSET_TABLE_LOG);
            in.setOffs(in.getOffs() + read);
            currentOffsetCodesTable = offsetCodesTable;
        } else
            throw fail(offs, "Invalid offset code encoding type");
    }

    private void computeLiteralsTable(int literalsLengthType, ByteArrayWithOffs in, int inputLimit) {
        final int offs = in.getOffs();

        if (literalsLengthType == SEQUENCE_ENCODING_RLE) {
            byte value = (byte) in.getByte();
            FseTableReader.initializeRleTable(literalsLengthTable, value);
            currentLiteralsLengthTable = literalsLengthTable;
        } else if (literalsLengthType == SEQUENCE_ENCODING_BASIC)
            currentLiteralsLengthTable = DEFAULT_LITERALS_LENGTH_TABLE;
        else if (literalsLengthType == SEQUENCE_ENCODING_REPEAT)
            verify(currentLiteralsLengthTable != null, offs, "Expected match length table to be present");
        else if (literalsLengthType == SEQUENCE_ENCODING_COMPRESSED) {
            int read = fse.readFseTable(literalsLengthTable,
                                        in, in.getOffs(), inputLimit,
                                        MAX_LITERALS_LENGTH_SYMBOL,
                                        LITERAL_LENGTH_TABLE_LOG);
            in.setOffs(in.getOffs() + read);
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

    private int decodeCompressedLiterals(ByteArrayWithOffs in, int b1, int literalsBlockType) {
        // compressed
        int compressedSize;
        int uncompressedSize;
        boolean singleStream = false;
        int headerSize;
        int type = (b1 >> 2) & 0b11;

        if (type == 0b00)
            singleStream = true;

        if (type == 0b00 || type == 0b01) {
            headerSize = 3;
            uncompressedSize = (b1 >>> 4) & mask(10);
            compressedSize = (b1 >>> 14) & mask(10);
        } else if (type == 0b10) {
            int b2 = in.getByte();
            int b3 = in.getByte();
            int b4 = in.getByte();
            int header = b4 << 24 | b3 << 16 | b2 << 8 | b1;

            headerSize = 4;
            uncompressedSize = (header >>> 4) & mask(14);
            compressedSize = (header >>> 18) & mask(14);
        } else {    // type == 0b11
            long hi = in.getInt() & 0xFFFF_FFFFL;
            long header = hi << 8 | b1;

            headerSize = 5;
            uncompressedSize = (int) ((header >>> 4) & mask(18));
            compressedSize = (int) ((header >>> 22) & mask(18));
        }

        int offs = in.getOffs();

        int inputLimit = offs + compressedSize;
        if (literalsBlockType != TREELESS_LITERALS_BLOCK) {
            offs += huffman.readTable(in);
        }

        literalsBase = literals;
        literalsAddress = 0;
        literalsLimit = uncompressedSize;

        if (singleStream) {
            huffman.decodeSingleStream(in,
                                       offs,
                                       inputLimit,
                                       new ByteArrayWithOffs(literals),
                                       literalsAddress,
                                       literalsLimit);
        } else {
            in.setOffs(offs);
            huffman.decode4Streams(in, inputLimit,
                                   new ByteArrayWithOffs(literals), literalsAddress, literalsLimit);
        }

        return headerSize + compressedSize;
    }

    private int decodeRleLiterals(ByteArrayWithOffs in, final int inOffs) {
        int input = inOffs;
        int outputSize;

        int type = (in.getByte(input) >> 2) & 0b11;
        switch (type) {
            case 0:
            case 2:
                outputSize = (in.getByte(input) & 0xFF) >>> 3;
                input++;
                break;
            case 1:
                outputSize = (in.getShort(input) & 0xFFFF) >>> 4;
                input += 2;
                break;
            case 3:
                // we need at least 4 bytes (3 for the header, 1 for the payload)
                outputSize = (in.getInt(input) & 0xFF_FFFF) >>> 4;
                input += 3;
                break;
            default:
                throw fail(input, "Invalid RLE literals header encoding type");
        }

        byte value = in.getByte(input++);
        Arrays.fill(literals, 0, outputSize + SIZE_OF_LONG, value);

        literalsBase = literals;
        literalsAddress = 0;
        literalsLimit = outputSize;

        return input - inOffs;
    }

    private int decodeRawLiterals(ByteArrayWithOffs in, final int inOffs, long inputLimit) {
        int input = inOffs;
        int type = (in.getByte(input) >> 2) & 0b11;

        int literalSize;
        switch (type) {
            case 0b00:
            case 0b10:
                literalSize = (in.getByte(input) & 0xFF) >>> 3;
                input++;
                break;
            case 0b01:
                literalSize = (in.getShort(input) & 0xFFFF) >>> 4;
                input += 2;
                break;
            case 0b11:
                // read 3 little-endian bytes
                int header = (in.getByte(input) & 0xFF) | ((in.getShort(input + 1) & 0xFFFF) << 8);

                literalSize = header >>> 4;
                input += 3;
                break;
            default:
                throw fail(input, "Invalid raw literals header encoding type");
        }

        // Set literals pointer to [input, literalSize], but only if we can copy 8 bytes at a time during sequence decoding
        // Otherwise, copy literals into buffer that's big enough to guarantee that
        if (literalSize > inputLimit - input - SIZE_OF_LONG) {
            literalsBase = literals;
            literalsAddress = 0;
            literalsLimit = literalSize;

            System.arraycopy(in.buf, input, literals, literalsAddress, literalSize);
            Arrays.fill(literals, literalSize, literalSize + SIZE_OF_LONG, (byte) 0);
        } else {
            literalsBase = in.buf;
            literalsAddress = input;
            literalsLimit = literalsAddress + literalSize;
        }
        input += literalSize;

        return input - inOffs;
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
        boolean hasChecksum = getContentChecksumFlag(frameHeaderDescriptor);
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

    private boolean getContentChecksumFlag(int frameHeaderDescriptor) {
        return BitUtils.isBitSet(frameHeaderDescriptor, BitUtils.BIT2);
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
