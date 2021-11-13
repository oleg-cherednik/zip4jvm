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

import ru.olegcherednik.zip4jvm.io.zstd.frame.BlockCompressionState;
import ru.olegcherednik.zip4jvm.io.zstd.frame.RepeatedOffsets;
import ru.olegcherednik.zip4jvm.io.zstd.frame.SequenceStore;

public interface BlockCompressor
{
    BlockCompressor UNSUPPORTED = (inputBase, inputAddress, inputSize, sequenceStore, blockCompressionState, offsets, parameters) -> { throw new UnsupportedOperationException(); };

    int compressBlock(byte[] inputBase, int inputAddress, int inputSize, SequenceStore output, BlockCompressionState state, RepeatedOffsets offsets, CompressionParameters parameters);
}
