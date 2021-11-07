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
package ru.olegcherednik.zip4jvm.io.zstd;

import java.nio.ByteBuffer;

import static sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET;

public class ZstdDecompressor {

    public void decompress(ByteBuffer input, ByteBuffer output) throws MalformedInputException {
        byte[] inputBase = input.array();
        long inputAddress = ARRAY_BYTE_BASE_OFFSET + input.arrayOffset() + input.position();
        long inputLimit = ARRAY_BYTE_BASE_OFFSET + input.arrayOffset() + input.limit();

        byte[] outputBase = output.array();
        long outputAddress = ARRAY_BYTE_BASE_OFFSET + output.arrayOffset() + output.position();
        long outputLimit = ARRAY_BYTE_BASE_OFFSET + output.arrayOffset() + output.limit();

        int written = new ZstdFrameDecompressor().decompress(inputBase, inputAddress, inputLimit, outputBase, outputAddress, outputLimit);
        output.position(output.position() + written);
    }

}
