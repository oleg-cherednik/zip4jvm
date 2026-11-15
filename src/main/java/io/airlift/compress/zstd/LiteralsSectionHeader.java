package io.airlift.compress.zstd;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <ul>
 * <tt>ВCBA</tt>
 * <li><b>A</b> - Literals_Block_Type</li>
 * <li><b>B</b> - Size_Format</li>
 * <li><b>C</b> - Regenerated_Size</li>
 * <li><b>D</b> - Compressed_Size</li>
 * </ul>
 * <p>
 * see: 3.1.1.3.1.1
 *
 * @author Oleg Cherednik
 * @since 12.11.2026
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class LiteralsSectionHeader {

    private static final int MASK_BLOCK_TYPE = 0b11;
    private static final int MASK_SIZE_FORMAT_1BIT = 0b1;
    private static final int MASK_SIZE_FORMAT_2BITS = 0b11;

    @Getter
    protected final BlockType blockType;
    @Getter
    protected int sizeFormat;
    @Getter
    protected int regeneratedSize;
    protected int sizeMask;

    @Getter
    protected int headerSize;

    public static LiteralsSectionHeader read(In in) {
        return read(in.readByte(), in);
    }

    public static LiteralsSectionHeader read(int b0, In in) {
        BlockType blockType = BlockType.parseId(b0 & MASK_BLOCK_TYPE);

        if (blockType == BlockType.RAW_LITERALS_BLOCK || blockType == BlockType.RLE_LITERALS_BLOCK)
            return new RawRle(blockType).read1(b0, in);
        return new CompressedTreeless(blockType).read1(b0, in);
    }

    public static RawRle createForRaw() {
        return new RawRle(BlockType.RAW_LITERALS_BLOCK);
    }

    public static RawRle createForRle() {
        return new RawRle(BlockType.RLE_LITERALS_BLOCK);
    }

    public static CompressedTreeless createForCompressed() {
        return new CompressedTreeless(BlockType.COMPRESSED_LITERALS_BLOCK);
    }

    public static CompressedTreeless createForTreeless() {
        return new CompressedTreeless(BlockType.TREELESS_LITERALS_BLOCK);
    }

    public abstract void encode(OutByteArrayWithOffs out);

    @RequiredArgsConstructor
    public enum BlockType {
        /**
         * Literals are stored uncompressed.
         * Literals_Section_Content is Regenerated_Size.
         */
        RAW_LITERALS_BLOCK(0),
        /**
         * Literals consist of a single-byte value repeated
         * Regenerated_Size times. Literals_Section_Content is 1.
         */
        RLE_LITERALS_BLOCK(1),
        /**
         * This is a standard Huffman-compressed block, starting
         * with a Huffman tree description. See details below.
         * Literals_Section_Content is Compressed_Size.
         */
        COMPRESSED_LITERALS_BLOCK(2),
        /**
         * This is a Huffman-compressed block, using the Huffman
         * tree from the previous Compressed_Literals_Block or a
         * dictionary if there is no previous Huffman-compressed
         * literals block. Huffman_Tree_Description will be skipped.
         * Note that if this mode is triggered without any previous
         * Huffman table in the frame (or dictionary, per Section 5),
         * it should be treated as data corruption.Literals_Section_Content
         * is Compressed_Size.
         */
        TREELESS_LITERALS_BLOCK(3);

        private final int id;

        public static BlockType parseId(int id) {
            for (BlockType blockType : values())
                if (blockType.id == id)
                    return blockType;

            throw new EnumConstantNotPresentException(BlockType.class, String.valueOf(id));
        }

    }

    public static final class RawRle extends LiteralsSectionHeader {

        private static final int MASK_5_BITS = 0b1_1111;
        private static final int MASK_12_BITS = 0b1111_1111_1111;
        private static final int MASK_20_BITS = 0b1111_1111_1111_1111_1111;

        public RawRle read1(int b0, In in) {
            int sizeFormat = (b0 >> 2) & 0b11;

            if (sizeFormat == 0b00 || sizeFormat == 0b10) {
                this.sizeFormat = 0b0;
                sizeMask = MASK_5_BITS;
                regeneratedSize = (b0 >> 3) & sizeMask;
            } else if (sizeFormat == 0b01) {
                int b1 = in.readByte();

                this.sizeFormat = 0b01;
                sizeMask = MASK_12_BITS;
                regeneratedSize = (b1 << 8 | b0) >> 4;
            } else {    // sizeFormat == 0b11
                int b1 = in.readByte();
                int b2 = in.readByte();

                this.sizeFormat = 0b11;
                sizeMask = MASK_20_BITS;
                regeneratedSize = (b2 << 16 | b1 << 8 | b0) >> 4;
            }

            return this;
        }

        private RawRle(BlockType blockType) {
            super(blockType);
        }

        public void withRegeneratedSize(int regeneratedSize) {
            this.regeneratedSize = regeneratedSize;

            if (regeneratedSize <= MASK_5_BITS) {
                sizeFormat = 0b0;   // 1 bit
                sizeMask = MASK_5_BITS;
                headerSize = 1;
            } else if (regeneratedSize <= MASK_12_BITS) {
                sizeFormat = 0b01;  // 2 bits
                sizeMask = MASK_12_BITS;
                headerSize = 2;
            } else if (regeneratedSize <= MASK_20_BITS) {
                sizeFormat = 0b11;  // 2 bits
                sizeMask = MASK_20_BITS;
                headerSize = 3;
            } else
                throw new RuntimeException("regeneratedSize = " + regeneratedSize + " is too big");
        }

        @Override
        public void encode(OutByteArrayWithOffs out) {
            int d;

            if (headerSize == 1)
                // format in bits [1 byte]: CCCCCBAA
                d = (regeneratedSize & sizeMask) << 3
                        | (sizeFormat & MASK_SIZE_FORMAT_1BIT) << 2
                        | (blockType.id & MASK_BLOCK_TYPE);
            else
                // format in bits [2 bytes]: CCCCCCCC_CCCCBBAA
                // format in bits [3 bytes]: CCCCCCCC_CCCCCCCC_CCCCBBAA
                d = (regeneratedSize & sizeMask) << 4
                        | (sizeFormat & MASK_SIZE_FORMAT_2BITS) << 2
                        | (blockType.id & MASK_BLOCK_TYPE);

            for (int i = 0; i < headerSize; i++) {
                out.putByte((byte) (d & 0xFF));
                d >>>= 8;
            }
        }

    }

    public static final class CompressedTreeless extends LiteralsSectionHeader {

        private static final int MASK_10_BITS = 0b11_1111_1111;
        private static final int MASK_14_BITS = 0b11_1111_1111_1111;
        private static final int MASK_18_BITS = 0b11_1111_1111_1111_1111;

        // 0-18 bits
        @Getter
        private int compressedSize;

        public CompressedTreeless read1(int b0, In in) {
            sizeFormat = (b0 >> 2) & MASK_SIZE_FORMAT_2BITS;

            if (sizeFormat == 0b00 || sizeFormat == 0b01) {
                int b1 = in.readByte();
                int b2 = in.readByte();

                sizeMask = MASK_10_BITS;

                int data = b2 << 16 | b1 << 8 | b0;
                regeneratedSize = (data >>> 4) & sizeMask;
                compressedSize = (data >>> 14) & sizeMask;
            } else if (sizeFormat == 0b10) {
                int b1 = in.readByte();
                int b2 = in.readByte();
                int b3 = in.readByte();

                sizeMask = MASK_14_BITS;

                int data = b3 << 24 | b2 << 16 | b1 << 8 | b0;
                regeneratedSize = (data >>> 4) & sizeMask;
                compressedSize = (data >>> 18) & sizeMask;
            } else {    // sizeFormat == 0b11
                long b1 = in.readByte();
                long b2 = in.readByte();
                long b3 = in.readByte();
                long b4 = in.readByte();

                sizeMask = MASK_18_BITS;

                long data = b4 << 32 | b3 << 24 | b2 << 16 | b1 << 8 | b0;
                regeneratedSize = (int) ((data >>> 4) & sizeMask);
                compressedSize = (int) ((data >>> 22) & sizeMask);
            }

            return this;
        }

        private CompressedTreeless(BlockType blockType) {
            super(blockType);
        }

        public boolean isSingleStream() {
            return sizeFormat == 0b00;
        }

        public void withSize(boolean singleStream, int regeneratedSize, int compressedSize) {
            this.regeneratedSize = regeneratedSize;
            this.compressedSize = compressedSize;

            if (singleStream) {
                if (regeneratedSize <= MASK_10_BITS) {
                    sizeFormat = 0b00;
                    sizeMask = MASK_10_BITS;
                    headerSize = 3;
                } else
                    throw new RuntimeException(String.format(
                            "regeneratedSize = %d and compressedSize = %d cannot be compressed as single stream",
                            regeneratedSize, compressedSize));
            } else if (regeneratedSize <= MASK_10_BITS) {
                sizeFormat = 0b01;
                sizeMask = MASK_10_BITS;
                headerSize = 3;
            } else if (regeneratedSize <= MASK_14_BITS) {
                sizeFormat = 0b10;
                sizeMask = MASK_14_BITS;
                headerSize = 4;
            } else if (regeneratedSize <= MASK_18_BITS) {
                sizeFormat = 0b11;
                sizeMask = MASK_18_BITS;
                headerSize = 5;
            } else
                throw new RuntimeException(String.format(
                        "regeneratedSize = %d and compressedSize = %d is too big",
                        regeneratedSize, compressedSize));
        }

        @Override
        public void encode(OutByteArrayWithOffs out) {
            // format in bits [3 bytes]: DDDDDDDD_DDCCCCCC_CCCCBBAA
            // format in bits [4 bytes]: DDDDDDDD_DDDDDDCC_CCCCCCCC_CCCCBBAA
            // format in bits [5 bytes]: DDDDDDDD_DDDDDDDD_DDCCCCCC_CCCCCCCC_CCCCBBAA
            long d;

            if (headerSize == 3)
                // format in bits [3 bytes]: DDDDDDDD_DDCCCCCC_CCCCBBAA
                d = ((long) compressedSize & sizeMask) << 14
                        | ((long) regeneratedSize & sizeMask) << 4
                        | (sizeFormat & MASK_SIZE_FORMAT_2BITS) << 2
                        | (blockType.id & MASK_BLOCK_TYPE);
            else if (headerSize == 4)
                // format in bits [4 bytes]: DDDDDDDD_DDDDDDCC_CCCCCCCC_CCCCBBAA
                d = ((long) compressedSize & sizeMask) << 18
                        | ((long) regeneratedSize & sizeMask) << 4
                        | (sizeFormat & MASK_SIZE_FORMAT_2BITS) << 2
                        | (blockType.id & MASK_BLOCK_TYPE);
            else if (headerSize == 5)
                // format in bits [5 bytes]: DDDDDDDD_DDDDDDDD_DDCCCCCC_CCCCCCCC_CCCCBBAA
                d = ((long) compressedSize & sizeMask) << 22
                        | ((long) regeneratedSize & sizeMask) << 4
                        | (sizeFormat & MASK_SIZE_FORMAT_2BITS) << 2
                        | (blockType.id & MASK_BLOCK_TYPE);
            else
                throw new RuntimeException("headerSize = " + headerSize + " is not supported");

            for (int i = 0; i < headerSize; i++) {
                out.putByte((byte) (d & 0xFF));
                d >>>= 8;
            }
        }

        public static int calcHeaderSize(int regeneratedSize) {
            if (regeneratedSize <= MASK_10_BITS)
                return 3;
            if (regeneratedSize <= MASK_14_BITS)
                return 4;
            if (regeneratedSize <= MASK_18_BITS)
                return 5;
            throw new RuntimeException("regeneratedSize = " + regeneratedSize + " is too big");
        }

    }

}
