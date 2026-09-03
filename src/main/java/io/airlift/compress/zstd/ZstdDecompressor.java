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
    public int decompress(byte[] input,
                          int inputOffset,
                          int inputLength,
                          byte[] output,
                          int outputOffset,
                          int maxOutputLength)
            throws MalformedInputException {
        long inputAddress = inputOffset;
        long inputLimit = inputAddress + inputLength;
        long outputAddress = outputOffset;
        long outputLimit = outputAddress + maxOutputLength;

        return decompressor.decompress(input, inputAddress, inputLimit, output, outputAddress, outputLimit);
    }

    public static long getDecompressedSize(byte[] input, int offset, int length) {
        int baseAddress = (int) (offset);
        return ZstdFrameDecompressor.getDecompressedSize(input, baseAddress, baseAddress + length);
    }
}
