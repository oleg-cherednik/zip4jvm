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

import lombok.Getter;

public class HuffmanCompressionContext {

    @Getter
    private final HuffmanCompressionTableWorkspace compressionTableWorkspace = new HuffmanCompressionTableWorkspace();

    @Getter
    private HuffmanCompressionTable prevTable = new HuffmanCompressionTable(HuffmanDecompressor.MAX_SYMBOL_COUNT);
    private HuffmanCompressionTable temporaryTable = new HuffmanCompressionTable(HuffmanDecompressor.MAX_SYMBOL_COUNT);

    private HuffmanCompressionTable prevCandidate = prevTable;
    private HuffmanCompressionTable temporaryCandidate = temporaryTable;

    public HuffmanCompressionTable borrowTemporaryTable() {
        prevCandidate = temporaryTable;
        temporaryCandidate = prevTable;
        return temporaryTable;
    }

    public void discardTemporaryTable() {
        prevCandidate = prevTable;
        temporaryCandidate = temporaryTable;
    }

    public void saveChanges() {
        temporaryTable = temporaryCandidate;
        prevTable = prevCandidate;
    }

}
