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

import static io.airlift.compress.zstd.Constants.SIZE_OF_SHORT;

@RequiredArgsConstructor
public class HuffmanCompressor {

    private final In in;

    public int compress4streams(OutByteArrayWithOffs out, int outputSize,
                                final int inOffs, final int inputSize,
                                HuffmanCompressionTable table) throws IOException {
        int outOffs = out.getOffs();
        long inputLimit = inOffs + inputSize;
        int output = outOffs;
        int outputLimit = outOffs + outputSize;

        int segmentSize = (inputSize + 3) / 4;

        if (outputSize < 6 /* jump table */ + 1 /* first stream */ + 1 /* second stream */ + 1 /* third stream */ +
                8 /* 8 bytes minimum needed by the bitstream encoder */) {
            return 0; // minimum space to compress successfully
        }

        if (inputSize <= 6 + 1 + 1 + 1) // jump table + one byte per stream
            return 0;  // no saving possible: input too small

        output += SIZE_OF_SHORT + SIZE_OF_SHORT + SIZE_OF_SHORT; // jump table

        // first segment
        out.setOffs(output);
        int loOffs = out.getOffs();
        compressSingleStream(out, segmentSize, table);
        int compressedSize = out.getOffs() - loOffs;
        if (compressedSize == 0)
            return 0;

        out.putShort(outOffs, (short) compressedSize);
        output += compressedSize;

        // second segment
        out.setOffs(output);
        loOffs = out.getOffs();
        compressSingleStream(out, segmentSize, table);
        compressedSize = out.getOffs() - loOffs;
        if (compressedSize == 0) {
            return 0;
        }
        out.putShort(outOffs + SIZE_OF_SHORT, (short) compressedSize);
        output += compressedSize;

        // third segment
        out.setOffs(output);
        loOffs = out.getOffs();
        compressSingleStream(out, segmentSize, table);
        compressedSize = out.getOffs() - loOffs;
        if (compressedSize == 0) {
            return 0;
        }
        out.putShort(outOffs + SIZE_OF_SHORT + SIZE_OF_SHORT, (short) compressedSize);
        output += compressedSize;

        // fourth segment
        out.setOffs(output);
        loOffs = out.getOffs();
        compressSingleStream(out, (int) (inputLimit - in.getPos()), table);
        compressedSize = out.getOffs() - loOffs;
        if (compressedSize == 0) {
            return 0;
        }
        output += compressedSize;

        return output - outOffs;
    }

    public void compressSingleStream(OutByteArrayWithOffs out, int totalBytes, HuffmanCompressionTable table)
            throws IOException {
        try (HuffmanBitOutputStream bos = new HuffmanBitOutputStream(out)) {

            // symbols are encoded from the last to the first one (the decoder reads the bos backward),
            // so the input is read with a backward cursor that starts at its last byte
            BackwardDecorator bd = new BackwardDecorator(in, totalBytes);

            for (int i = 0; i < totalBytes; i++)
                table.encodeSymbol(bos, bd.getByte());
        }
    }
}
