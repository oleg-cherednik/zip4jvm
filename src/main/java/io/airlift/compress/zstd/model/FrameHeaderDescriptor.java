package io.airlift.compress.zstd.model;

import ru.olegcherednik.zip4jvm.utils.BitUtils;

import io.airlift.compress.zstd.In;
import io.airlift.compress.zstd.Out;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
@RequiredArgsConstructor
public class FrameHeaderDescriptor {

    private final int dictionaryIdFlag;
    private final boolean contentChecksumFlag;
    private final boolean singleSegmentFlag;
    private final int frameContentSizeFlag;

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

        out.writeByte((byte) (data & 0xFF));
    }

}
