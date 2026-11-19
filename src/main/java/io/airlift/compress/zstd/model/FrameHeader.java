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
package io.airlift.compress.zstd.model;

import io.airlift.compress.zstd.In;
import io.airlift.compress.zstd.Out;
import lombok.Getter;
import lombok.Setter;

import static io.airlift.compress.zstd.model.Util.verify;

/**
 * <ul>
 * Frame_Header_Descriptor:
 * <li>[Window_Descriptor]</li>
 * <li>[Dictionary_ID]</li>
 * <li>[Frame_Content_Size]</li>
 * </ul>
 */
@Getter
@Setter
public class FrameHeader {

    public static final long UNKNOWN_SIZE = -1;
    // the same limit as the default one in the reference implementation (ZSTD_WINDOWLOG_LIMIT_DEFAULT = 27); the
    // decoder may reject bigger windows
    // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.1.2
    public static final int MAX_WINDOW_SIZE = 1 << 27;

    private FrameHeaderDescriptor frameHeaderDescriptor;
    private WindowDescriptor windowDescriptor;
    private long dictionaryId;
    private long frameContentSize;

    private int windowSize;

    public static FrameHeader decode(In in) {
        long windowSize = UNKNOWN_SIZE;

        FrameHeader frameHeader = new FrameHeader();
        frameHeader.frameHeaderDescriptor = FrameHeaderDescriptor.decode(in);

        if (!frameHeader.frameHeaderDescriptor.isSingleSegmentFlag()) {
            frameHeader.windowDescriptor = WindowDescriptor.decode(in);
            windowSize = frameHeader.windowDescriptor.getWindowSize();
        }

        frameHeader.dictionaryId = frameHeader.frameHeaderDescriptor.readDictionaryId(in);
        frameHeader.frameContentSize = frameHeader.frameHeaderDescriptor.readFrameContentSize(in);

        // if Single_Segment_flag == true => windowSize = frameContentSize
        if (windowSize == UNKNOWN_SIZE)
            windowSize = frameHeader.frameContentSize;

        verify(windowSize != UNKNOWN_SIZE, "Frame content size is unknown");
        verify(windowSize <= MAX_WINDOW_SIZE, "Frame requires too much memory");

        frameHeader.windowSize = (int) windowSize;

        return frameHeader;
    }

    public void encode(Out out) {
        frameHeaderDescriptor.encode(out);
    }

    public boolean isHasChecksum() {
        return frameHeaderDescriptor.isContentChecksumFlag();
    }

    public boolean isSingleSegment() {
        return frameHeaderDescriptor.isSingleSegmentFlag();
    }

}
