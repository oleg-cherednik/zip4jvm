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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static Builder builder() {
        return new Builder();
    }

    public static FrameHeader decode(In in) {
        FrameHeaderDescriptor frameHeaderDescriptor = FrameHeaderDescriptor.decode(in);
        WindowDescriptor windowDescriptor =
                frameHeaderDescriptor.isSingleSegmentFlag() ? null : WindowDescriptor.decode(in);

        long dictionaryId = readDictionaryId(in, frameHeaderDescriptor);
        long frameContentSize = readFrameContentSize(in, frameHeaderDescriptor);
        long windowSize = windowDescriptor == null ? frameContentSize : windowDescriptor.getWindowSize();

        verify(windowSize != UNKNOWN_SIZE, "Frame content size is unknown");
        verify(windowSize <= MAX_WINDOW_SIZE, "Frame requires too much memory");

        FrameHeader frameHeader = new FrameHeader();
        frameHeader.frameHeaderDescriptor = frameHeaderDescriptor;
        frameHeader.windowDescriptor = windowDescriptor;
        frameHeader.dictionaryId = dictionaryId;
        frameHeader.frameContentSize = frameContentSize;
        frameHeader.windowSize = (int) windowSize;
        return frameHeader;
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
        boolean singleSegmentFlag = frameHeaderDescriptor.isSingleSegmentFlag();
        int frameContentSizeFlag = frameHeaderDescriptor.getFrameContentSizeFlag();

        if (frameContentSizeFlag == 0)
            return singleSegmentFlag ? in.readByte() : UNKNOWN_SIZE;
        if (frameContentSizeFlag == 1)
            return (in.readWord() & 0xFFFF) + 256;
        if (frameContentSizeFlag == 2)
            return in.readDword() & 0xFFFF_FFFFL;
        // frameContentSizeFlag == 3
        return in.readQword();
    }

//    private static void writeFrameContentSize(FrameHeaderDescriptor frameHeaderDescriptor,
//                                              int contentSize) {
//        boolean singleSegmentFlag = frameHeaderDescriptor.isSingleSegmentFlag();
//        int frameContentSizeFlag = frameHeaderDescriptor.getFrameContentSizeFlag();
//
//        if (frameContentSizeFlag == 0) {
//            if (singleSegment)
//                out.putByte((byte) contentSize);
//        } else if (frameContentSizeFlag == 1)
//            out.putShort((short) (contentSize - 256));
//        else if (frameContentSizeFlag == 2)
//            out.putInt(contentSize);
//        else
//            throw new AssertionError();
//    }

    public void encode(Out out) {
        frameHeaderDescriptor.encode(out);

        if (windowDescriptor != null)
            windowDescriptor.encode(out);
    }

    public boolean isHasChecksum() {
        return frameHeaderDescriptor.isContentChecksumFlag();
    }

    public boolean isSingleSegment() {
        return frameHeaderDescriptor.isSingleSegmentFlag();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private FrameHeaderDescriptor frameHeaderDescriptor;
        private WindowDescriptor windowDescriptor;
        private long dictionaryId;
        private long frameContentSize;
        private long windowSize;

        public FrameHeader build() {
            return new FrameHeader(frameHeaderDescriptor, windowDescriptor, dictionaryId,
                                   frameContentSize, (int) windowSize);
        }

        public Builder decode(In in) {
            frameHeaderDescriptor = FrameHeaderDescriptor.decode(in);

            if (!frameHeaderDescriptor.isSingleSegmentFlag())
                windowDescriptor = WindowDescriptor.decode(in);
            dictionaryId = readDictionaryId(in);
            frameContentSize = readFrameContentSize(in);
            windowSize = windowDescriptor == null ? frameContentSize : windowDescriptor.getWindowSize();

            verify(windowSize != UNKNOWN_SIZE, "Frame content size is unknown");
            verify(windowSize <= MAX_WINDOW_SIZE, "Frame requires too much memory");

            FrameHeader frameHeader = new FrameHeader();
            frameHeader.frameHeaderDescriptor = frameHeaderDescriptor;
            frameHeader.windowDescriptor = windowDescriptor;
            frameHeader.dictionaryId = dictionaryId;
            frameHeader.frameContentSize = frameContentSize;
            frameHeader.windowSize = (int) windowSize;
            return this;
        }

        private long readDictionaryId(In in) {
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

        private long readFrameContentSize(In in) {
            boolean singleSegmentFlag = frameHeaderDescriptor.isSingleSegmentFlag();
            int frameContentSizeFlag = frameHeaderDescriptor.getFrameContentSizeFlag();

            if (frameContentSizeFlag == 0)
                return singleSegmentFlag ? in.readByte() : UNKNOWN_SIZE;
            if (frameContentSizeFlag == 1)
                return (in.readWord() & 0xFFFF) + 256;
            if (frameContentSizeFlag == 2)
                return in.readDword() & 0xFFFF_FFFFL;
            // frameContentSizeFlag == 3
            return in.readQword();
        }

    }

}
