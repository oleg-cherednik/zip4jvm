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

import lombok.Getter;

import java.util.Arrays;

public class BlockCompressionState {

    public final int[] hashTable;
    public final int[] chainTable;

    // starting point of the window with respect to baseAddress
    @Getter
    private int windowBaseOffs;

    public BlockCompressionState(CompressionParameters parameters) {
        hashTable = new int[1 << parameters.getHashLog()];
        chainTable = new int[1 << parameters.getChainLog()];
    }

    public void reset() {
        Arrays.fill(hashTable, 0);
        Arrays.fill(chainTable, 0);
    }

    public void enforceMaxDistance(long inputLimit, int maxDistance) {
        int newOffs = (int) inputLimit - maxDistance;

        if (windowBaseOffs < newOffs)
            windowBaseOffs = newOffs;
    }

    /**
     * After the input window slides left by {@code delta}, rebase absolute indices stored in the hash tables and
     * {@link #windowBaseOffs}. Entries that would point before the new buffer start are cleared.
     */
    public void rebase(int delta) {
        if (delta <= 0)
            return;

        rebaseTable(hashTable, delta);
        rebaseTable(chainTable, delta);
        windowBaseOffs = Math.max(0, windowBaseOffs - delta);
    }

    private static void rebaseTable(int[] table, int delta) {
        for (int i = 0; i < table.length; i++) {
            int v = table[i];
            table[i] = v >= delta ? v - delta : 0;
        }
    }

}
