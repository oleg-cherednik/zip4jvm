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
import io.airlift.compress.zstd.In;
import io.airlift.compress.zstd.OutByteArrayWithOffs;
import io.airlift.compress.zstd.bos.HuffmanBitOutputStream;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import static io.airlift.compress.zstd.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.Constants.SIZE_OF_SHORT;

@RequiredArgsConstructor
public class HuffmanCompressor {

    private final In in;
    private final HuffmanCompressionTable table;

    public int compress4streams(OutByteArrayWithOffs out, final int totalBytes) throws IOException {
        if (totalBytes <= 3 * SIZE_OF_SHORT + 3 * SIZE_OF_BYTE) // jump table + one byte per stream
            return 0;  // no saving possible: input too small

        final int loOffs = out.getOffs();
        int segmentSize = (totalBytes + 3) / 4;

        out.putShort((short) 0x0);  // size1
        out.putShort((short) 0x0);  // size2
        out.putShort((short) 0x0);  // size3

        int size1 = compressSingleStream(out, segmentSize);
        int size2 = compressSingleStream(out, segmentSize);
        int size3 = compressSingleStream(out, segmentSize);

        out.setOffs(loOffs);
        out.putShort((short) size1);
        out.putShort((short) size2);
        out.putShort((short) size3);

        out.setOffs(loOffs + size1 + size2 + size3 + 3 * SIZE_OF_SHORT);
        compressSingleStream(out, totalBytes - 3 * segmentSize);
        return out.getOffs() - loOffs;
    }

    public int compressSingleStream(OutByteArrayWithOffs out, int totalBytes) throws IOException {
        final int loOffs = out.getOffs();

        try (HuffmanBitOutputStream bos = new HuffmanBitOutputStream(out)) {
            int maxBytes = Math.min(totalBytes, in.available());

            // symbols are encoded from the last to the first one (the decoder reads the bos backward),
            // so the input is read with a backward cursor that starts at its last byte
            BackwardDecorator bd = new BackwardDecorator(in, maxBytes);

            for (int i = 0; i < maxBytes; i++)
                table.encodeSymbol(bos, bd.getByte());
        }

        return out.getOffs() - loOffs;
    }
}
