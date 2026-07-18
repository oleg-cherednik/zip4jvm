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

import io.airlift.compress.zstd.Constants;
import io.airlift.compress.zstd.FseCompressionTable;

import static io.airlift.compress.zstd.Constants.MAX_LITERALS_LENGTH_SYMBOL;
import static io.airlift.compress.zstd.Constants.MAX_MATCH_LENGTH_SYMBOL;
import static io.airlift.compress.zstd.Constants.MAX_OFFSET_CODE_SYMBOL;

public class SequenceEncodingContextNew
{
    private static final int MAX_SEQUENCES = Math.max(MAX_LITERALS_LENGTH_SYMBOL, MAX_MATCH_LENGTH_SYMBOL);

    public final FseCompressionTableNew literalLengthTable = new FseCompressionTableNew(Constants.LITERAL_LENGTH_TABLE_LOG, MAX_LITERALS_LENGTH_SYMBOL);
    public final FseCompressionTableNew offsetCodeTable = new FseCompressionTableNew(Constants.OFFSET_TABLE_LOG, MAX_OFFSET_CODE_SYMBOL);
    public final FseCompressionTableNew matchLengthTable = new FseCompressionTableNew(Constants.MATCH_LENGTH_TABLE_LOG, MAX_MATCH_LENGTH_SYMBOL);

    public final int[] counts = new int[MAX_SEQUENCES + 1];
    public final short[] normalizedCounts = new short[MAX_SEQUENCES + 1];
}
