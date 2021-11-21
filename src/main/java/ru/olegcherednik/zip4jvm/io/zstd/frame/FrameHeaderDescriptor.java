package ru.olegcherednik.zip4jvm.io.zstd.frame;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.zstd.Buffer;
import ru.olegcherednik.zip4jvm.utils.BitUtils;

import java.util.function.BiFunction;
import java.util.function.Function;

import static ru.olegcherednik.zip4jvm.io.zstd.Constants.MIN_WINDOW_LOG;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT0;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT1;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT2;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT5;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT6;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT7;

/**
 * see 3.1.1.1.1
 *
 * @author Oleg Cherednik
 * @since 21.11.2021
 */
@Getter
@Setter
public class FrameHeaderDescriptor {

    // total size:1

    // bit:0-1 - Dictionary ID Flag
    private DictionaryIdSize dictionaryIdSize;
    // bit:2 - Content Checksum Flag
    private boolean contentChecksum;
    // bit:3 - reserved
    // bit:4 - unused
    // bit:5 - Single Segment Flag
    private boolean singleSegment;
    // bit:6-7 - Frame Content Size Flag
    private FrameContentSize frameContentSize;

    /** see 3.1.1.1.2 */
    public long readWindowSize(Buffer inputBase) {
        if (singleSegment)
            return -1L;

        long windowDescriptor = inputBase.getByte();
        long exponent = windowDescriptor >> 3;
        long mantissa = windowDescriptor & (BIT0 | BIT1 | BIT2);
        long base = 1 << (MIN_WINDOW_LOG + exponent);
        return base + (base / 8) * mantissa;
    }

    public long readDictionaryId(Buffer inputBase) {
        return dictionaryIdSize.readDictionaryId(inputBase);
    }

    public long readContentSize(Buffer inputBase) {
        return frameContentSize.readContentSize(inputBase, singleSegment);
    }

    public static FrameHeaderDescriptor read(Buffer inputBase) {
        int data = inputBase.getByte();
        FrameHeaderDescriptor descriptor = new FrameHeaderDescriptor();
        descriptor.setDictionaryIdSize(DictionaryIdSize.parseValue(data & (BIT0 | BIT1)));
        descriptor.setSingleSegment(BitUtils.isBitSet(data, BIT2));
        descriptor.setContentChecksum(BitUtils.isBitSet(data, BIT5));
        descriptor.setFrameContentSize(FrameContentSize.parseValue((data & (BIT6 | BIT7)) >> 6));
        return descriptor;
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public enum DictionaryIdSize {
        ZERO(0, inputBase -> -1L),
        BYTE(1, inputBase -> (long)inputBase.getByte()),
        WORD(2, inputBase -> (long)inputBase.getShort()),
        DWORD(3, Buffer::getInt);

        private final int value;
        private final Function<Buffer, Long> func;

        public final long readDictionaryId(Buffer inputBase) {
            return func.apply(inputBase);
        }

        public static DictionaryIdSize parseValue(int value) {
            for (DictionaryIdSize dictionaryId : values())
                if (dictionaryId.value == value)
                    return dictionaryId;
            throw new Zip4jvmException("Custom dictionaries not supported");
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public enum FrameContentSize {
        BYTE(0, (inputBase, singleSegmentFlag) -> singleSegmentFlag == Boolean.TRUE ? inputBase.getByte() : -1L),
        WORD(1, (inputBase, singleSegmentFlag) -> inputBase.getShort() + 256L),
        DWORD(2, (inputBase, singleSegmentFlag) -> inputBase.getInt()),
        QWORD(3, (inputBase, singleSegmentFlag) -> inputBase.getLong());

        private final int value;
        private final BiFunction<Buffer, Boolean, Long> func;

        public final long readContentSize(Buffer inputBase, boolean singleSegment) {
            return func.apply(inputBase, singleSegment);
        }

        public static FrameContentSize parseValue(int value) {
            for (FrameContentSize frameContentSize : values())
                if (frameContentSize.value == value)
                    return frameContentSize;
            throw new EnumConstantNotPresentException(FrameContentSize.class, String.valueOf(value));
        }
    }

}
