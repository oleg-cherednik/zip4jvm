package io.airlift.compress.zstd;

import ru.olegcherednik.zip4jvm.utils.BitUtils;

import io.airlift.compress.zstd.io.Out;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * see: 3.1.1.2
 * Block_Header (3 bytes)
 */
@Setter
public final class BlockHeader {

    public static final int HEADER_SIZE = 3;

    private static final int MASK_BLOCK_TYPE = 0b11;
    private static final int MASK_BLOCK_SIZE = 0x1F_FFFF;

    @Getter
    private boolean lastBlock;
    private BlockType blockType;
    @Getter
    private int blockSize;

    public static BlockHeader decode(In in) {
        int b0 = in.readByte();
        int b1 = in.readByte();
        int b2 = in.readByte();

        int data = b2 << 16 | b1 << 8 | b0;

        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setLastBlock(BitUtils.isBitSet(data, BitUtils.BIT0));
        blockHeader.setBlockType(BlockType.parseId((data >> 1) & MASK_BLOCK_TYPE));
        blockHeader.setBlockSize((data >> 3) & MASK_BLOCK_SIZE);

        return blockHeader;
    }

    public void encode(Out out) {
        int data = (blockSize & MASK_BLOCK_SIZE) << 3
                | (blockType.id & MASK_BLOCK_TYPE) << 1
                | (lastBlock ? 1 : 0);

        for (int i = 0; i < HEADER_SIZE; i++) {
            out.putByte((byte) (data & 0xFF));
            data >>>= 8;
        }
    }

    public boolean isRawBlock() {
        return blockType == BlockType.RAW_BLOCK;
    }

    public boolean isRleBlock() {
        return blockType == BlockType.RLE_BLOCK;
    }

    public boolean isCompressedBlock() {
        return blockType == BlockType.COMPRESSED_BLOCK;
    }

    @RequiredArgsConstructor
    public enum BlockType {
        /**
         * This is an uncompressed block.
         * Block_Content contains Block_Size bytes.
         */
        RAW_BLOCK(0),
        /**
         * This is a single byte, repeated Block_Size times.
         * Block_Content consists of a single byte. On the decompression side,
         * this byte must be repeated Block_Size times.
         */
        RLE_BLOCK(1),
        /**
         * This is a compressed block as described in Section 3.1.1.3.
         * Block_Size is the length of Block_Content, namely the compressed
         * data. The decompressed size is not known, but its maximum possible
         * value is guaranteed (see below).
         */
        COMPRESSED_BLOCK(2);

        private final int id;

        public static BlockType parseId(int id) {
            for (BlockType blockType : values())
                if (blockType.id == id)
                    return blockType;

            throw new EnumConstantNotPresentException(BlockType.class, String.valueOf(id));
        }

    }

}
