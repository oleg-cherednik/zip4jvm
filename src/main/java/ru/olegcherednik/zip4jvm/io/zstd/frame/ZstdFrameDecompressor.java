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
package ru.olegcherednik.zip4jvm.io.zstd.frame;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.zstd.Buffer;
import ru.olegcherednik.zip4jvm.io.zstd.UnsafeUtil;
import ru.olegcherednik.zip4jvm.io.zstd.XxHash64;
import ru.olegcherednik.zip4jvm.io.zstd.fse.FiniteStateEntropy;
import ru.olegcherednik.zip4jvm.io.zstd.fse.FseTableReader;
import ru.olegcherednik.zip4jvm.io.zstd.fse.bit.BitInputStream;
import ru.olegcherednik.zip4jvm.io.zstd.huffman.Huffman;

import java.util.Arrays;

import static ru.olegcherednik.zip4jvm.io.zstd.Constants.DEFAULT_MAX_OFFSET_CODE_SYMBOL;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.LITERALS_LENGTH_BITS;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.LITERAL_LENGTH_TABLE_LOG;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.LONG_NUMBER_OF_SEQUENCES;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.MAGIC_NUMBER;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.MATCH_LENGTH_BITS;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.MATCH_LENGTH_TABLE_LOG;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.MAX_BLOCK_SIZE;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.MAX_LITERALS_LENGTH_SYMBOL;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.MAX_MATCH_LENGTH_SYMBOL;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.OFFSET_TABLE_LOG;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SEQUENCE_ENCODING_BASIC;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SEQUENCE_ENCODING_COMPRESSED;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SEQUENCE_ENCODING_REPEAT;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SEQUENCE_ENCODING_RLE;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_INT;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_LONG;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_SHORT;
import static ru.olegcherednik.zip4jvm.io.zstd.Util.fail;
import static ru.olegcherednik.zip4jvm.io.zstd.Util.verify;
import static ru.olegcherednik.zip4jvm.io.zstd.fse.bit.BitInputStream.peekBits;

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
                    0, 16, 32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 32, 0, 0, 32, 0, 32, 0, 32, 0, 0, 32, 0, 32, 0, 32, 0, 0, 16,
                    32, 0, 0, 48, 16, 32, 32, 32,
                    32, 32, 32, 32, 32, 0, 32, 32, 32, 32, 32, 32, 0, 0, 0, 0 },
            new byte[] {
                    0, 0, 1, 3, 4, 6, 7, 9, 10, 12, 14, 16, 18, 19, 21, 22, 24, 25, 26, 27, 29, 31, 0, 1, 2, 4, 5, 7, 8, 10, 11, 13, 16, 17, 19, 20,
                    22, 23, 25, 25, 26, 28, 30, 0,
                    1, 2, 3, 5, 6, 8, 9, 11, 12, 15, 17, 18, 20, 21, 23, 24, 35, 34, 33, 32 },
            new byte[] {
                    4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 6, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 4, 4, 5, 5, 5, 5, 5, 5, 5, 6, 5, 5, 5, 5, 5, 5, 4, 4, 5, 6, 6,
                    4, 4, 5, 5, 5, 5, 5, 5, 5, 5,
                    6, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6 });

    private static final FiniteStateEntropy.Table DEFAULT_OFFSET_CODES_TABLE = new FiniteStateEntropy.Table(
            5,
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0 },
            new byte[] { 0, 6, 9, 15, 21, 3, 7, 12, 18, 23, 5, 8, 14, 20, 2, 7, 11, 17, 22, 4, 8, 13, 19, 1, 6, 10, 16, 28, 27, 26, 25, 24 },
            new byte[] { 5, 4, 5, 5, 5, 5, 4, 5, 5, 5, 5, 4, 5, 5, 5, 4, 5, 5, 5, 5, 4, 5, 5, 5, 4, 5, 5, 5, 5, 5, 5, 5 });

    private static final FiniteStateEntropy.Table DEFAULT_MATCH_LENGTH_TABLE = new FiniteStateEntropy.Table(
            6,
            new int[] {
                    0, 0, 32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 32, 0, 32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 32, 48, 16, 32, 32, 32, 32,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            new byte[] {
                    0, 1, 2, 3, 5, 6, 8, 10, 13, 16, 19, 22, 25, 28, 31, 33, 35, 37, 39, 41, 43, 45, 1, 2, 3, 4, 6, 7, 9, 12, 15, 18, 21, 24, 27, 30,
                    32, 34, 36, 38, 40, 42, 44, 1,
                    1, 2, 4, 5, 7, 8, 11, 14, 17, 20, 23, 26, 29, 52, 51, 50, 49, 48, 47, 46 },
            new byte[] {
                    6, 4, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
                    4, 4, 4, 5, 5, 5, 5, 6, 6, 6,
                    6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6 });

    private final byte[] literals = new byte[MAX_BLOCK_SIZE + SIZE_OF_LONG]; // extra space to allow for long-at-a-time copy
    private final int[] previousOffsets = new int[3];
    private final FiniteStateEntropy.Table literalsLengthTable = new FiniteStateEntropy.Table(LITERAL_LENGTH_TABLE_LOG);
    private final FiniteStateEntropy.Table offsetCodesTable = new FiniteStateEntropy.Table(OFFSET_TABLE_LOG);
    private final FiniteStateEntropy.Table matchLengthTable = new FiniteStateEntropy.Table(MATCH_LENGTH_TABLE_LOG);
    final Huffman huffman = new Huffman();
    private final FseTableReader fse = new FseTableReader();
    // current buffer containing literals
    private byte[] literalsBase;
    private int literalsAddress;
    private int literalsLimit;
    private FiniteStateEntropy.Table currentLiteralsLengthTable;
    private FiniteStateEntropy.Table currentOffsetCodesTable;
    private FiniteStateEntropy.Table currentMatchLengthTable;

    private static int verifyMagic(Buffer inputBase) {
        int magic = (int)inputBase.getInt();
        if (magic != MAGIC_NUMBER) {
            if (magic == V07_MAGIC_NUMBER)
                throw new Zip4jvmException("Data encoded in unsupported ZSTD v0.7 format");
            throw new Zip4jvmException("Invalid magic prefix");
        }

        return SIZE_OF_INT;
    }

    private static int copyMatchHead(byte[] outputBase, int output, int offset, int matchAddress) {
        // copy match
        if (offset < 8) {
            // 8 bytes apart so that we can copy long-at-a-time below
            int increment32 = DEC_32_TABLE[offset];
            int decrement64 = DEC_64_TABLE[offset];

            UnsafeUtil.putByte(outputBase, output, UnsafeUtil.getByte(outputBase, matchAddress));
            UnsafeUtil.putByte(outputBase, output + 1, UnsafeUtil.getByte(outputBase, matchAddress + 1));
            UnsafeUtil.putByte(outputBase, output + 2, UnsafeUtil.getByte(outputBase, matchAddress + 2));
            UnsafeUtil.putByte(outputBase, output + 3, UnsafeUtil.getByte(outputBase, matchAddress + 3));
            matchAddress += increment32;

            UnsafeUtil.putInt(outputBase, output + 4, UnsafeUtil.getInt(outputBase, matchAddress));
            matchAddress -= decrement64;
        } else {
            UnsafeUtil.putLong(outputBase, output, UnsafeUtil.getLong(outputBase, matchAddress));
            matchAddress += SIZE_OF_LONG;
        }
        return matchAddress;
    }

    private static int getLiteralSize(Buffer inputBase, LiteralsSizeFormat sizeFormat) {
        if (sizeFormat == LiteralsSizeFormat.ONE_STREAM_10BITS || sizeFormat == LiteralsSizeFormat.FOUR_STREAMS_10BITS)
            return inputBase.getByte() >>> 3;
        if (sizeFormat == LiteralsSizeFormat.FOUR_STREAMS_14BITS)
            return inputBase.getShort() >>> 4;
        return inputBase.get3Bytes() >>> 4; // sizeFormat == LiteralBlockHeader.SizeFormat.FOUR_STREAMS_18BITS
    }

    public int decompress(Buffer inputBase, byte[] outputBase) {
        int output = 0;
        int outputStart = output;

        verifyMagic(inputBase);

        FrameHeader frameHeader = FrameHeader.read(inputBase);

        while (true) {
            BlockHeader blockHeader = BlockHeader.read(inputBase);
            output += blockHeader.getBlockType().decode(blockHeader.getBlockSize(), inputBase, outputBase, output, this);

            if (blockHeader.isLastBlock())
                break;
        }

        if (frameHeader.isHasChecksum()) {
            int decodedFrameSize = output - outputStart;
            long hash = XxHash64.hash(0, outputBase, outputStart, decodedFrameSize);
            long checksum = inputBase.getInt();

            if (checksum != hash)
                throw new Zip4jvmException(String.format("Bad checksum. Expected: %s, actual: %s",
                        Integer.toHexString((int)checksum), Integer.toHexString((int)hash)));
        }

        return output;
    }

    int decodeCompressedBlock(Buffer inputBase, int blockSize, byte[] outputBase, int outputAddress) {
        final int pos = inputBase.getOffs();
        LiteralsSectionHeader literalsSectionHeader = LiteralsSectionHeader.read(inputBase);
        literalsSectionHeader.getLiteralsBlockType().decode(inputBase, blockSize, literalsSectionHeader, this);
        int written = decompressSequences(inputBase, pos + blockSize, outputBase, outputAddress);
        inputBase.seek(pos + blockSize);
        return written;
    }

    private int decompressSequences(Buffer inputBase, final int inputLimit, final byte[] outputBase, final int outputAddress) {
        final int fastOutputLimit = outputBase.length - SIZE_OF_LONG;
        final long fastMatchOutputLimit = fastOutputLimit - SIZE_OF_LONG;

        final int inputAddress = inputBase.getOffs();
        int input = inputAddress;
        int output = outputAddress;

        int literalsInput = literalsAddress;

        // decode header
        int sequenceCount = UnsafeUtil.getByte(inputBase.getBuf(), input++) & 0xFF;
        if (sequenceCount != 0) {
            if (sequenceCount == 255) {
                sequenceCount = (UnsafeUtil.getShort(inputBase.getBuf(), input) & 0xFFFF) + LONG_NUMBER_OF_SEQUENCES;
                input += SIZE_OF_SHORT;
            } else if (sequenceCount > 127) {
                sequenceCount = ((sequenceCount - 128) << 8) + (UnsafeUtil.getByte(inputBase.getBuf(), input++) & 0xFF);
            }

            byte type = UnsafeUtil.getByte(inputBase.getBuf(), input++);

            int literalsLengthType = (type & 0xFF) >>> 6;
            int offsetCodesType = (type >>> 4) & 0b11;
            int matchLengthType = (type >>> 2) & 0b11;

            input = computeLiteralsTable(literalsLengthType, inputBase.getBuf(), input, inputLimit);
            input = computeOffsetsTable(offsetCodesType, inputBase.getBuf(), input, inputLimit);
            input = computeMatchLengthTable(matchLengthType, inputBase.getBuf(), input, inputLimit);

            // decompress sequences
            BitInputStream.Initializer initializer = new BitInputStream.Initializer(inputBase, input, inputLimit);
            initializer.initialize();
            int bitsConsumed = initializer.getBitsConsumed();
            long bits = initializer.getBits();
            int currentAddress = initializer.getCurrentAddress();

            FiniteStateEntropy.Table currentLiteralsLengthTable = this.currentLiteralsLengthTable;
            FiniteStateEntropy.Table currentOffsetCodesTable = this.currentOffsetCodesTable;
            FiniteStateEntropy.Table currentMatchLengthTable = this.currentMatchLengthTable;

            int literalsLengthState = (int)peekBits(bitsConsumed, bits, currentLiteralsLengthTable.log2Size);
            bitsConsumed += currentLiteralsLengthTable.log2Size;

            int offsetCodesState = (int)peekBits(bitsConsumed, bits, currentOffsetCodesTable.log2Size);
            bitsConsumed += currentOffsetCodesTable.log2Size;

            int matchLengthState = (int)peekBits(bitsConsumed, bits, currentMatchLengthTable.log2Size);
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

                BitInputStream.Loader loader = new BitInputStream.Loader(inputBase.getBuf(), input, currentAddress, bits, bitsConsumed);
                loader.load();
                bitsConsumed = loader.getBitsConsumed();
                bits = loader.getBits();
                currentAddress = loader.getCurrentAddress();
                if (loader.isOverflow()) {
                    verify(sequenceCount == 0, input, "Not all sequences were consumed");
                    break;
                }

                // decode sequence
                int literalsLengthCode = literalsLengthSymbols[literalsLengthState];
                int matchLengthCode = matchLengthSymbols[matchLengthState];
                int offsetCode = offsetCodesSymbols[offsetCodesState];

                int literalsLengthBits = LITERALS_LENGTH_BITS[literalsLengthCode];
                int matchLengthBits = MATCH_LENGTH_BITS[matchLengthCode];
                int offsetBits = offsetCode;

                int offset = OFFSET_CODES_BASE[offsetCode];
                if (offsetCode > 0) {
                    offset += peekBits(bitsConsumed, bits, offsetBits);
                    bitsConsumed += offsetBits;
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

                int totalBits = literalsLengthBits + matchLengthBits + offsetBits;
                if (totalBits > 64 - 7 - (LITERAL_LENGTH_TABLE_LOG + MATCH_LENGTH_TABLE_LOG + OFFSET_TABLE_LOG)) {
                    BitInputStream.Loader loader1 = new BitInputStream.Loader(inputBase.getBuf(), input, currentAddress, bits, bitsConsumed);
                    loader1.load();

                    bitsConsumed = loader1.getBitsConsumed();
                    bits = loader1.getBits();
                    currentAddress = loader1.getCurrentAddress();
                }

                int numberOfBits;

                numberOfBits = literalsLengthNumbersOfBits[literalsLengthState];
                literalsLengthState = (int)(literalsLengthNewStates[literalsLengthState] + peekBits(bitsConsumed, bits, numberOfBits)); // <= 9 bits
                bitsConsumed += numberOfBits;

                numberOfBits = matchLengthNumbersOfBits[matchLengthState];
                matchLengthState = (int)(matchLengthNewStates[matchLengthState] + peekBits(bitsConsumed, bits, numberOfBits)); // <= 9 bits
                bitsConsumed += numberOfBits;

                numberOfBits = offsetCodesNumbersOfBits[offsetCodesState];
                offsetCodesState = (int)(offsetCodesNewStates[offsetCodesState] + peekBits(bitsConsumed, bits, numberOfBits)); // <= 8 bits
                bitsConsumed += numberOfBits;

                final int literalOutputLimit = output + literalsLength;
                final int matchOutputLimit = literalOutputLimit + matchLength;

                int literalEnd = literalsInput + literalsLength;
                verify(literalEnd <= literalsLimit, input, "Input is corrupted");

                int matchAddress = literalOutputLimit - offset;

                if (literalOutputLimit > fastOutputLimit) {
                    executeLastSequence(outputBase, output, literalOutputLimit, matchOutputLimit, fastOutputLimit, literalsInput, matchAddress);
                } else {
                    // copy literals. literalOutputLimit <= fastOutputLimit, so we can copy
                    // long at a time with over-copy
                    output = copyLiterals(outputBase, output, literalsInput, literalOutputLimit);
                    copyMatch(outputBase, fastOutputLimit, output, offset, matchOutputLimit, matchAddress, matchLength, fastMatchOutputLimit);
                }
                output = matchOutputLimit;
                literalsInput = literalEnd;
            }
        }

        // last literal segment
        output = copyLastLiteral(outputBase, output, literalsInput);

        return output - outputAddress;
    }

    private int copyLastLiteral(byte[] outputBase, int output, int literalsInput) {
        int lastLiteralsSize = literalsLimit - literalsInput;
        UnsafeUtil.copyMemory(literalsBase, literalsInput, outputBase, output, lastLiteralsSize);
        output += lastLiteralsSize;
        return output;
    }

    private void copyMatch(byte[] outputBase, long fastOutputLimit, int output, int offset, long matchOutputLimit, int matchAddress,
            int matchLength, long fastMatchOutputLimit) {
        matchAddress = copyMatchHead(outputBase, output, offset, matchAddress);
        output += SIZE_OF_LONG;
        matchLength -= SIZE_OF_LONG; // first 8 bytes copied above

        copyMatchTail(outputBase, fastOutputLimit, output, matchOutputLimit, matchAddress, matchLength, fastMatchOutputLimit);
    }

    private void copyMatchTail(byte[] outputBase, long fastOutputLimit, int output, long matchOutputLimit, int matchAddress, int matchLength,
            long fastMatchOutputLimit) {
        // fastMatchOutputLimit is just fastOutputLimit - SIZE_OF_LONG. It needs to be passed in so that it can be computed once for the
        // whole invocation to decompressSequences. Otherwise, we'd just compute it here.
        // If matchOutputLimit is < fastMatchOutputLimit, we know that even after the head (8 bytes) has been copied, the output pointer
        // will be within fastOutputLimit, so it's safe to copy blindly before checking the limit condition
        if (matchOutputLimit < fastMatchOutputLimit) {
            int copied = 0;
            do {
                UnsafeUtil.putLong(outputBase, output, UnsafeUtil.getLong(outputBase, matchAddress));
                output += SIZE_OF_LONG;
                matchAddress += SIZE_OF_LONG;
                copied += SIZE_OF_LONG;
            }
            while (copied < matchLength);
        } else {
            while (output < fastOutputLimit) {
                UnsafeUtil.putLong(outputBase, output, UnsafeUtil.getLong(outputBase, matchAddress));
                matchAddress += SIZE_OF_LONG;
                output += SIZE_OF_LONG;
            }

            while (output < matchOutputLimit) {
                UnsafeUtil.putByte(outputBase, output++, UnsafeUtil.getByte(outputBase, matchAddress++));
            }
        }
    }

    private int copyLiterals(byte[] outputBase, int output, int literalsInput, int literalOutputLimit) {
        int literalInput = literalsInput;
        do {
            UnsafeUtil.putLong(outputBase, output, UnsafeUtil.getLong(literalsBase, literalInput));
            output += SIZE_OF_LONG;
            literalInput += SIZE_OF_LONG;
        }
        while (output < literalOutputLimit);
        output = literalOutputLimit; // correction in case we over-copied
        return output;
    }

    private int computeMatchLengthTable(int matchLengthType, byte[] inputBase, int input, int inputLimit) {
        switch (matchLengthType) {
            case SEQUENCE_ENCODING_RLE:
                byte value = UnsafeUtil.getByte(inputBase, input++);
                verify(value <= MAX_MATCH_LENGTH_SYMBOL, input, "Value exceeds expected maximum value");

                FseTableReader.initializeRleTable(matchLengthTable, value);
                currentMatchLengthTable = matchLengthTable;
                break;
            case SEQUENCE_ENCODING_BASIC:
                currentMatchLengthTable = DEFAULT_MATCH_LENGTH_TABLE;
                break;
            case SEQUENCE_ENCODING_REPEAT:
                verify(currentMatchLengthTable != null, input, "Expected match length table to be present");
                break;
            case SEQUENCE_ENCODING_COMPRESSED:
                input += fse.readFseTable(matchLengthTable, inputBase, input, inputLimit, MAX_MATCH_LENGTH_SYMBOL, MATCH_LENGTH_TABLE_LOG);
                currentMatchLengthTable = matchLengthTable;
                break;
            default:
                throw fail(input, "Invalid match length encoding type");
        }
        return input;
    }

    private int computeOffsetsTable(int offsetCodesType, byte[] inputBase, int input, int inputLimit) {
        switch (offsetCodesType) {
            case SEQUENCE_ENCODING_RLE:
                byte value = UnsafeUtil.getByte(inputBase, input++);
                verify(value <= DEFAULT_MAX_OFFSET_CODE_SYMBOL, input, "Value exceeds expected maximum value");

                FseTableReader.initializeRleTable(offsetCodesTable, value);
                currentOffsetCodesTable = offsetCodesTable;
                break;
            case SEQUENCE_ENCODING_BASIC:
                currentOffsetCodesTable = DEFAULT_OFFSET_CODES_TABLE;
                break;
            case SEQUENCE_ENCODING_REPEAT:
                verify(currentOffsetCodesTable != null, input, "Expected match length table to be present");
                break;
            case SEQUENCE_ENCODING_COMPRESSED:
                input += fse.readFseTable(offsetCodesTable, inputBase, input, inputLimit, DEFAULT_MAX_OFFSET_CODE_SYMBOL, OFFSET_TABLE_LOG);
                currentOffsetCodesTable = offsetCodesTable;
                break;
            default:
                throw fail(input, "Invalid offset code encoding type");
        }
        return input;
    }

    private int computeLiteralsTable(int literalsLengthType, byte[] inputBase, int input, int inputLimit) {
        switch (literalsLengthType) {
            case SEQUENCE_ENCODING_RLE:
                byte value = UnsafeUtil.getByte(inputBase, input++);
                verify(value <= MAX_LITERALS_LENGTH_SYMBOL, input, "Value exceeds expected maximum value");

                FseTableReader.initializeRleTable(literalsLengthTable, value);
                currentLiteralsLengthTable = literalsLengthTable;
                break;
            case SEQUENCE_ENCODING_BASIC:
                currentLiteralsLengthTable = DEFAULT_LITERALS_LENGTH_TABLE;
                break;
            case SEQUENCE_ENCODING_REPEAT:
                verify(currentLiteralsLengthTable != null, input, "Expected match length table to be present");
                break;
            case SEQUENCE_ENCODING_COMPRESSED:
                input += fse.readFseTable(literalsLengthTable, inputBase, input, inputLimit, MAX_LITERALS_LENGTH_SYMBOL, LITERAL_LENGTH_TABLE_LOG);
                currentLiteralsLengthTable = literalsLengthTable;
                break;
            default:
                throw fail(input, "Invalid literals length encoding type");
        }
        return input;
    }

    private void executeLastSequence(byte[] outputBase, int output, long literalOutputLimit, long matchOutputLimit, int fastOutputLimit,
            int literalInput, int matchAddress) {
        // copy literals
        if (output < fastOutputLimit) {
            // wild copy
            do {
                UnsafeUtil.putLong(outputBase, output, UnsafeUtil.getLong(literalsBase, literalInput));
                output += SIZE_OF_LONG;
                literalInput += SIZE_OF_LONG;
            }
            while (output < fastOutputLimit);

            literalInput -= output - fastOutputLimit;
            output = fastOutputLimit;
        }

        while (output < literalOutputLimit) {
            UnsafeUtil.putByte(outputBase, output, UnsafeUtil.getByte(literalsBase, literalInput));
            output++;
            literalInput++;
        }

        // copy match
        while (output < matchOutputLimit) {
            UnsafeUtil.putByte(outputBase, output, UnsafeUtil.getByte(outputBase, matchAddress));
            output++;
            matchAddress++;
        }
    }

    void decodeCompressedLiterals(Buffer inputBase, int blockSize, LiteralsSectionHeader literalsSectionHeader) {
        final int pos = inputBase.getOffs();

        int compressedSize;
        int uncompressedSize;
        LiteralsSizeFormat sizeFormat = literalsSectionHeader.getLiteralsSizeFormat();

        if (sizeFormat == LiteralsSizeFormat.ONE_STREAM_10BITS || sizeFormat == LiteralsSizeFormat.FOUR_STREAMS_10BITS) {
            long data = literalsSectionHeader.getSizePart1();
            uncompressedSize = (int)(data & 0x3FF);
            compressedSize = (int)(data >>> 10 & 0x3FF);
        } else if (sizeFormat == LiteralsSizeFormat.FOUR_STREAMS_14BITS) {
            long sizePart2 = inputBase.getByte();
            long data = sizePart2 << 20 | literalsSectionHeader.getSizePart1();
            uncompressedSize = (int)(data & 0x3FFF);
            compressedSize = (int)(data >>> 14 & 0x3FFF);
        } else if (sizeFormat == LiteralsSizeFormat.FOUR_STREAMS_18BITS) {
            long sizePart2 = inputBase.getShort();
            long data = sizePart2 << 20 | literalsSectionHeader.getSizePart1();
            uncompressedSize = (int)(data & 0x3FFFF);
            compressedSize = (int)(data >>> 18 & 0x3FFFF);
        } else
            throw new Zip4jvmException("Invalid literals header size type");

        verify(uncompressedSize <= MAX_BLOCK_SIZE, pos, "Block exceeds maximum size");
        verify(inputBase.getOffs() - pos + compressedSize <= blockSize, pos, "Input is corrupted");

        final int inputLimit = inputBase.getOffs() + compressedSize;

        if (literalsSectionHeader.getLiteralsBlockType() == LiteralsBlockType.COMPRESSED)
            huffman.readTable(inputBase, compressedSize);

        literalsBase = literals;
        literalsAddress = 0;
        literalsLimit = uncompressedSize;

        if (sizeFormat == LiteralsSizeFormat.ONE_STREAM_10BITS)
            huffman.decodeSingleStream(inputBase, inputLimit, literals, literalsAddress, literalsLimit);
        else
            huffman.decode4Streams(inputBase, inputLimit, literals, literalsAddress, literalsLimit);
    }

    void decodeRleLiterals(Buffer inputBase, LiteralsSectionHeader literalsSectionHeader) {
        inputBase.skip(-3);
        final int pos = inputBase.getOffs();
        int literalSize = getLiteralSize(inputBase, literalsSectionHeader.getLiteralsSizeFormat());

        if (literalSize > MAX_BLOCK_SIZE)
            throw new Zip4jvmException("Output exceeds maximum block size");

        Arrays.fill(literals, 0, literalSize + SIZE_OF_LONG, (byte)inputBase.getByte());
        inputBase.seek(inputBase.getOffs() - pos);

        literalsBase = literals;
        literalsAddress = 0;
        literalsLimit = literalSize;
    }

    void decodeRawLiterals(Buffer inputBase, long blockSize, LiteralsSectionHeader literalsSectionHeader) {
        inputBase.skip(-3);
        final int pos = inputBase.getOffs();
        long inputLimit = pos + blockSize;
        int literalSize = getLiteralSize(inputBase, literalsSectionHeader.getLiteralsSizeFormat());
        int input = inputBase.getOffs();
        // TODO temporary
        inputBase.seek(pos);

        // Set literals pointer to [input, literalSize], but only if we can copy 8 bytes at a time during sequence decoding
        // Otherwise, copy literals into buffer that's big enough to guarantee that
        if (literalSize > inputLimit - input - SIZE_OF_LONG) {
            literalsBase = literals;
            literalsAddress = 0;
            literalsLimit = literalSize;

            UnsafeUtil.copyMemory(inputBase.getBuf(), input, literals, literalsAddress, literalSize);
            Arrays.fill(literals, literalSize, literalSize + SIZE_OF_LONG, (byte)0);
        } else {
            literalsBase = inputBase.getBuf();
            literalsAddress = input;
            literalsLimit = literalsAddress + literalSize;
        }

        inputBase.skip(input + literalSize - pos);
    }

}
