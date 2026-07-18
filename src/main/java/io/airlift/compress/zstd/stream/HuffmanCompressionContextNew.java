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
package io.airlift.compress.zstd.stream;

import io.airlift.compress.zstd.Huffman;
import io.airlift.compress.zstd.HuffmanCompressionTableWorkspace;

public class HuffmanCompressionContextNew
{
    private final HuffmanTableWriterWorkspaceNew tableWriterWorkspace = new HuffmanTableWriterWorkspaceNew();
    private final HuffmanCompressionTableWorkspace compressionTableWorkspace = new HuffmanCompressionTableWorkspace();

    private HuffmanCompressionTableNew previousTable = new HuffmanCompressionTableNew(Huffman.MAX_SYMBOL_COUNT);
    private HuffmanCompressionTableNew temporaryTable = new HuffmanCompressionTableNew(Huffman.MAX_SYMBOL_COUNT);

    private HuffmanCompressionTableNew previousCandidate = previousTable;
    private HuffmanCompressionTableNew temporaryCandidate = temporaryTable;

    public HuffmanCompressionTableNew getPreviousTable()
    {
        return previousTable;
    }

    public HuffmanCompressionTableNew borrowTemporaryTable()
    {
        previousCandidate = temporaryTable;
        temporaryCandidate = previousTable;

        return temporaryTable;
    }

    public void discardTemporaryTable()
    {
        previousCandidate = previousTable;
        temporaryCandidate = temporaryTable;
    }

    public void saveChanges()
    {
        temporaryTable = temporaryCandidate;
        previousTable = previousCandidate;
    }

    public HuffmanCompressionTableWorkspace getCompressionTableWorkspace()
    {
        return compressionTableWorkspace;
    }

    public HuffmanTableWriterWorkspaceNew getTableWriterWorkspace()
    {
        return tableWriterWorkspace;
    }
}
