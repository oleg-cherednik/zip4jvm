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

import io.airlift.compress.zstd.BackwardDecorator;
import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.InputStreamForRead;
import io.airlift.compress.zstd.Util;
import io.airlift.compress.zstd.bis.BackwardBitInputStream;
import io.airlift.compress.zstd.fse.FiniteStateEntropy;
import lombok.RequiredArgsConstructor;

import static io.airlift.compress.zstd.Constants.SIZE_OF_INT;
import static io.airlift.compress.zstd.Util.isPowerOf2;
import static io.airlift.compress.zstd.Util.verify;

@RequiredArgsConstructor
public class Huffman {

    public static final int MAX_SYMBOL = 255;
    public static final int MAX_SYMBOL_COUNT = MAX_SYMBOL + 1;

    public static final int MAX_TABLE_LOG = 12;
    public static final int MIN_TABLE_LOG = 5;
    public static final int MAX_FSE_TABLE_LOG = 6;

    // table
    private int tableLog = -1;
    private final byte[] symbols = new byte[1 << MAX_TABLE_LOG];
    private final byte[] numbersOfBits = new byte[1 << MAX_TABLE_LOG];

    private final InputStreamForRead in;

    public boolean isLoaded() {
        return tableLog != -1;
    }

    // see 4.2.1.1
    public int readTable() {
        byte[] weights = new byte[MAX_SYMBOL + 1];
        int[] ranks = new int[MAX_TABLE_LOG + 1];

        int headerByte = in.getByte();
        int outputSize;
        ByteArrayWithOffs weights1 = new ByteArrayWithOffs(weights);

        if (headerByte >= 128) {
            readWeightsAsDirect(in, headerByte, weights1);
            outputSize = weights1.getOffs();
            headerByte = outputSize / 2;
        } else {
            readWeightsAsFse(in, headerByte, weights1);
            outputSize = weights1.getOffs();
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

    public static void main(String... args) {
        int headerByte = 127 + 11;
        byte[] buf = {
                (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9A,
                (byte) 0xBC, (byte) 0xDE, (byte) 0xF1, (byte) 0x23, (byte) 0x45,
                (byte) 0x67
        };

        InputStreamForRead in = new InputStreamForRead(buf);
        ByteArrayWithOffs weights = new ByteArrayWithOffs(new byte[15]);
        readWeightsAsDirect(in, headerByte, weights);
        int a = 0;
        a++;
    }

    private static int readWeightsAsDirect(InputStreamForRead in, int headerByte, ByteArrayWithOffs weights) {
        int outputSize = headerByte - 127;

        for (int i = 0; i < outputSize; i += 2) {
            int b = in.getByte();
            weights.putByte((byte) (b >> 4));

            if (i + 1 < outputSize)
                weights.putByte((byte) (b & 0b1111));
        }

        return outputSize;
    }

    private static void readWeightsAsFse(InputStreamForRead in, int totalBytes, ByteArrayWithOffs weights) {
        int lo = in.getOffs();
        FiniteStateEntropy fse = new FiniteStateEntropy().readFseTable(in, totalBytes);
        totalBytes -= in.getOffs() - lo;
        fse.decompress(new BackwardDecorator(in, totalBytes), weights);
    }

    public void decodeSingleStream(int inputLimit, ByteArrayWithOffs out) {
        // inputLimit is an absolute position in 'in', the stream is the rest of the literals section
        BackwardDecorator bwd = new BackwardDecorator(in, inputLimit - in.getOffs());
        BackwardBitInputStream bitStream = new BackwardBitInputStream(bwd, tableLog, symbols, numbersOfBits);

        // 4 symbols at a time
        long fastOutputLimit = out.getLimit() - 4;

        while (out.getOffs() < fastOutputLimit) {
            if (bitStream.load())
                break;

            bitStream.decodeSymbol(out);
            bitStream.decodeSymbol(out);
            bitStream.decodeSymbol(out);
            bitStream.decodeSymbol(out);
        }

        bitStream.decodeTail(out);
    }

    public void decode4Streams(final int inputLimit, ByteArrayWithOffs out) {
        verify(inputLimit - in.getOffs() >= 10, in.getOffs(), "Input is corrupted"); // jump table + 1 byte per stream

        int size1 = in.getShort();
        int size2 = in.getShort();
        int size3 = in.getShort();

        BackwardBitInputStream bbis1 = createStream(size1);
        BackwardBitInputStream bbis2 = createStream(size2);
        BackwardBitInputStream bbis3 = createStream(size3);
        BackwardBitInputStream bbis4 = createStream(inputLimit - in.getOffs());

        int segmentSize = (out.getLimit() + 3) / 4;

        int outputStart2 = segmentSize;
        int outputStart3 = segmentSize + segmentSize;
        int outputStart4 = outputStart3 + segmentSize;

        int output1 = 0;
        int output2 = outputStart2;
        int output3 = outputStart3;
        int output4 = outputStart4;

        long fastOutputLimit = out.getLimit() - 7;

        while (output4 < fastOutputLimit) {
            bbis1.decodeSymbol(out, output1);
            bbis2.decodeSymbol(out, output2);
            bbis3.decodeSymbol(out, output3);
            bbis4.decodeSymbol(out, output4);

            bbis1.decodeSymbol(out, output1 + 1);
            bbis2.decodeSymbol(out, output2 + 1);
            bbis3.decodeSymbol(out, output3 + 1);
            bbis4.decodeSymbol(out, output4 + 1);

            bbis1.decodeSymbol(out, output1 + 2);
            bbis2.decodeSymbol(out, output2 + 2);
            bbis3.decodeSymbol(out, output3 + 2);
            bbis4.decodeSymbol(out, output4 + 2);

            bbis1.decodeSymbol(out, output1 + 3);
            bbis2.decodeSymbol(out, output2 + 3);
            bbis3.decodeSymbol(out, output3 + 3);
            bbis4.decodeSymbol(out, output4 + 3);

            output1 += SIZE_OF_INT;
            output2 += SIZE_OF_INT;
            output3 += SIZE_OF_INT;
            output4 += SIZE_OF_INT;

            if (bbis1.load())
                break;
            if (bbis2.load())
                break;
            if (bbis3.load())
                break;
            if (bbis4.load())
                break;
        }

        verify(output1 <= outputStart2 && output2 <= outputStart3 && output3 <= outputStart4,
               in.getOffs(), "Input is corrupted");

        /// finish streams one by one
        bbis1.decodeTail(out, output1, outputStart2);
        bbis2.decodeTail(out, output2, outputStart3);
        bbis3.decodeTail(out, output3, outputStart4);
        bbis4.decodeTail(out, output4, out.getLimit());
    }

    private BackwardBitInputStream createStream(int totalBytes) {
        BackwardDecorator bd = new BackwardDecorator(in, totalBytes);
        return new BackwardBitInputStream(bd, tableLog, symbols, numbersOfBits);
    }

}
