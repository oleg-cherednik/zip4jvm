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
package io.airlift.compress.zstd.huffman;

import io.airlift.compress.zstd.BitInputStream;
import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.FiniteStateEntropy;
import io.airlift.compress.zstd.FseTableReader;
import io.airlift.compress.zstd.Util;

import java.util.Arrays;

import static io.airlift.compress.zstd.BitInputStream.isEndOfStream;
import static io.airlift.compress.zstd.BitInputStream.peekBitsFast;
import static io.airlift.compress.zstd.Constants.SIZE_OF_INT;
import static io.airlift.compress.zstd.Constants.SIZE_OF_SHORT;
import static io.airlift.compress.zstd.Util.isPowerOf2;
import static io.airlift.compress.zstd.Util.verify;

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

    public int readTable(ByteArrayWithOffs in) {
        Arrays.fill(ranks, 0);
        int offs = in.getOffs();
        int inputSize = in.getByte(offs++) & 0xFF;

        int outputSize;
        if (inputSize >= 128) {
            outputSize = inputSize - 127;
            inputSize = (outputSize + 1) / 2;

            for (int i = 0; i < outputSize; i += 2) {
                int value = in.getByte(offs + i / 2) & 0xFF;
                weights[i] = (byte) (value >>> 4);
                weights[i + 1] = (byte) (value & 0b1111);
            }
        } else {
            int inputLimit = offs + inputSize;
            offs += reader.readFseTable(fseTable,
                                        in,
                                        offs,
                                        inputLimit,
                                        FiniteStateEntropy.MAX_SYMBOL,
                                        MAX_FSE_TABLE_LOG);
            outputSize = FiniteStateEntropy.decompress(fseTable, in, offs, inputLimit, new ByteArrayWithOffs(weights));
        }

        int totalWeight = 0;
        for (int i = 0; i < outputSize; i++) {
            ranks[weights[i]]++;
            totalWeight += (1 << weights[i]) >> 1;   // TODO same as 1 << (weights[n] - 1)?
        }
        verify(totalWeight != 0, offs, "Input is corrupted");

        tableLog = Util.highestBit(totalWeight) + 1;
        verify(tableLog <= MAX_TABLE_LOG, offs, "Input is corrupted");

        int total = 1 << tableLog;
        int rest = total - totalWeight;
        verify(isPowerOf2(rest), offs, "Input is corrupted");

        int lastWeight = Util.highestBit(rest) + 1;

        weights[outputSize] = (byte) lastWeight;
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

            byte symbol = (byte) n;
            byte numberOfBits = (byte) (tableLog + 1 - weight);
            for (int i = ranks[weight]; i < ranks[weight] + length; i++) {
                symbols[i] = symbol;
                numbersOfBits[i] = numberOfBits;
            }
            ranks[weight] += length;
        }

        verify(ranks[1] >= 2 && (ranks[1] & 1) == 0, offs, "Input is corrupted");

        return inputSize + 1;
    }

    public void decodeSingleStream(ByteArrayWithOffs in,
                                   final int inOffs,
                                   final int inputLimit,
                                   ByteArrayWithOffs out,
                                   final int outOffs,
                                   final long outputLimit) {
        BitInputStream.Initializer initializer = new BitInputStream.Initializer(in, inOffs, inputLimit);
        initializer.initialize();

        long bits = initializer.getBits();
        int bitsConsumed = initializer.getBitsConsumed();
        int curOffs = initializer.getCurOffs();

        int tableLog = this.tableLog;
        byte[] numbersOfBits = this.numbersOfBits;
        byte[] symbols = this.symbols;

        // 4 symbols at a time
        int output = outOffs;
        long fastOutputLimit = outputLimit - 4;
        while (output < fastOutputLimit) {
            BitInputStream.Loader loader = new BitInputStream.Loader(in,
                                                                     inOffs,
                                                                     curOffs,
                                                                     bits,
                                                                     bitsConsumed);
            boolean done = loader.load();
            bits = loader.getBits();
            bitsConsumed = loader.getBitsConsumed();
            curOffs = loader.getCurOffs();
            if (done) {
                break;
            }

            bitsConsumed = decodeSymbol(out, output, bits, bitsConsumed, tableLog, numbersOfBits, symbols);
            bitsConsumed = decodeSymbol(out, output + 1, bits, bitsConsumed, tableLog, numbersOfBits, symbols);
            bitsConsumed = decodeSymbol(out, output + 2, bits, bitsConsumed, tableLog, numbersOfBits, symbols);
            bitsConsumed = decodeSymbol(out, output + 3, bits, bitsConsumed, tableLog, numbersOfBits, symbols);
            output += SIZE_OF_INT;
        }

        decodeTail(in, inOffs, curOffs, bitsConsumed, bits, out, output, outputLimit);
    }

    public void decode4Streams(ByteArrayWithOffs in,
                               final int inOffs,
                               final int inputLimit,
                               ByteArrayWithOffs out,
                               final int outOffs,
                               final long outputLimit) {
        verify(inputLimit - inOffs >= 10, inOffs, "Input is corrupted"); // jump table + 1 byte per stream

        int start1 = inOffs + 3 * SIZE_OF_SHORT; // for the shorts we read below
        int start2 = start1 + (in.getShort(inOffs) & 0xFFFF);
        int start3 = start2 + (in.getShort(inOffs + 2) & 0xFFFF);
        int start4 = start3 + (in.getShort(inOffs + 4) & 0xFFFF);

        BitInputStream.Initializer initializer = new BitInputStream.Initializer(in, start1, start2);
        initializer.initialize();
        int stream1bitsConsumed = initializer.getBitsConsumed();
        int stream1curOffs = initializer.getCurOffs();
        long stream1bits = initializer.getBits();

        initializer = new BitInputStream.Initializer(in, start2, start3);
        initializer.initialize();
        int stream2bitsConsumed = initializer.getBitsConsumed();
        int stream2curOffs = initializer.getCurOffs();
        long stream2bits = initializer.getBits();

        initializer = new BitInputStream.Initializer(in, start3, start4);
        initializer.initialize();
        int stream3bitsConsumed = initializer.getBitsConsumed();
        int stream3curOffs = initializer.getCurOffs();
        long stream3bits = initializer.getBits();

        initializer = new BitInputStream.Initializer(in, start4, inputLimit);
        initializer.initialize();
        int stream4bitsConsumed = initializer.getBitsConsumed();
        int stream4curOffs = initializer.getCurOffs();
        long stream4bits = initializer.getBits();

        int segmentSize = (int) ((outputLimit - outOffs + 3) / 4);

        int outputStart2 = outOffs + segmentSize;
        int outputStart3 = outputStart2 + segmentSize;
        int outputStart4 = outputStart3 + segmentSize;

        int output1 = outOffs;
        int output2 = outputStart2;
        int output3 = outputStart3;
        int output4 = outputStart4;

        long fastOutputLimit = outputLimit - 7;
        int tableLog = this.tableLog;
        byte[] numbersOfBits = this.numbersOfBits;
        byte[] symbols = this.symbols;

        while (output4 < fastOutputLimit) {
            stream1bitsConsumed = decodeSymbol(out,
                                               output1,
                                               stream1bits,
                                               stream1bitsConsumed,
                                               tableLog,
                                               numbersOfBits,
                                               symbols);
            stream2bitsConsumed = decodeSymbol(out,
                                               output2,
                                               stream2bits,
                                               stream2bitsConsumed,
                                               tableLog,
                                               numbersOfBits,
                                               symbols);
            stream3bitsConsumed = decodeSymbol(out,
                                               output3,
                                               stream3bits,
                                               stream3bitsConsumed,
                                               tableLog,
                                               numbersOfBits,
                                               symbols);
            stream4bitsConsumed = decodeSymbol(out,
                                               output4,
                                               stream4bits,
                                               stream4bitsConsumed,
                                               tableLog,
                                               numbersOfBits,
                                               symbols);

            stream1bitsConsumed = decodeSymbol(out,
                                               output1 + 1,
                                               stream1bits,
                                               stream1bitsConsumed,
                                               tableLog,
                                               numbersOfBits,
                                               symbols);
            stream2bitsConsumed = decodeSymbol(out,
                                               output2 + 1,
                                               stream2bits,
                                               stream2bitsConsumed,
                                               tableLog,
                                               numbersOfBits,
                                               symbols);
            stream3bitsConsumed = decodeSymbol(out,
                                               output3 + 1,
                                               stream3bits,
                                               stream3bitsConsumed,
                                               tableLog,
                                               numbersOfBits,
                                               symbols);
            stream4bitsConsumed = decodeSymbol(out,
                                               output4 + 1,
                                               stream4bits,
                                               stream4bitsConsumed,
                                               tableLog,
                                               numbersOfBits,
                                               symbols);

            stream1bitsConsumed = decodeSymbol(out,
                                               output1 + 2,
                                               stream1bits,
                                               stream1bitsConsumed,
                                               tableLog,
                                               numbersOfBits,
                                               symbols);
            stream2bitsConsumed = decodeSymbol(out,
                                               output2 + 2,
                                               stream2bits,
                                               stream2bitsConsumed,
                                               tableLog,
                                               numbersOfBits,
                                               symbols);
            stream3bitsConsumed = decodeSymbol(out,
                                               output3 + 2,
                                               stream3bits,
                                               stream3bitsConsumed,
                                               tableLog,
                                               numbersOfBits,
                                               symbols);
            stream4bitsConsumed = decodeSymbol(out,
                                               output4 + 2,
                                               stream4bits,
                                               stream4bitsConsumed,
                                               tableLog,
                                               numbersOfBits,
                                               symbols);

            stream1bitsConsumed = decodeSymbol(out,
                                               output1 + 3,
                                               stream1bits,
                                               stream1bitsConsumed,
                                               tableLog,
                                               numbersOfBits,
                                               symbols);
            stream2bitsConsumed = decodeSymbol(out,
                                               output2 + 3,
                                               stream2bits,
                                               stream2bitsConsumed,
                                               tableLog,
                                               numbersOfBits,
                                               symbols);
            stream3bitsConsumed = decodeSymbol(out,
                                               output3 + 3,
                                               stream3bits,
                                               stream3bitsConsumed,
                                               tableLog,
                                               numbersOfBits,
                                               symbols);
            stream4bitsConsumed = decodeSymbol(out,
                                               output4 + 3,
                                               stream4bits,
                                               stream4bitsConsumed,
                                               tableLog,
                                               numbersOfBits,
                                               symbols);

            output1 += SIZE_OF_INT;
            output2 += SIZE_OF_INT;
            output3 += SIZE_OF_INT;
            output4 += SIZE_OF_INT;

            BitInputStream.Loader loader = new BitInputStream.Loader(in,
                                                                     start1,
                                                                     stream1curOffs,
                                                                     stream1bits,
                                                                     stream1bitsConsumed);
            boolean done = loader.load();
            stream1bitsConsumed = loader.getBitsConsumed();
            stream1bits = loader.getBits();
            stream1curOffs = loader.getCurOffs();

            if (done) {
                break;
            }

            loader = new BitInputStream.Loader(in,
                                               start2,
                                               stream2curOffs,
                                               stream2bits,
                                               stream2bitsConsumed);
            done = loader.load();
            stream2bitsConsumed = loader.getBitsConsumed();
            stream2bits = loader.getBits();
            stream2curOffs = loader.getCurOffs();

            if (done) {
                break;
            }

            loader = new BitInputStream.Loader(in,
                                               start3,
                                               stream3curOffs,
                                               stream3bits,
                                               stream3bitsConsumed);
            done = loader.load();
            stream3bitsConsumed = loader.getBitsConsumed();
            stream3bits = loader.getBits();
            stream3curOffs = loader.getCurOffs();
            if (done) {
                break;
            }

            loader = new BitInputStream.Loader(in,
                                               start4,
                                               stream4curOffs,
                                               stream4bits,
                                               stream4bitsConsumed);
            done = loader.load();
            stream4bitsConsumed = loader.getBitsConsumed();
            stream4bits = loader.getBits();
            stream4curOffs = loader.getCurOffs();
            if (done) {
                break;
            }
        }

        verify(output1 <= outputStart2 && output2 <= outputStart3 && output3 <= outputStart4,
               inOffs,
               "Input is corrupted");

        /// finish streams one by one
        decodeTail(in,
                   start1,
                   stream1curOffs,
                   stream1bitsConsumed,
                   stream1bits,
                   out,
                   output1,
                   outputStart2);
        decodeTail(in,
                   start2,
                   stream2curOffs,
                   stream2bitsConsumed,
                   stream2bits,
                   out,
                   output2,
                   outputStart3);
        decodeTail(in,
                   start3,
                   stream3curOffs,
                   stream3bitsConsumed,
                   stream3bits,
                   out,
                   output3,
                   outputStart4);
        decodeTail(in,
                   start4,
                   stream4curOffs,
                   stream4bitsConsumed,
                   stream4bits,
                   out,
                   output4,
                   outputLimit);
    }

    private void decodeTail(ByteArrayWithOffs in,
                            final int inOffs,
                            int curOffs,
                            int bitsConsumed,
                            long bits,
                            ByteArrayWithOffs out,
                            int outOffs,
                            final long outputLimit) {
        int tableLog = this.tableLog;
        byte[] numbersOfBits = this.numbersOfBits;
        byte[] symbols = this.symbols;

        // closer to the end
        while (outOffs < outputLimit) {
            BitInputStream.Loader loader = new BitInputStream.Loader(in,
                                                                     inOffs,
                                                                     curOffs,
                                                                     bits,
                                                                     bitsConsumed);
            boolean done = loader.load();
            bitsConsumed = loader.getBitsConsumed();
            bits = loader.getBits();
            curOffs = loader.getCurOffs();
            if (done) {
                break;
            }

            bitsConsumed = decodeSymbol(out,
                                        outOffs++,
                                        bits,
                                        bitsConsumed,
                                        tableLog,
                                        numbersOfBits,
                                        symbols);
        }

        // not more data in bit stream, so no need to reload
        while (outOffs < outputLimit) {
            bitsConsumed = decodeSymbol(out,
                                        outOffs++,
                                        bits,
                                        bitsConsumed,
                                        tableLog,
                                        numbersOfBits,
                                        symbols);
        }

        verify(isEndOfStream(inOffs, curOffs, bitsConsumed),
               inOffs,
               "Bit stream is not fully consumed");
    }

    private static int decodeSymbol(ByteArrayWithOffs out,
                                    int offs,
                                    long bitContainer,
                                    int bitsConsumed,
                                    int tableLog,
                                    byte[] numbersOfBits,
                                    byte[] symbols) {
        int value = (int) peekBitsFast(bitsConsumed, bitContainer, tableLog);
        out.putByte(offs, symbols[value]);
        return bitsConsumed + numbersOfBits[value];
    }
}
