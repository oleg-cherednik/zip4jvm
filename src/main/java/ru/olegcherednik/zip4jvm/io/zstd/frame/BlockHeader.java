package ru.olegcherednik.zip4jvm.io.zstd.frame;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.zstd.Buffer;

/**
 * see 3.1.1.2
 *
 * @author Oleg Cherednik
 * @since 08.11.2021
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockHeader {

    // size:3
    // bit:0
    private final boolean lastBlock;
    // bit:1-2
    private final BlockType blockType;
    // bit:3-23
    // when type == COMPRESSED or RAW - size of Block_Content
    // when type == RLE - the number of times the single byte must be repeated.
    // limited: min of Window_Size and 128Kb
    private final int blockSize;

    public static BlockHeader read(Buffer inputBase) {
        int data = inputBase.get3Bytes();
        boolean last = (data & 1) != 0;
        BlockType blockType = BlockType.parseId((data >>> 1) & 0b11);
        int size = (data >>> 3) & 0x1F_FFFF; // 21 bits

        return new BlockHeader(last, blockType, size);
    }

}
