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
package ru.olegcherednik.zip4jvm.io.zstd.huffman;

import ru.olegcherednik.zip4jvm.io.zstd.Buffer;
import ru.olegcherednik.zip4jvm.io.zstd.UnsafeUtil;
import ru.olegcherednik.zip4jvm.io.zstd.Util;
import ru.olegcherednik.zip4jvm.io.zstd.fse.FiniteStateEntropy;
import ru.olegcherednik.zip4jvm.io.zstd.fse.FseTableReader;
import ru.olegcherednik.zip4jvm.io.zstd.fse.bit.BitInputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_BYTE;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_INT;
import static ru.olegcherednik.zip4jvm.io.zstd.Util.isPowerOf2;
import static ru.olegcherednik.zip4jvm.io.zstd.Util.verify;
import static ru.olegcherednik.zip4jvm.io.zstd.fse.bit.BitInputStream.isEndOfStream;
import static ru.olegcherednik.zip4jvm.io.zstd.fse.bit.BitInputStream.peekBitsFast;

public class Huffman {

    public static final int MAX_SYMBOL = 255;
    public static final int MAX_SYMBOL_COUNT = MAX_SYMBOL + 1;

    public static final int MAX_TABLE_LOG = 12;
    public static final int MIN_TABLE_LOG = 5;
    public static final int MAX_FSE_TABLE_LOG = 6;

    // stats
    private final byte[] weights = new byte[MAX_SYMBOL + 1];
    private final int[] ranks = new int[MAX_TABLE_LOG + 1];

    // table
    private int tableLog = -1;
    private final byte[] symbols = new byte[1 << MAX_TABLE_LOG];
    private final byte[] numbersOfBits = new byte[1 << MAX_TABLE_LOG];

    private final FseTableReader reader = new FseTableReader();
    private final FiniteStateEntropy.Table fseTable = new FiniteStateEntropy.Table(MAX_FSE_TABLE_LOG);

    public boolean isLoaded() {
        return tableLog != -1;
    }

    public void readTable(Buffer inputBase, int size) {
        final int pos = inputBase.getOffs();
        Arrays.fill(ranks, 0);
        int input = pos;

        // read table header
        int inputSize = inputBase.getByte();
        input += SIZE_OF_BYTE;

        int outputSize;
        if (inputSize >= 128) {
            outputSize = inputSize - 127;
            inputSize = (outputSize + 1) / 2;

            verify(inputSize + 1 <= size, input, "Not enough input bytes");
            verify(outputSize <= MAX_SYMBOL + 1, input, "Input is corrupted");

            for (int i = 0; i < outputSize; i += 2) {
                int value = UnsafeUtil.getByte(inputBase.getBuf(), input + i / 2) & 0xFF;
                weights[i] = (byte)(value >>> 4);
                weights[i + 1] = (byte)(value & 0b1111);
            }
        } else {
            verify(inputSize + 1 <= size, input, "Not enough input bytes");

            int inputLimit = input + inputSize;
            input += reader.readFseTable(fseTable, inputBase.getBuf(), input, inputLimit, FiniteStateEntropy.MAX_SYMBOL, MAX_FSE_TABLE_LOG);
            outputSize = FiniteStateEntropy.decompress(fseTable, inputBase.getBuf(), input, inputLimit, weights);
        }

        int totalWeight = 0;
        for (int i = 0; i < outputSize; i++) {
            ranks[weights[i]]++;
            totalWeight += (1 << weights[i]) >> 1;   // TODO same as 1 << (weights[n] - 1)?
        }
        verify(totalWeight != 0, input, "Input is corrupted");

        tableLog = Util.highestBit(totalWeight) + 1;
        verify(tableLog <= MAX_TABLE_LOG, input, "Input is corrupted");

        int total = 1 << tableLog;
        int rest = total - totalWeight;
        verify(isPowerOf2(rest), input, "Input is corrupted");

        int lastWeight = Util.highestBit(rest) + 1;

        weights[outputSize] = (byte)lastWeight;
        ranks[lastWeight]++;

        int numberOfSymbols = outputSize + 1;

        // populate table
        int nextRankStart = 0;
        for (int i = 1; i < tableLog + 1; ++i) {
            int current = nextRankStart;
            nextRankStart += ranks[i] << (i - 1);
            ranks[i] = current;
        }

        for (int n = 0; n < numberOfSymbols; n++) {
            int weight = weights[n];
            int length = (1 << weight) >> 1;  // TODO: 1 << (weight - 1) ??

            byte symbol = (byte)n;
            byte numberOfBits = (byte)(tableLog + 1 - weight);
            for (int i = ranks[weight]; i < ranks[weight] + length; i++) {
                symbols[i] = symbol;
                numbersOfBits[i] = numberOfBits;
            }
            ranks[weight] += length;
        }

        verify(ranks[1] >= 2 && (ranks[1] & 1) == 0, input, "Input is corrupted");
        inputBase.seek(pos + inputSize + 1);
    }

