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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import static io.airlift.compress.zstd.model.Constants.MIN_WINDOW_LOG;
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
@NoArgsConstructor
public class FrameHeader {

    public static final long UNKNOWN_SIZE = -1;
    // the same limit as the default one in the reference implementation (ZSTD_WINDOWLOG_LIMIT_DEFAULT = 27); the
    // decoder may reject bigger windows
    // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.1.2
    public static final int MAX_WINDOW_SIZE = 1 << 27;

    private FrameHeaderDescriptor frameHeaderDescriptor;
    private WindowDescriptor windowDescriptor;

    private int windowSize;
    private long contentSize;
    private long dictionaryId;
    private boolean hasChecksum;

    public FrameHeader(int windowSize, long contentSize, long dictionaryId, boolean hasChecksum) {
        this.windowSize = windowSize;
        this.contentSize = contentSize;
        this.dictionaryId = dictionaryId;
        this.hasChecksum = hasChecksum;
    }

    public static FrameHeader decode(In in) {
        FrameHeader fh = new FrameHeader();
        fh.frameHeaderDescriptor = FrameHeaderDescriptor.decode(in);

        if (!fh.frameHeaderDescriptor.isSingleSegmentFlag())
            fh.windowDescriptor = WindowDescriptor.decode(in);

        long windowSize = readWindowDescriptorAndGetWindowSize(fh);
        long dictionaryId = readDictionaryId(in, fh.frameHeaderDescriptor);
        long frameContentSize = readFrameContentSize(in, fh.frameHeaderDescriptor);

        // if Single_Segment_flag == true => windowSize = frameContentSize
        if (windowSize == UNKNOWN_SIZE)
            windowSize = frameContentSize;

        verify(windowSize != UNKNOWN_SIZE, "Frame content size is unknown");
        verify(windowSize <= MAX_WINDOW_SIZE, "Frame requires too much memory");

        boolean hasChecksum = fh.frameHeaderDescriptor.isContentChecksumFlag();
        return new FrameHeader((int) windowSize, frameContentSize, dictionaryId, hasChecksum);
    }

    private static long readWindowDescriptorAndGetWindowSize(FrameHeader frameHeader) {
        if (frameHeader.frameHeaderDescriptor.isSingleSegmentFlag())
            return UNKNOWN_SIZE;

        WindowDescriptor wd = frameHeader.windowDescriptor;
        int b = wd.b;
        // bit7_3 - Exponent
        int exponent = (b >> 3) & 0b11111;
        // bit2-0 - Mantissa
        int mantissa = b & 0b111;

        long base = 1L << (MIN_WINDOW_LOG + exponent);
        return base + (base / 8) * mantissa;
    }

    private static long readDictionaryId(In in, FrameHeaderDescriptor frameHeaderDescriptor) {
        int dictionaryIdFlag = frameHeaderDescriptor.getDictionaryIdFlag();

        if (dictionaryIdFlag == 0)
            return 0;
        if (dictionaryIdFlag == 1)
            return in.readByte();
        if (dictionaryIdFlag == 2)
            return in.readWord() & 0xFFFF;
        // dictionaryIdFlag == 3
        return in.readDword() & 0xFFFF_FFFFL;
    }

    private static long readFrameContentSize(In in, FrameHeaderDescriptor frameHeaderDescriptor) {
        int frameContentSizeFlag = frameHeaderDescriptor.getFrameContentSizeFlag();

        if (frameContentSizeFlag == 0)
            return frameHeaderDescriptor.isSingleSegmentFlag() ? in.readByte() : UNKNOWN_SIZE;
        if (frameContentSizeFlag == 1)
            return (in.readWord() & 0xFFFF) + 256;
        if (frameContentSizeFlag == 2)
            return in.readDword() & 0xFFFF_FFFFL;
        // frameContentSizeFlag == e
        return in.readQword();
    }


}
