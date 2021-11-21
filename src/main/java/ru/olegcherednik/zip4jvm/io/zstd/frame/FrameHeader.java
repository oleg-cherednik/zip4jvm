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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.zstd.Buffer;
import ru.olegcherednik.zip4jvm.utils.BitUtils;

import java.util.function.BiFunction;

import static ru.olegcherednik.zip4jvm.io.zstd.Constants.MIN_WINDOW_LOG;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT0;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT1;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT2;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT5;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT6;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT7;

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
    private final int windowSize;
    private final long contentSize;
    private final long dictionaryId;
    private final boolean hasChecksum;

    public static FrameHeader read(Buffer inputBase) {
        final int pos = inputBase.getOffs();
        FrameHeader.Descriptor descriptor = FrameHeader.Descriptor.read(inputBase);

        // decode window size
        int windowSize = -1;
        if (!descriptor.isSingleSegment()) {
            int windowDescriptor = inputBase.getByte();
            int exponent = windowDescriptor >>> 3;
            int mantissa = windowDescriptor & 0b111;

            int base = 1 << (MIN_WINDOW_LOG + exponent);
            windowSize = base + (base / 8) * mantissa;
        }

        // decode dictionary id
        long dictionaryId = -1;
        switch (descriptor.getDictionaryId()) {
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
        long contentSize = descriptor.readContentSize(inputBase);
        boolean hasChecksum = descriptor.isContentChecksum();
        return new FrameHeader(inputBase.getOffs() - pos, windowSize, contentSize, dictionaryId, hasChecksum);
    }

    /** @see 3.1.1.1.1 */
    @Getter
    @Setter
    public static final class Descriptor {

        // total size:1

        // bitNum:0-1 - Dictionary ID Flag
        private int dictionaryId;
        // bitNum:2 - Content Checksum Flag
        private boolean contentChecksum;
        // bitNum:3 - reserved
        // bitNum:4 - unused
        // bitNum:5 - Single Segment Flag
        private boolean singleSegment;
        // bitNum:6-7 - Frame Content Size Flag
        private FrameContentSize frameContentSize;

        public long readContentSize(Buffer inputBase) {
            return frameContentSize.readContentSize(inputBase, singleSegment);
        }

        public static Descriptor read(Buffer inputBase) {
            int data = inputBase.getByte();
            Descriptor descriptor = new Descriptor();
            descriptor.setDictionaryId(data & (BIT0 | BIT1));
            descriptor.setSingleSegment(BitUtils.isBitSet(data, BIT2));
            descriptor.setContentChecksum(BitUtils.isBitSet(data, BIT5));
            descriptor.setFrameContentSize(FrameContentSize.parseValue((data & (BIT6 | BIT7)) >> 6));
            return descriptor;
        }

    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public enum FrameContentSize {
        BYTE(0, (inputBase, singleSegment) -> singleSegment == Boolean.TRUE ? inputBase.getByte() : -1L),
        WORD(1, (inputBase, singleSegment) -> inputBase.getShort() + 256L),
        DWORD(2, (inputBase, singleSegment) -> inputBase.getInt()),
        QWORD(3, (inputBase, singleSegment) -> inputBase.getLong());

        private final int value;
        private final BiFunction<Buffer, Boolean, Long> readContentSize;

        public final long readContentSize(Buffer inputBase, boolean singleSegment) {
            return readContentSize.apply(inputBase, singleSegment);
        }

        public static FrameContentSize parseValue(int value) {
            for (FrameContentSize frameContentSize : values())
                if (frameContentSize.value == value)
                    return frameContentSize;
            throw new EnumConstantNotPresentException(FrameContentSize.class, String.valueOf(value));
        }
    }

}
