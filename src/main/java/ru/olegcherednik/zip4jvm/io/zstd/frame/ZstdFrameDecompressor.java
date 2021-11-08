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
import ru.olegcherednik.zip4jvm.io.zstd.FiniteStateEntropy;
import ru.olegcherednik.zip4jvm.io.zstd.FseTableReader;
import ru.olegcherednik.zip4jvm.io.zstd.MalformedInputException;
import ru.olegcherednik.zip4jvm.io.zstd.UnsafeUtil;
import ru.olegcherednik.zip4jvm.io.zstd.XxHash64;
import ru.olegcherednik.zip4jvm.io.zstd.bit.BitInputStream;
import ru.olegcherednik.zip4jvm.io.zstd.huffman.Huffman;

import java.util.Arrays;

import static ru.olegcherednik.zip4jvm.io.zstd.Constants.COMPRESSED_BLOCK;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.COMPRESSED_LITERALS_BLOCK;
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
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.RAW_BLOCK;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.RAW_LITERALS_BLOCK;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.RLE_BLOCK;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.RLE_LITERALS_BLOCK;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SEQUENCE_ENCODING_BASIC;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SEQUENCE_ENCODING_COMPRESSED;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SEQUENCE_ENCODING_REPEAT;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SEQUENCE_ENCODING_RLE;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_BLOCK_HEADER;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_INT;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_LONG;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_SHORT;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.TREELESS_LITERALS_BLOCK;
import static ru.olegcherednik.zip4jvm.io.zstd.Util.fail;
import static ru.olegcherednik.zip4jvm.io.zstd.Util.mask;
import static ru.olegcherednik.zip4jvm.io.zstd.Util.verify;
import static ru.olegcherednik.zip4jvm.io.zstd.bit.BitInputStream.peekBits;

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
    private final Huffman huffman = new Huffman();
    private final FseTableReader fse = new FseTableReader();
    // current buffer containing literals
    private byte[] literalsBase;
    private int literalsAddress;
    private int literalsLimit;
    private FiniteStateEntropy.Table currentLiteralsLengthTable;
    private FiniteStateEntropy.Table currentOffsetCodesTable;
    private FiniteStateEntropy.Table currentMatchLengthTable;

    private static int decodeRawBlock(byte[] inputBase, int inputAddress, int blockSize, byte[] outputBase, int outputAddress) {
        UnsafeUtil.copyMemory(inputBase, inputAddress, outputBase, outputAddress, blockSize);
        return blockSize;
    }

    private static int decodeRleBlock(int size, byte[] inputBase, int inputAddress, byte[] outputBase, int outputAddress) {
        int output = outputAddress;
        long value = UnsafeUtil.getByte(inputBase, inputAddress) & 0xFFL;

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
                UnsafeUtil.putLong(outputBase, output, packed);
                output += SIZE_OF_LONG;
                remaining -= SIZE_OF_LONG;
            }
            while (remaining >= SIZE_OF_LONG);
        }

        for (int i = 0; i < remaining; i++) {
            UnsafeUtil.putByte(outputBase, output, (byte)value);
            output++;
        }

        return size;
    }

    private static int verifyMagic(Buffer inputBase) {
        int magic = inputBase.getInt();
        if (magic != MAGIC_NUMBER) {
            if (magic == V07_MAGIC_NUMBER) {
                throw new Zip4jvmException("Data encoded in unsupported ZSTD v0.7 format");
            }
            throw new Zip4jvmException("Invalid magic prefix");
        }

        return SIZE_OF_INT;
    }

    public int decompress(Buffer inputBase, byte[] outputBase) {
        int input = 0;
        int output = 0;

        reset();
        int outputStart = output;

        input += verifyMagic(inputBase);

        FrameHeader frameHeader = FrameHeader.read(inputBase.getBuf(), input);
        input += frameHeader.getHeaderSize();

        while (true) {
            // read block header
            int header = UnsafeUtil.getInt(inputBase.getBuf(), input) & 0xFF_FFFF;
            input += SIZE_OF_BLOCK_HEADER;

            boolean lastBlock = (header & 1) != 0;
            int blockType = (header >>> 1) & 0b11;
            int blockSize = (header >>> 3) & 0x1F_FFFF; // 21 bits

            int decodedSize;

            switch (blockType) {
                case RAW_BLOCK:
                    decodedSize = decodeRawBlock(inputBase.getBuf(), input, blockSize, outputBase, output);
                    input += blockSize;
                    break;
                case RLE_BLOCK:
                    decodedSize = decodeRleBlock(blockSize, inputBase.getBuf(), input, outputBase, output);
                    input += 1;
                    break;
                case COMPRESSED_BLOCK:
                    decodedSize = decodeCompressedBlock(inputBase.getBuf(), input, blockSize, outputBase, output);
                    input += blockSize;
                    break;
                default:
                    throw fail(input, "Invalid block type");
            }

            output += decodedSize;

            if (lastBlock)
                break;
        }

        if (frameHeader.isHasChecksum()) {
            int decodedFrameSize = output - outputStart;

            long hash = XxHash64.hash(0, outputBase, outputStart, decodedFrameSize);

            int checksum = UnsafeUtil.getInt(inputBase.getBuf(), input);
            if (checksum != (int)hash) {
                throw new MalformedInputException(input,
                        String.format("Bad checksum. Expected: %s, actual: %s", Integer.toHexString(checksum), Integer.toHexString((int)hash)));
            }
        }

        return output;
    }

    private void reset() {
        previousOffsets[0] = 1;
        previousOffsets[1] = 4;
        previousOffsets[2] = 8;

        currentLiteralsLengthTable = null;
        currentOffsetCodesTable = null;
        currentMatchLengthTable = null;
    }

    private int decodeCompressedBlock(byte[] inputBase, final int inputAddress, int blockSize, byte[] outputBase, int outputAddress) {
        long inputLimit = inputAddress + blockSize;
        int input = inputAddress;

        // decode literals
        int literalsBlockType = UnsafeUtil.getByte(inputBase, input) & 0b11;

        switch (literalsBlockType) {
            case RAW_LITERALS_BLOCK:
                input += decodeRawLiterals(inputBase, input, inputLimit);
                break;
            case RLE_LITERALS_BLOCK:
                input += decodeRleLiterals(inputBase, input, blockSize);
                break;
            case TREELESS_LITERALS_BLOCK:
                verify(huffman.isLoaded(), input, "Dictionary is corrupted");
            case COMPRESSED_LITERALS_BLOCK:
                input += decodeCompressedLiterals(inputBase, input, blockSize, literalsBlockType);
                break;
            default:
                throw fail(input, "Invalid literals block encoding type");
        }

        return decompressSequences(
                inputBase, input, inputAddress + blockSize,
                outputBase, outputAddress, literalsBase, literalsAddress, literalsLimit);
    }

    private int decompressSequences(
            final byte[] inputBase, final int inputAddress, final int inputLimit,
            final byte[] outputBase, final int outputAddress,
            final byte[] literalsBase, final int literalsAddress, final int literalsLimit) {
        final int fastOutputLimit = outputBase.length - SIZE_OF_LONG;
        final long fastMatchOutputLimit = fastOutputLimit - SIZE_OF_LONG;

        int input = inputAddress;
        int output = outputAddress;

        int literalsInput = literalsAddress;

        // decode header
        int sequenceCount = UnsafeUtil.getByte(inputBase, input++) & 0xFF;
        if (sequenceCount != 0) {
            if (sequenceCount == 255) {
                sequenceCount = (UnsafeUtil.getShort(inputBase, input) & 0xFFFF) + LONG_NUMBER_OF_SEQUENCES;
                input += SIZE_OF_SHORT;
            } else if (sequenceCount > 127) {
                sequenceCount = ((sequenceCount - 128) << 8) + (UnsafeUtil.getByte(inputBase, input++) & 0xFF);
            }

            byte type = UnsafeUtil.getByte(inputBase, input++);

            int literalsLengthType = (type & 0xFF) >>> 6;
            int offsetCodesType = (type >>> 4) & 0b11;
            int matchLengthType = (type >>> 2) & 0b11;

            input = computeLiteralsTable(literalsLengthType, inputBase, input, inputLimit);
            input = computeOffsetsTable(offsetCodesType, inputBase, input, inputLimit);
            input = computeMatchLengthTable(matchLengthType, inputBase, input, inputLimit);

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

                BitInputStream.Loader loader = new BitInputStream.Loader(inputBase, input, currentAddress, bits, bitsConsumed);
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
                    BitInputStream.Loader loader1 = new BitInputStream.Loader(inputBase, input, currentAddress, bits, bitsConsumed);
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
                    output = copyLiterals(outputBase, literalsBase, output, literalsInput, literalOutputLimit);
                    copyMatch(outputBase, fastOutputLimit, output, offset, matchOutputLimit, matchAddress, matchLength, fastMatchOutputLimit);
                }
                output = matchOutputLimit;
                literalsInput = literalEnd;
            }
        }

        // last literal segment
        output = copyLastLiteral(outputBase, literalsBase, literalsLimit, output, literalsInput);

        return output - outputAddress;
    }

    private static int copyLastLiteral(byte[] outputBase, byte[] literalsBase, int literalsLimit, int output, int literalsInput) {
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

    private static int copyLiterals(byte[] outputBase, byte[] literalsBase, int output, int literalsInput, int literalOutputLimit) {
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
            UnsafeUtil.putByte(outputBase, output, UnsafeUtil.getByte((byte[])outputBase, matchAddress));
            output++;
            matchAddress++;
        }
    }

    private int decodeCompressedLiterals(byte[] inputBase, final int inputAddress, int blockSize, int literalsBlockType) {
        int input = inputAddress;

        // compressed
        int compressedSize;
        int uncompressedSize;
        boolean singleStream = false;
        int headerSize;
        int type = (UnsafeUtil.getByte(inputBase, input) >> 2) & 0b11;
        switch (type) {
            case 0:
                singleStream = true;
            case 1: {
                int header = UnsafeUtil.getInt(inputBase, input);

                headerSize = 3;
                uncompressedSize = (header >>> 4) & mask(10);
                compressedSize = (header >>> 14) & mask(10);
                break;
            }
            case 2: {
                int header = UnsafeUtil.getInt(inputBase, input);

                headerSize = 4;
                uncompressedSize = (header >>> 4) & mask(14);
                compressedSize = (header >>> 18) & mask(14);
                break;
            }
            case 3:
                // read 5 little-endian bytes
                long header = UnsafeUtil.getByte(inputBase, input) & 0xFF |
                        (UnsafeUtil.getInt(inputBase, input + 1) & 0xFFFF_FFFFL) << 8;

                headerSize = 5;
                uncompressedSize = (int)((header >>> 4) & mask(18));
                compressedSize = (int)((header >>> 22) & mask(18));
                break;
            default:
                throw fail(input, "Invalid literals header size type");
        }

        verify(uncompressedSize <= MAX_BLOCK_SIZE, input, "Block exceeds maximum size");
        verify(headerSize + compressedSize <= blockSize, input, "Input is corrupted");

        input += headerSize;

        int inputLimit = input + compressedSize;
        if (literalsBlockType != TREELESS_LITERALS_BLOCK) {
            input += huffman.readTable(inputBase, input, compressedSize);
        }

        literalsBase = literals;
        literalsAddress = 0;
        literalsLimit = uncompressedSize;

        if (singleStream) {
            huffman.decodeSingleStream(inputBase, input, inputLimit, literals, literalsAddress, literalsLimit);
        } else {
            huffman.decode4Streams(inputBase, input, inputLimit, literals, literalsAddress, literalsLimit);
        }

        return headerSize + compressedSize;
    }

    private int decodeRleLiterals(byte[] inputBase, final int inputAddress, int blockSize) {
        int input = inputAddress;
        int outputSize;

        int type = (UnsafeUtil.getByte(inputBase, input) >> 2) & 0b11;
        switch (type) {
            case 0:
            case 2:
                outputSize = (UnsafeUtil.getByte(inputBase, input) & 0xFF) >>> 3;
                input++;
                break;
            case 1:
                outputSize = (UnsafeUtil.getShort(inputBase, input) & 0xFFFF) >>> 4;
                input += 2;
                break;
            case 3:
                // we need at least 4 bytes (3 for the header, 1 for the payload)
                verify(blockSize >= SIZE_OF_INT, input, "Not enough input bytes");
                outputSize = (UnsafeUtil.getInt(inputBase, input) & 0xFF_FFFF) >>> 4;
                input += 3;
                break;
            default:
                throw fail(input, "Invalid RLE literals header encoding type");
        }

        verify(outputSize <= MAX_BLOCK_SIZE, input, "Output exceeds maximum block size");

        byte value = UnsafeUtil.getByte(inputBase, input++);
        Arrays.fill(literals, 0, outputSize + SIZE_OF_LONG, value);

        literalsBase = literals;
        literalsAddress = 0;
        literalsLimit = outputSize;

        return input - inputAddress;
    }

    private int decodeRawLiterals(byte[] inputBase, final int inputAddress, long inputLimit) {
        int input = inputAddress;
        int type = (UnsafeUtil.getByte(inputBase, input) >> 2) & 0b11;

        int literalSize;
        switch (type) {
            case 0:
            case 2:
                literalSize = (UnsafeUtil.getByte(inputBase, input) & 0xFF) >>> 3;
                input++;
                break;
            case 1:
                literalSize = (UnsafeUtil.getShort(inputBase, input) & 0xFFFF) >>> 4;
                input += 2;
                break;
            case 3:
                // read 3 little-endian bytes
                int header = (UnsafeUtil.getByte(inputBase, input) & 0xFF) |
                        ((UnsafeUtil.getShort(inputBase, input + 1) & 0xFFFF) << 8);

                literalSize = header >>> 4;
                input += 3;
                break;
            default:
                throw fail(input, "Invalid raw literals header encoding type");
        }

        verify(input + literalSize <= inputLimit, input, "Not enough input bytes");

        // Set literals pointer to [input, literalSize], but only if we can copy 8 bytes at a time during sequence decoding
        // Otherwise, copy literals into buffer that's big enough to guarantee that
        if (literalSize > inputLimit - input - SIZE_OF_LONG) {
            literalsBase = literals;
            literalsAddress = 0;
            literalsLimit = literalSize;

            UnsafeUtil.copyMemory(inputBase, input, literals, literalsAddress, literalSize);
            Arrays.fill(literals, literalSize, literalSize + SIZE_OF_LONG, (byte)0);
        } else {
            literalsBase = inputBase;
            literalsAddress = input;
            literalsLimit = literalsAddress + literalSize;
        }
        input += literalSize;

        return input - inputAddress;
    }

}
