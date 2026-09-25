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

import io.airlift.compress.zstd.BackwardBitInputStream;
import io.airlift.compress.zstd.BitInputStream;
import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.Util;
import io.airlift.compress.zstd.fse.FiniteStateEntropy;

import java.util.Arrays;

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

    public boolean isLoaded() {
        return tableLog != -1;
    }

    // see 4.2.1.1
    public int readTable(ByteArrayWithOffs in) {
        Arrays.fill(ranks, 0);

        int headerByte = in.getByte();
        int offs = in.getOffs();
        int outputSize;

        if (headerByte >= 128) {
            outputSize = headerByte - 127;
            headerByte = (outputSize + 1) / 2;

            for (int i = 0; i < outputSize; i += 2) {
                int value = in.getByte(offs + i / 2) & 0xFF;
                weights[i] = (byte) (value >>> 4);
                weights[i + 1] = (byte) (value & 0b1111);
            }
        } else {
            int lo = in.getOffs();
            FiniteStateEntropy fse = new FiniteStateEntropy();
            fse.readFseTable(in, headerByte);
            int totalBytes = headerByte - in.getOffs() + lo;
            BackwardBitInputStream bbis = new BackwardBitInputStream(in, totalBytes, true);
            outputSize = fse.decompress(bbis, weights);
        }

        int totalWeight = 0;
        for (int i = 0; i < outputSize; i++) {
            ranks[weights[i]]++;
            totalWeight += (1 << weights[i]) >> 1;   // TODO same as 1 << (weights[n] - 1)?
        }
        verify(totalWeight != 0, in.getOffs(), "Input is corrupted");

        tableLog = Util.highestBit(totalWeight) + 1;
        verify(tableLog <= MAX_TABLE_LOG, in.getOffs(), "Input is corrupted");

        int total = 1 << tableLog;
        int rest = total - totalWeight;
        verify(isPowerOf2(rest), in.getOffs(), "Input is corrupted");

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

        verify(ranks[1] >= 2 && (ranks[1] & 1) == 0, in.getOffs(), "Input is corrupted");

        return headerByte + 1;
    }

    public void decodeSingleStream(ByteArrayWithOffs in, final int inputLimit,
                                   ByteArrayWithOffs out, final int outOffs, final long outputLimit) {
        BitInputStream.InitializerNew bitStream =
                new BitInputStream.InitializerNew(new BackwardBitInputStream(in, inputLimit, false),
                                                  tableLog, symbols, numbersOfBits);

        // 4 symbols at a time
        int output = outOffs;
        long fastOutputLimit = outputLimit - 4;

        while (output < fastOutputLimit) {
            if (bitStream.load())
                break;

            bitStream.decodeSymbol(out, output);
            bitStream.decodeSymbol(out, output + 1);
            bitStream.decodeSymbol(out, output + 2);
            bitStream.decodeSymbol(out, output + 3);

            output += SIZE_OF_INT;
        }

        bitStream.decodeTail(in, out, output, outputLimit);
    }

    public void decode4Streams(ByteArrayWithOffs in, final int inputLimit,
                               ByteArrayWithOffs out, final int outOffs, final long outputLimit) {
        verify(inputLimit - in.getOffs() >= 10, in.getOffs(), "Input is corrupted"); // jump table + 1 byte per stream

        int start1 = in.getOffs() + 3 * SIZE_OF_SHORT; // for the shorts we read below
        int start2 = start1 + in.getShort();
        int start3 = start2 + in.getShort();
        int start4 = start3 + in.getShort();

        BitInputStream.InitializerNew bitStream1 =
                new BitInputStream.InitializerNew(new BackwardBitInputStream(in, start2 - start1, false),
                                                  tableLog, symbols, numbersOfBits);
        BitInputStream.InitializerNew bitStream2 =
                new BitInputStream.InitializerNew(new BackwardBitInputStream(in, start3 - start2, false),
                                                  tableLog, symbols, numbersOfBits);

        BitInputStream.InitializerNew bitStream3 =
                new BitInputStream.InitializerNew(new BackwardBitInputStream(in, start4 - start3, false),
                                                  tableLog, symbols, numbersOfBits);

        BitInputStream.InitializerNew bitStream4 =
                new BitInputStream.InitializerNew(new BackwardBitInputStream(in, inputLimit - start4, false),
                                                  tableLog, symbols, numbersOfBits);

        int segmentSize = (int) ((outputLimit - outOffs + 3) / 4);

        int outputStart2 = outOffs + segmentSize;
        int outputStart3 = outputStart2 + segmentSize;
        int outputStart4 = outputStart3 + segmentSize;

        int output1 = outOffs;
        int output2 = outputStart2;
        int output3 = outputStart3;
        int output4 = outputStart4;

        long fastOutputLimit = outputLimit - 7;

        while (output4 < fastOutputLimit) {
            bitStream1.decodeSymbol(out, output1);
            bitStream2.decodeSymbol(out, output2);
            bitStream3.decodeSymbol(out, output3);
            bitStream4.decodeSymbol(out, output4);

            bitStream1.decodeSymbol(out, output1 + 1);
            bitStream2.decodeSymbol(out, output2 + 1);
            bitStream3.decodeSymbol(out, output3 + 1);
            bitStream4.decodeSymbol(out, output4 + 1);

            bitStream1.decodeSymbol(out, output1 + 2);
            bitStream2.decodeSymbol(out, output2 + 2);
            bitStream3.decodeSymbol(out, output3 + 2);
            bitStream4.decodeSymbol(out, output4 + 2);

            bitStream1.decodeSymbol(out, output1 + 3);
            bitStream2.decodeSymbol(out, output2 + 3);
            bitStream3.decodeSymbol(out, output3 + 3);
            bitStream4.decodeSymbol(out, output4 + 3);

            output1 += SIZE_OF_INT;
            output2 += SIZE_OF_INT;
            output3 += SIZE_OF_INT;
            output4 += SIZE_OF_INT;

            if (bitStream1.load())
                break;
            if (bitStream2.load())
                break;
            if (bitStream3.load())
                break;
            if (bitStream4.load())
                break;
        }

        verify(output1 <= outputStart2 && output2 <= outputStart3 && output3 <= outputStart4,
               in.getOffs(), "Input is corrupted");

        /// finish streams one by one
        bitStream1.decodeTail(in, out, output1, outputStart2);
        bitStream2.decodeTail(in, out, output2, outputStart3);
        bitStream3.decodeTail(in, out, output3, outputStart4);
        bitStream4.decodeTail(in, out, output4, outputLimit);
    }

}