    public void decodeSingleStream(Buffer inputBase, final int inputLimit, final byte[] outputBase,
            final int outputAddress, final long outputLimit) {
        final int pos = inputBase.getOffs();
        BitInputStream.Initializer initializer = new BitInputStream.Initializer(inputBase, pos, inputLimit);
        initializer.initialize();

        long bits = initializer.getBits();
        int bitsConsumed = initializer.getBitsConsumed();
        int currentAddress = initializer.getCurrentAddress();

        int tableLog = this.tableLog;
        byte[] numbersOfBits = this.numbersOfBits;
        byte[] symbols = this.symbols;

        // 4 symbols at a time
        int output = outputAddress;
        long fastOutputLimit = outputLimit - 4;
        while (output < fastOutputLimit) {
            BitInputStream.Loader loader = new BitInputStream.Loader(inputBase.getBuf(), pos, currentAddress, bits, bitsConsumed);
            boolean done = loader.load();
            bits = loader.getBits();
            bitsConsumed = loader.getBitsConsumed();
            currentAddress = loader.getCurrentAddress();
            if (done) {
                break;
            }

            bitsConsumed = decodeSymbol(outputBase, output, bits, bitsConsumed, tableLog, numbersOfBits, symbols);
            bitsConsumed = decodeSymbol(outputBase, output + 1, bits, bitsConsumed, tableLog, numbersOfBits, symbols);
            bitsConsumed = decodeSymbol(outputBase, output + 2, bits, bitsConsumed, tableLog, numbersOfBits, symbols);
            bitsConsumed = decodeSymbol(outputBase, output + 3, bits, bitsConsumed, tableLog, numbersOfBits, symbols);
            output += SIZE_OF_INT;
        }

        decodeTail(inputBase.getBuf(), pos, currentAddress, bitsConsumed, bits, outputBase, output, outputLimit);
        inputBase.seek(inputLimit);
    }

    public void decode4Streams(Buffer inputBase, int inputLimit, final byte[] outputBase, final int outputAddress,
            final long outputLimit) {
        final int inputAddress = inputBase.getOffs();
        verify(inputLimit - inputAddress >= 10, inputAddress, "Input is corrupted"); // jump table + 1 byte per stream

        JumpTable jumpTable = JumpTable.read(inputBase);
        List<BitStreamData> streams = new ArrayList<>();
        streams.add(new BitStreamData(inputBase.getOffs(), jumpTable.getStreamOneSize()));
        streams.add(new BitStreamData(streams.get(0).getEndOffs(), jumpTable.getStreamTwoSize()));
        streams.add(new BitStreamData(streams.get(1).getEndOffs(), jumpTable.getStreamThreeSize()));
        streams.add(new BitStreamData(streams.get(2).getEndOffs(), inputLimit - streams.get(2).getEndOffs()));

        for (BitStreamData stream : streams) {
            BitInputStream.InitializerBuffer initializerBuffer = BitInputStream.InitializerBuffer.read(inputBase, stream.getSize());
            stream.setBitsConsumed(initializerBuffer.getBitsConsumed());
            stream.setCurrentAddress(initializerBuffer.getCurrentAddress());
            stream.setBits(initializerBuffer.getBits());
        }

        int segmentSize = (int)((outputLimit - outputAddress + 3) / 4);

        streams.get(0).setOutputAddress(outputAddress);
        streams.get(1).setOutputAddress(streams.get(0).getOutputAddress() + segmentSize);
        streams.get(2).setOutputAddress(streams.get(1).getOutputAddress() + segmentSize);
        streams.get(3).setOutputAddress(streams.get(2).getOutputAddress() + segmentSize);

        streams.get(0).setOutput(streams.get(0).getOutputAddress());
        streams.get(1).setOutput(streams.get(1).getOutputAddress());
        streams.get(2).setOutput(streams.get(2).getOutputAddress());
        streams.get(3).setOutput(streams.get(3).getOutputAddress());

        long fastOutputLimit = outputLimit - 7;
        int tableLog = this.tableLog;
        byte[] numbersOfBits = this.numbersOfBits;
        byte[] symbols = this.symbols;

        out:
        while (streams.get(3).getOutput() < fastOutputLimit) {
            for (int i = 0; i < 4; i++)
                for (BitStreamData stream : streams)
                    decodeSymbol(outputBase, i, stream, tableLog, numbersOfBits, symbols);

            for (BitStreamData stream : streams)
                stream.setOutput(stream.getOutput() + SIZE_OF_INT);

            for (BitStreamData stream : streams)
                if (new BitInputStream.LoaderBuffer(inputBase.getBuf(), stream).load())
                    break out;
        }

        verify(streams.get(0).getOutput() <= streams.get(1).getOutputAddress()
                && streams.get(1).getOutput() <= streams.get(2).getOutputAddress()
                && streams.get(2).getOutput() <= streams.get(3).getOutputAddress(), inputAddress, "Input is corrupted");

        decodeTail(inputBase.getBuf(), streams.get(0), outputBase, streams.get(1).getOutputAddress());
        decodeTail(inputBase.getBuf(), streams.get(1), outputBase, streams.get(2).getOutputAddress());
        decodeTail(inputBase.getBuf(), streams.get(2), outputBase, streams.get(3).getOutputAddress());
        decodeTail(inputBase.getBuf(), streams.get(3), outputBase, outputLimit);
        inputBase.seek(inputLimit);
    }

