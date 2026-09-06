package io.airlift.compress.zstd;

import ru.olegcherednik.zip4jvm.utils.BitUtils;

import lombok.Getter;

/**
 * 3.1.1.2 Blocks
 * Block_Header (3 bytes)
 */
@Getter
public final class BlockHeader {

    private final boolean lastBlock;
    private final int type;
    private final int size;

    public BlockHeader(int data) {
        lastBlock = isLastBlock(data);
        type = getType(data);
        size = getSize(data);
    }

    private static boolean isLastBlock(int data) {
        // bit0
        return BitUtils.isBitSet(data, BitUtils.BIT0);
    }

    private static int getType(int data) {
        // bit2_1
        return (data >> 1) & 0b11;
    }

    // Block_Size <= min(Window_Size, 128Kb)
    private static int getSize(int data) {
        // bit23_3
        return (data >> 3) & 0x1F_FFFF; // 21 bits
    }

}
