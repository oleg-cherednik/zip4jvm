package io.airlift.compress.zstd;

import ru.olegcherednik.zip4jvm.utils.BitUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 3.1.1.2 Blocks
 * Block_Header (3 bytes)
 */
@Getter
@Setter
@NoArgsConstructor
public final class BlockHeader {

    public static final int SIZE = 3;
    public static final int TYPE_RAW_BLOCK = 0;
    public static final int TYPE_RLE_BLOCK = 1;
    public static final int TYPE_COMPRESSED_BLOCK = 2;

    private static final int MASK_2_BITS = 0x3;
    private static final int MASK_21_BITS = 0x1FFFFF;

    private boolean last;
    private int type;
    private int size;

    public BlockHeader(int data) {
        last = isLast(data);
        type = getType(data);
        size = getSize(data);
    }

    public int getData() {
        return (last ? 1 : 0)
                | (type & MASK_2_BITS) << 1
                | (size & MASK_21_BITS) << 3;
    }

    private static boolean isLast(int data) {
        // bit0
        return BitUtils.isBitSet(data, BitUtils.BIT0);
    }

    private static int getType(int data) {
        // bit2_1
        return (data >> 1) & 0b11;
    }

    // Block_Size <= min(Window_Size, 128Kb)
    private static int getSize(int data) {
        // bit23_3 - 21 bits
        return (data >> 3) & 0x1F_FFFF;
    }

}
