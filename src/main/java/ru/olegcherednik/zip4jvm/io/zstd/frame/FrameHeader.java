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
package ru.olegcherednik.zip4jvm.io.zstd.frame;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.zstd.Buffer;

/**
 * see 3.1.1.1
 *
 * @author Oleg Cherednik
 * @since 21.11.2021
 */
@Getter
@RequiredArgsConstructor
public class FrameHeader {

    private final int headerSize;
    private final long windowSize;
    private final long contentSize;
    private final long dictionaryId;
    private final boolean hasChecksum;

    public static FrameHeader read(Buffer inputBase) {
        final int pos = inputBase.getOffs();
        FrameHeaderDescriptor descriptor = FrameHeaderDescriptor.read(inputBase);
        long windowSize = descriptor.readWindowSize(inputBase);
        long dictionaryId = descriptor.readDictionaryId(inputBase);
        long contentSize = descriptor.readContentSize(inputBase);
        boolean hasChecksum = descriptor.isContentChecksum();

        if (descriptor.isSingleSegment())
            windowSize = contentSize;

        return new FrameHeader(inputBase.getOffs() - pos, windowSize, contentSize, dictionaryId, hasChecksum);
    }

}
