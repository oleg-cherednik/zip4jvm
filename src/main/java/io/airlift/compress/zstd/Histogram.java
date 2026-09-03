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

import java.util.Arrays;

class Histogram {

    private Histogram() {
    }

    // TODO: count parallel heuristic for large inputs
    private static void count1(ByteArrayWithOffs in, int inputSize, int[] counts) {
        long input = 0;

        Arrays.fill(counts, 0);

        for (int i = 0; i < inputSize; i++) {
            int symbol = UnsafeUtil.getByte(in, input) & 0xFF;
            input++;
            counts[symbol]++;
        }
    }

    public static int findLargestCount(int[] counts, int maxSymbol) {
        int max = 0;
        for (int i = 0; i <= maxSymbol; i++) {
            if (counts[i] > max) {
                max = counts[i];
            }
        }

        return max;
    }

    public static int findMaxSymbol(int[] counts, int maxSymbol) {
        while (counts[maxSymbol] == 0) {
            maxSymbol--;
        }
        return maxSymbol;
    }

    public static void count(ByteArrayWithOffs in, int length, int[] counts) {
        count1(in, length, counts);
    }
}
