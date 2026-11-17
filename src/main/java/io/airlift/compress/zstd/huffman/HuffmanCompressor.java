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
import io.airlift.compress.zstd.DecompressIn;
import io.airlift.compress.zstd.io.HuffmanBitOutputStream;
import io.airlift.compress.zstd.io.HeaderDataOutDecorator;
import io.airlift.compress.zstd.Out;
import io.airlift.compress.zstd.io.RandomAccessOut;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import static io.airlift.compress.zstd.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.Constants.SIZE_OF_SHORT;

@RequiredArgsConstructor
public class HuffmanCompressor {

    private final DecompressIn in;
    private final HuffmanCompressionTable table;

    public void compressFourStreams(RandomAccessOut rao, final int totalBytes) throws IOException {
        if (totalBytes <= 3 * SIZE_OF_SHORT + 3 * SIZE_OF_BYTE) // jump table + one byte per stream
            return;  // no saving possible: input too small

        HeaderDataOutDecorator hdod = new HeaderDataOutDecorator(rao);
        hdod.setHeaderSize(3 * SIZE_OF_SHORT);

        int segmentSize = (totalBytes + 3) / 4;

        Out dataOut = hdod.getOutForData();
        int size1 = compressSingleStreamAndGetSize(dataOut, segmentSize);
        int size2 = compressSingleStreamAndGetSize(dataOut, segmentSize);
        int size3 = compressSingleStreamAndGetSize(dataOut, segmentSize);
        compressSingleStream(dataOut, totalBytes - 3 * segmentSize);

        hdod.saveDataSize();

        Out headerOut = hdod.getOutForHeader();
        headerOut.putShort((short) size1);
        headerOut.putShort((short) size2);
        headerOut.putShort((short) size3);

        hdod.close();
    }

    public void compressSingleStream(Out out, int totalBytes) throws IOException {
        try (HuffmanBitOutputStream bos = new HuffmanBitOutputStream(out)) {
            int maxBytes = Math.min(totalBytes, in.available());

            // symbols are encoded from the last to the first one (the decoder reads the bos backward),
            // so the input is read with a backward cursor that starts at its last byte
            BackwardDecorator bd = new BackwardDecorator(in, maxBytes);

            for (int i = 0; i < maxBytes; i++)
                table.encodeSymbol(bos, bd.getByte());
        }
    }

    private int compressSingleStreamAndGetSize(Out out, int totalBytes) throws IOException {
        int offs = out.getOffs();
        compressSingleStream(out, totalBytes);
        return out.getOffs() - offs;
    }
}
