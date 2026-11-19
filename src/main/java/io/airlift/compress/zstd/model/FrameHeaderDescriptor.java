package io.airlift.compress.zstd.model;

import io.airlift.compress.zstd.In;
import io.airlift.compress.zstd.Out;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT0;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT1;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT2;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT5;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT6;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT7;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.getValue;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.isBitSet;

/**
 * see: 3.1.1.1.1
 *
 * @author Oleg Cherednik
 * @since 18.11.2026
 */
@Getter
@Setter
public class FrameHeaderDescriptor {

    private int dictionaryIdFlag;
    private boolean contentChecksumFlag;
    private boolean singleSegmentFlag;
    @Setter(AccessLevel.NONE)
    private int frameContentSizeFlag;

    public static FrameHeaderDescriptor decode(In in) {
        int b = in.readByte();

        FrameHeaderDescriptor descriptor = new FrameHeaderDescriptor();
        descriptor.dictionaryIdFlag = getValue(b, BIT0 | BIT1);
        descriptor.contentChecksumFlag = isBitSet(b, BIT2);
        descriptor.singleSegmentFlag = isBitSet(b, BIT5);
        descriptor.frameContentSizeFlag = getValue(b, BIT6 | BIT7);

        return descriptor;
    }

    public void encode(Out out) {
        int data = (frameContentSizeFlag & 0b11) << 6
                | (singleSegmentFlag ? 1 : 0) << 5
                | (contentChecksumFlag ? 1 : 0) << 2
                | (dictionaryIdFlag & 0b11);

        out.putByte((byte) (data & 0xFF));
    }

    public long readFrameContentSize(In in) {
        if (frameContentSizeFlag == 0)
            return singleSegmentFlag ? in.readByte() : FrameHeader.UNKNOWN_SIZE;
        if (frameContentSizeFlag == 1)
            return (in.readWord() & 0xFFFF) + 256;
        if (frameContentSizeFlag == 2)
            return in.readDword() & 0xFFFF_FFFFL;
        // frameContentSizeFlag == e
        return in.readQword();
    }

    public void withContentSize(int contentSize) {
        frameContentSizeFlag = (contentSize > 0xFF ? 1 : 0)
                + (contentSize >= 65536 + 256 ? 1 : 0);
    }

}
