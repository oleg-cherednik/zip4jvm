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
package io.airlift.compress.zstd;

import io.airlift.compress.Decompressor;
import io.airlift.compress.MalformedInputException;

public class ZstdDecompressor
        implements Decompressor {

    private final ZstdFrameDecompressor decompressor = new ZstdFrameDecompressor();

    @Override
    public int decompress(ByteArrayWithOffs in,
                          int inputOffset,
                          int inputLength,
                          ByteArrayWithOffs out,
                          int outputOffset,
                          int maxOutputLength)
            throws MalformedInputException {
        long inputAddress = inputOffset;
        long inputLimit = inputAddress + inputLength;
        long outputAddress = outputOffset;
        long outputLimit = outputAddress + maxOutputLength;

        return decompressor.decompress(in, inputAddress, inputLimit, out, outputAddress, outputLimit);
    }

    public static long getDecompressedSize(ByteArrayWithOffs in, int offset, int length) {
        int baseAddress = (int) (offset);
        return ZstdFrameDecompressor.getDecompressedSize(in, baseAddress, baseAddress + length);
    }
}
