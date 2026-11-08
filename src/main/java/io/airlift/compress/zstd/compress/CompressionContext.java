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
package io.airlift.compress.zstd.compress;

import io.airlift.compress.zstd.huffman.HuffmanCompressionContext;
import io.airlift.compress.zstd.seq.SequenceEncodingContext;
import io.airlift.compress.zstd.seq.SequenceStore;
import lombok.AllArgsConstructor;

import static io.airlift.compress.zstd.Constants.MAX_BLOCK_SIZE;

public class CompressionContext {

    private final RepeatedOffs[] repeatedOffs = { new RepeatedOffs(1, 0), new RepeatedOffs(4, 0) };
    public final SequenceEncodingContext sequenceEncodingContext = new SequenceEncodingContext();
    public final HuffmanCompressionContext huffmanCompressionContext = new HuffmanCompressionContext();

    public final SequenceStore sequenceStore;
    public final BlockCompressionState blockCompressionState;
    public final CompressionParameters parameters;

    public CompressionContext(CompressionParameters parameters, int inputSize) {
        int windowSize = Math.max(1, Math.min(parameters.getWindowSize(), inputSize));
        int blockSize = Math.min(MAX_BLOCK_SIZE, windowSize);
        int divider = parameters.getSearchLength() == 3 ? 3 : 4;
        int maxSequences = blockSize / divider;

        sequenceStore = new SequenceStore(blockSize, maxSequences);
        blockCompressionState = new BlockCompressionState(parameters);
        this.parameters = parameters;
    }

    public int getOffs0() {
        return repeatedOffs[0].offs;
    }

    public int getOffs1() {
        return repeatedOffs[1].offs;
    }

    public void setOffs0(int offs) {
        repeatedOffs[0].tmp = offs;
    }

    public void setOffs1(int offs) {
        repeatedOffs[1].tmp = offs;
    }

    public void commit() {
        repeatedOffs[0].commit();
        repeatedOffs[1].commit();
        huffmanCompressionContext.saveChanges();
    }

    @AllArgsConstructor
    private static final class RepeatedOffs {

        private int offs;
        private int tmp;

        public void commit() {
            offs = tmp;
        }

    }

}
