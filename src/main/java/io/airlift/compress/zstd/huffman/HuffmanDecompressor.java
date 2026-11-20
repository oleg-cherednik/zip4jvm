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

import io.airlift.compress.zstd.In;
import io.airlift.compress.zstd.Out;
import io.airlift.compress.zstd.fse.decompress.FiniteStateEntropyForDecompress;
import io.airlift.compress.zstd.io.BackwardBitInputStream;
import io.airlift.compress.zstd.io.BackwardDecorator;
import io.airlift.compress.zstd.io.ByteArrayRandomAccessOut;
import io.airlift.compress.zstd.io.RandomAccessOut;
import io.airlift.compress.zstd.model.Util;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import static io.airlift.compress.zstd.model.Util.isPowerOf2;
import static io.airlift.compress.zstd.model.Util.verify;

@RequiredArgsConstructor
public class HuffmanDecompressor {

    private static final int TABLE_NOT_LOADED = -1;

    // table
    private int tableLog = TABLE_NOT_LOADED;
    private final byte[] symbols = new byte[1 << HuffmanConst.MAX_TABLE_LOG];
    private final byte[] numbersOfBits = new byte[1 << HuffmanConst.MAX_TABLE_LOG];

    public boolean isLoaded() {
        return tableLog != TABLE_NOT_LOADED;
    }

    public void reset() {
        tableLog = TABLE_NOT_LOADED;
    }

    // see 4.2.1.1
    public int readTable(In in) throws IOException {
        byte[] weights = new byte[HuffmanConst.MAX_SYMBOL + 1];
        int[] ranks = new int[HuffmanConst.MAX_TABLE_LOG + 1];

        int headerByte = in.readByte();

        ByteArrayRandomAccessOut weights1 = new ByteArrayRandomAccessOut(weights);

        if (headerByte >= 128) {
            readWeightsAsDirect(in, headerByte, weights1);
            headerByte = weights1.getOffs() / 2;
        } else
            readWeightsAsFse(in, headerByte, weights1);

        int outputSize = weights1.getOffs();
        int totalWeight = 0;

        for (int i = 0; i < outputSize; i++) {
            ranks[weights[i]]++;
            totalWeight += (1 << weights[i]) >> 1;
        }

        verify(totalWeight != 0, "Input is corrupted");

        tableLog = Util.highestBit(totalWeight) + 1;
        verify(tableLog <= HuffmanConst.MAX_TABLE_LOG, "Input is corrupted");

        int total = 1 << tableLog;
        int rest = total - totalWeight;
        verify(isPowerOf2(rest), "Input is corrupted");

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
            int length = (1 << weight) >> 1;

            byte symbol = (byte) n;
            byte numberOfBits = (byte) (tableLog + 1 - weight);
            for (int i = ranks[weight]; i < ranks[weight] + length; i++) {
                symbols[i] = symbol;
                numbersOfBits[i] = numberOfBits;
            }
            ranks[weight] += length;
        }

        verify(ranks[1] >= 2 && (ranks[1] & 1) == 0, "Input is corrupted");

        return headerByte + 1;
    }

    private static int readWeightsAsDirect(In in, int headerByte, Out weights) {
        int numberOfSymbols = headerByte - 127;

        for (int i = 0; i < numberOfSymbols; i += 2) {
            int b = in.readByte();
            weights.writeByte((byte) (b >> 4));

            if (i + 1 < numberOfSymbols)
                weights.writeByte((byte) (b & 0xF));
        }

        return numberOfSymbols;
    }

    private static void readWeightsAsFse(In in, int totalBytes, Out weights) throws IOException {
        long lo = in.getPos();
        FiniteStateEntropyForDecompress fse = new FiniteStateEntropyForDecompress().readFseTable(in, totalBytes);
        totalBytes -= (int) (in.getPos() - lo);
        fse.decompress(new BackwardDecorator(in, totalBytes), weights);
    }

    public void decodeSingleStream(In in, Out out) throws IOException {
        BackwardDecorator bwd = new BackwardDecorator(in, in.available());
        BackwardBitInputStream bitStream = new BackwardBitInputStream(bwd, tableLog, symbols, numbersOfBits);

        while (out.available() >= 4) {
            if (bitStream.load())
                break;

            bitStream.decodeSymbol(out);
            bitStream.decodeSymbol(out);
            bitStream.decodeSymbol(out);
            bitStream.decodeSymbol(out);
        }

        bitStream.decodeTail(out);
    }

    public void decode4Streams(In in, RandomAccessOut rao) throws IOException {
        verify(in.available() >= 10, "Input is corrupted"); // jump table + 1 byte per stream

        int size1 = in.readWord();
        int size2 = in.readWord();
        int size3 = in.readWord();
        int size4 = in.available() - size1 - size2 - size3;

        int segmentSize = (rao.available() + 3) / 4;
        int offsSegment2 = segmentSize;
        int offsSegment3 = offsSegment2 + segmentSize;
        int offsSegment4 = offsSegment3 + segmentSize;

        BackwardBitInputStream bbis1 = createStream(in, size1);
        BackwardBitInputStream bbis2 = createStream(in, size2);
        BackwardBitInputStream bbis3 = createStream(in, size3);
        BackwardBitInputStream bbis4 = createStream(in, size4);

        RandomAccessOut out1 = rao.slice(0, offsSegment2);
        RandomAccessOut out2 = rao.slice(offsSegment2, offsSegment3);
        RandomAccessOut out3 = rao.slice(offsSegment3, offsSegment4);
        RandomAccessOut out4 = rao.slice(offsSegment4, rao.available());

        while (rao.available() >= 8) {
            // 4 * 4 per loop
            for (int i = 0; i < 4; i++) {
                bbis1.decodeSymbol(out1);
                bbis2.decodeSymbol(out2);
                bbis3.decodeSymbol(out3);
                bbis4.decodeSymbol(out4);
            }

            if (bbis1.load())
                break;
            if (bbis2.load())
                break;
            if (bbis3.load())
                break;
            if (bbis4.load())
                break;
        }

        verify(out1.getOffs() <= offsSegment2, "Input is corrupted");
        verify(out2.getOffs() <= offsSegment3, "Input is corrupted");
        verify(out3.getOffs() <= offsSegment4, "Input is corrupted");

        bbis1.decodeTail(out1);
        bbis2.decodeTail(out2);
        bbis3.decodeTail(out3);
        bbis4.decodeTail(out4);
    }

    private BackwardBitInputStream createStream(In in, int totalBytes) throws IOException {
        BackwardDecorator bd = new BackwardDecorator(in, totalBytes);
        return new BackwardBitInputStream(bd, tableLog, symbols, numbersOfBits);
    }

}
