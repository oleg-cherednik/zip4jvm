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
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.zstd.Buffer;

import static ru.olegcherednik.zip4jvm.io.zstd.Constants.MIN_WINDOW_LOG;

@Getter
@RequiredArgsConstructor
public class FrameHeader {

    private final int headerSize;
    private final int windowSize;
    private final long contentSize;
    private final long dictionaryId;
    private final boolean hasChecksum;

    public static FrameHeader read(Buffer inputBase) {
        final int pos = inputBase.getOffs();
        int frameHeaderDescriptor = inputBase.getByte();

        boolean singleSegment = (frameHeaderDescriptor & 0b100000) != 0;
        int dictionaryDescriptor = frameHeaderDescriptor & 0b11;
        int contentSizeDescriptor = frameHeaderDescriptor >>> 6;

        // decode window size
        int windowSize = -1;
        if (!singleSegment) {
            int windowDescriptor = inputBase.getByte();
            int exponent = windowDescriptor >>> 3;
            int mantissa = windowDescriptor & 0b111;

            int base = 1 << (MIN_WINDOW_LOG + exponent);
            windowSize = base + (base / 8) * mantissa;
        }

        // decode dictionary id
        long dictionaryId = -1;
        switch (dictionaryDescriptor) {
            case 0:
                break;
            case 1:
                dictionaryId = inputBase.getByte();
                break;
            case 2:
                dictionaryId = inputBase.getShort();
                break;
            case 3:
                dictionaryId = inputBase.getInt();
                break;
            default:
                throw new Zip4jvmException("Custom dictionaries not supported");
        }

        // decode content size
        long contentSize = -1;
        switch (contentSizeDescriptor) {
            case 0:
                if (singleSegment) {
                    contentSize = inputBase.getByte();
                }
                break;
            case 1:
                contentSize = inputBase.getShort() + 256L;
                break;
            case 2:
                contentSize = inputBase.getInt();
                break;
            case 3:
                contentSize = inputBase.getLong();
                break;
        }

        boolean hasChecksum = (frameHeaderDescriptor & 0b100) != 0;
        return new FrameHeader(inputBase.getOffs() - pos, windowSize, contentSize, dictionaryId, hasChecksum);
    }

}
