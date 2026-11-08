package io.airlift.compress.zstd;

import ru.olegcherednik.zip4jvm.utils.BitUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 3.1.1.2 Blocks
 * Block_Header (3 bytes)
 */
@Setter
public final class BlockHeader {

    public static final int SIZE = 3;
    public static final int TYPE_RAW_BLOCK = 0;
    public static final int TYPE_RLE_BLOCK = 1;
    public static final int TYPE_COMPRESSED_BLOCK = 2;

    private static final int MASK_2_BITS = 0x3;
    private static final int MASK_21_BITS = 0x1FFFFF;

    @Getter
    private boolean lastBlock;
    private int blockType;
    @Getter
    private int blockSize;

    public static BlockHeader read(In in) {
        int b0 = in.readByte();
        int b1 = in.readByte();
        int b2 = in.readByte();

        int data = b2 << 16 | b1 << 8 | b0;

        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setLastBlock(isLastBlock(data));
        blockHeader.setBlockType(getBlockType(data));
        blockHeader.setBlockSize(getBlockSize(data));

        return blockHeader;
    }

    public int getData() {
        return (lastBlock ? 1 : 0)
                | (blockType & MASK_2_BITS) << 1
                | (blockSize & MASK_21_BITS) << 3;
    }

    public void write(OutByteArrayWithOffs out) {
        int data = getData();

        for (int i = 0; i < 3; i++) {
            out.putByte((byte) (data & 0xFF));
            data >>>= 8;
        }
    }

    public boolean isRawBlock() {
        return blockType == TYPE_RAW_BLOCK;
    }

    public boolean isRleBlock() {
        return blockType == TYPE_RLE_BLOCK;
    }

    public boolean isCompressedBlock() {
        return blockType == TYPE_COMPRESSED_BLOCK;
    }

    private static boolean isLastBlock(int data) {
        // bit0
        return BitUtils.isBitSet(data, BitUtils.BIT0);
    }

    private static int getBlockType(int data) {
        // bit2_1
        return (data >> 1) & 0b11;
    }

    // Block_Size <= min(Window_Size, 128Kb)
    private static int getBlockSize(int data) {
        // bit23_3 - 21 bits
        return (data >> 3) & 0x1F_FFFF;
    }

}