    private void decodeTail(final byte[] inputBase, BitStreamData stream, final byte[] outputBase, final long outputLimit) {
        int startAddress = stream.getOffs();
        int outputAddress = stream.getOutput();
        int currentAddress = stream.getCurrentAddress();
        int bitsConsumed = stream.getBitsConsumed();
        long bits = stream.getBits();
        int tableLog = this.tableLog;
        byte[] numbersOfBits = this.numbersOfBits;
        byte[] symbols = this.symbols;

        // closer to the end
        while (outputAddress < outputLimit) {
            BitInputStream.Loader loader = new BitInputStream.Loader(inputBase, startAddress, currentAddress, bits, bitsConsumed);
            boolean done = loader.load();
            bitsConsumed = loader.getBitsConsumed();
            bits = loader.getBits();
            currentAddress = loader.getCurrentAddress();
            if (done) {
                break;
            }

            bitsConsumed = decodeSymbol(outputBase, outputAddress++, bits, bitsConsumed, tableLog, numbersOfBits, symbols);
        }

        // not more data in bit stream, so no need to reload
        while (outputAddress < outputLimit) {
            bitsConsumed = decodeSymbol(outputBase, outputAddress++, bits, bitsConsumed, tableLog, numbersOfBits, symbols);
        }

        verify(isEndOfStream(startAddress, currentAddress, bitsConsumed), startAddress, "Bit stream is not fully consumed");
    }

    private void decodeTail(final byte[] inputBase, final int startAddress, int currentAddress, int bitsConsumed, long bits, final byte[] outputBase,
            int outputAddress, final long outputLimit) {
        int tableLog = this.tableLog;
        byte[] numbersOfBits = this.numbersOfBits;
        byte[] symbols = this.symbols;

        // closer to the end
        while (outputAddress < outputLimit) {
            BitInputStream.Loader loader = new BitInputStream.Loader(inputBase, startAddress, currentAddress, bits, bitsConsumed);
            boolean done = loader.load();
            bitsConsumed = loader.getBitsConsumed();
            bits = loader.getBits();
            currentAddress = loader.getCurrentAddress();
            if (done) {
                break;
            }

            bitsConsumed = decodeSymbol(outputBase, outputAddress++, bits, bitsConsumed, tableLog, numbersOfBits, symbols);
        }

        // not more data in bit stream, so no need to reload
        while (outputAddress < outputLimit) {
            bitsConsumed = decodeSymbol(outputBase, outputAddress++, bits, bitsConsumed, tableLog, numbersOfBits, symbols);
        }

        verify(isEndOfStream(startAddress, currentAddress, bitsConsumed), startAddress, "Bit stream is not fully consumed");
    }

    private static void decodeSymbol(byte[] outputBase, int i, BitStreamData stream, int tableLog, byte[] numbersOfBits, byte[] symbols) {
        int outputAddress = stream.getOutput() + i;
        long bitContainer = stream.getBits();
        int value = (int)peekBitsFast(stream.getBitsConsumed(), bitContainer, tableLog);
        UnsafeUtil.putByte(outputBase, outputAddress, symbols[value]);
        stream.setBitsConsumed(stream.getBitsConsumed() + numbersOfBits[value]);
    }

    private static int decodeSymbol(byte[] outputBase, int outputAddress, long bitContainer, int bitsConsumed, int tableLog, byte[] numbersOfBits,
            byte[] symbols) {
        int value = (int)peekBitsFast(bitsConsumed, bitContainer, tableLog);
        UnsafeUtil.putByte(outputBase, outputAddress, symbols[value]);
        return bitsConsumed + numbersOfBits[value];
    }

}
