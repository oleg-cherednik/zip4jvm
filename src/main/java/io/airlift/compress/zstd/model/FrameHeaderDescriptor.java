package io.airlift.compress.zstd.model;

import ru.olegcherednik.zip4jvm.utils.BitUtils;

import io.airlift.compress.zstd.In;
import io.airlift.compress.zstd.Out;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT0;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT1;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT2;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT5;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT6;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT7;

/**
 * see: 3.1.1.1.1
 *
 * @author Oleg Cherednik
 * @since 18.11.2026
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FrameHeaderDescriptor {

    private int dictionaryIdFlag;
    private boolean contentChecksumFlag;
    private boolean singleSegmentFlag;
    //    @Setter(AccessLevel.NONE)
    private int frameContentSizeFlag;

    public static FrameHeaderDescriptor decode(In in) {
        int b = in.readByte();

        int dictionaryIdFlag = BitUtils.getValue(b, BIT0 | BIT1);
        boolean contentChecksumFlag = BitUtils.isBitSet(b, BIT2);
        boolean singleSegmentFlag = BitUtils.isBitSet(b, BIT5);
        int frameContentSizeFlag = BitUtils.getValue(b, BIT6 | BIT7);

        return new FrameHeaderDescriptor(dictionaryIdFlag, contentChecksumFlag,
                                         singleSegmentFlag, frameContentSizeFlag);
    }

    public void encode(Out out) {
        int data = (frameContentSizeFlag & 0b11) << 6
                | (singleSegmentFlag ? 1 : 0) << 5
                | (contentChecksumFlag ? 1 : 0) << 2
                | (dictionaryIdFlag & 0b11);

        out.putByte((byte) (data & 0xFF));
    }

    public void withContentSize(int contentSize) {
        frameContentSizeFlag = (contentSize > 0xFF ? 1 : 0)
                + (contentSize >= 65536 + 256 ? 1 : 0);
    }

}
