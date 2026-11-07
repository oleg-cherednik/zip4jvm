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

    public void compress4streams(OutByteArrayWithOffs out,
                                 final int inOffs, final int inputSize) throws IOException {
        final int outOffs = out.getOffs();
        long inputLimit = inOffs + inputSize;
        int output = outOffs;
        int segmentSize = (inputSize + 3) / 4;

        if (inputSize <= 3 * SIZE_OF_SHORT + 3 * SIZE_OF_BYTE) // jump table + one byte per stream
            return;  // no saving possible: input too small

        out.putShort((short) 0x0);  // size1
        out.putShort((short) 0x0);  // size2
        out.putShort((short) 0x0);  // size3

        output += SIZE_OF_SHORT + SIZE_OF_SHORT + SIZE_OF_SHORT; // jump table

        // first segment
        out.setOffs(output);
        int loOffs = out.getOffs();
        compressSingleStream(out, segmentSize);
        int size1 = out.getOffs() - loOffs;

        if (size1 == 0)
            return;

        out.putShort(outOffs, (short) size1);
        output += size1;

        // second segment
        out.setOffs(output);
        loOffs = out.getOffs();
        compressSingleStream(out, segmentSize);
        int size2 = out.getOffs() - loOffs;
        if (size2 == 0) {
            return;
        }
        out.putShort(outOffs + SIZE_OF_SHORT, (short) size2);
        output += size2;

        // third segment
        out.setOffs(output);
        loOffs = out.getOffs();
        compressSingleStream(out, segmentSize);
        int size3 = out.getOffs() - loOffs;
        if (size3 == 0) {
            return;
        }
        out.putShort(outOffs + SIZE_OF_SHORT + SIZE_OF_SHORT, (short) size3);
        output += size3;

        // fourth segment
        out.setOffs(output);
        compressSingleStream(out, (int) (inputLimit - in.getPos()));
    }

    public void compressSingleStream(OutByteArrayWithOffs out, int totalBytes) throws IOException {
        try (HuffmanBitOutputStream bos = new HuffmanBitOutputStream(out)) {
            int maxBytes = Math.min(totalBytes, in.available());

            // symbols are encoded from the last to the first one (the decoder reads the bos backward),
            // so the input is read with a backward cursor that starts at its last byte
            BackwardDecorator bd = new BackwardDecorator(in, maxBytes);

            for (int i = 0; i < maxBytes; i++)
                table.encodeSymbol(bos, bd.getByte());
        }
    }
}
