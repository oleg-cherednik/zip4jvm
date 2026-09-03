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

import io.airlift.compress.Compressor;

import static io.airlift.compress.zstd.Constants.MAX_BLOCK_SIZE;
import static sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET;

public class ZstdCompressor
        implements Compressor {

    @Override
    public int maxCompressedLength(int uncompressedSize) {
        int result = uncompressedSize + (uncompressedSize >>> 8);

        if (uncompressedSize < MAX_BLOCK_SIZE) {
            result += (MAX_BLOCK_SIZE - uncompressedSize) >>> 11;
        }

        return result;
    }

    @Override
    public int compress(byte[] input,
                        int inputOffset,
                        int inputLength,
                        byte[] output,
                        int outputOffset,
                        int maxOutputLength) {
        long inputAddress = ARRAY_BYTE_BASE_OFFSET + inputOffset;
        long outputAddress = ARRAY_BYTE_BASE_OFFSET + outputOffset;

        return ZstdFrameCompressor.compress(input,
                                            inputAddress,
                                            inputAddress + inputLength,
                                            output,
                                            outputAddress,
                                            outputAddress + maxOutputLength,
                                            CompressionParameters.DEFAULT_COMPRESSION_LEVEL);
    }

}
