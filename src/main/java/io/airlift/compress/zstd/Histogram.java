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

import lombok.Getter;

public class Histogram {

    @Getter
    private final int[] counts;

    public Histogram(int length) {
        counts = new int[length];
    }

    // TODO: count parallel heuristic for large inputs
    public void count(byte[] buf, int len) {
        for (int i = 0; i < len; i++)
            counts[buf[i] & 0xFF]++;
    }

    public int findMaxSymbol(int maxSymbol) {
        while (counts[maxSymbol] == 0) {
            maxSymbol--;
        }

        return maxSymbol;
    }

    public int findLargestCount(int maxSymbol) {
        int max = 0;

        for (int i = 0; i <= maxSymbol; i++)
            if (counts[i] > max)
                max = counts[i];

        return max;
    }

}
