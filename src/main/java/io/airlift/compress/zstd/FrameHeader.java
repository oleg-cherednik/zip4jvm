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
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FrameHeader {

    public static final long UNKNOWN_SIZE = -1;
    // the same limit as the default one in the reference implementation (ZSTD_WINDOWLOG_LIMIT_DEFAULT = 27); the
    // decoder may reject bigger windows
    // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.1.2
    public static final int MAX_WINDOW_SIZE = 1 << 27;

    private final int windowSize;
    private final long contentSize;
    private final long dictionaryId;
    private final boolean hasChecksum;

}
