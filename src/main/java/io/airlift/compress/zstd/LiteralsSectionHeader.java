package io.airlift.compress.zstd;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

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
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class LiteralsSectionHeader {

    // 2 bits
    protected final BlockType blockType;
    // 1-2 bits
    protected int sizeFormat;
    // 5-20 bits
    protected int regeneratedSize;
    protected int regeneratedSizeMask;

    protected int headerSize;

    public static LiteralsSectionHeader.RawRle createForRaw() {
        return new RawRle(BlockType.RAW_LITERALS_BLOCK);
    }

    public static LiteralsSectionHeader.RawRle createForRle() {
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
        RAW_LITERALS_BLOCK(0, new Supplier<LiteralsSectionHeader>() {
            @Override
            public LiteralsSectionHeader get() {
                return new RawRle(RAW_LITERALS_BLOCK);
            }
        }),
        /**
         * Literals consist of a single-byte value repeated
         * Regenerated_Size times. Literals_Section_Content is 1.
         */
        RLE_LITERALS_BLOCK(1, new Supplier<LiteralsSectionHeader>() {
            @Override
            public LiteralsSectionHeader get() {
                return new RawRle(RLE_LITERALS_BLOCK);
            }
        }),
        /**
         * This is a standard Huffman-compressed block, starting
         * with a Huffman tree description. See details below.
         * Literals_Section_Content is Compressed_Size.
         */
        COMPRESSED_LITERALS_BLOCK(2, new Supplier<LiteralsSectionHeader>() {
            @Override
            public LiteralsSectionHeader get() {
                return new RawRle(COMPRESSED_LITERALS_BLOCK);
            }
        }),
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
        TREELESS_LITERALS_BLOCK(3, new Supplier<LiteralsSectionHeader>() {
            @Override
            public LiteralsSectionHeader get() {
                return new RawRle(TREELESS_LITERALS_BLOCK);
            }
        });

        private final int id;
        private final Supplier<LiteralsSectionHeader> createSectionHeader;

        public LiteralsSectionHeader createHeader() {
            return createSectionHeader.get();
        }
    }

//    public int getHeaderSize() {
//        if (regeneratedSize <= 31)
//            return 1;
//        if (regeneratedSize <= 4095)
//            return 2;
//        return 3;
//    }

    public static final class RawRle extends LiteralsSectionHeader {

        private static final int MASK_5_BITS = 0b1_1111;
        private static final int MASK_12_BITS = 0b1111_1111_1111;
        private static final int MASK_20_BITS = 0b1111_1111_1111_1111_1111;

        private RawRle(BlockType blockType) {
            super(blockType);
        }

        public void withRegeneratedSize(int regeneratedSize) {
            this.regeneratedSize = regeneratedSize;

            if (regeneratedSize <= MASK_5_BITS) {
                sizeFormat = 0b0;   // 1 bit
                regeneratedSizeMask = MASK_5_BITS;
                headerSize = 1;
            } else if (regeneratedSize <= MASK_12_BITS) {
                sizeFormat = 0b01;  // 2 bits
                regeneratedSizeMask = MASK_12_BITS;
                headerSize = 2;
            } else if (regeneratedSize <= MASK_20_BITS) {
                sizeFormat = 0b11;  // 2 bits
                regeneratedSizeMask = MASK_20_BITS;
                headerSize = 3;
            } else
                throw new RuntimeException("regeneratedSize = " + regeneratedSize + " is too big");
        }

        @Override
        public void encode(OutByteArrayWithOffs out) {
            int d;

            if (headerSize == 1)
                // format in bits [1 byte]: CCCCCBAA
                d = (regeneratedSize & regeneratedSizeMask) << 3
                        | (sizeFormat & 0b1) << 2
                        | (blockType.id & 0b11);
            else
                // format in bits [2 bytes]: CCCCCCCC_CCCCBBAA
                // format in bits [3 bytes]: CCCCCCCC_CCCCCCCC_CCCCBBAA
                d = (regeneratedSize & regeneratedSizeMask) << 4
                        | (sizeFormat & 0b11) << 2
                        | (blockType.id & 0b11);

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
        private int compressedSize;
        private int compressedSizeMask;

        private CompressedTreeless(BlockType blockType) {
            super(blockType);
        }

        public void withSize(boolean singleStream, int regeneratedSize, int compressedSize) {
            this.regeneratedSize = regeneratedSize;
            this.compressedSize = compressedSize;

            if (singleStream) {
                if (regeneratedSize <= MASK_10_BITS) {
                    sizeFormat = 0b00;
                    regeneratedSizeMask = MASK_10_BITS;
                    compressedSizeMask = MASK_10_BITS;
                    headerSize = 3;
                } else
                    throw new RuntimeException(String.format(
                            "regeneratedSize = %d and compressedSize = %d cannot be compressed as single stream",
                            regeneratedSize, compressedSize));
            } else if (regeneratedSize <= MASK_10_BITS) {
                sizeFormat = 0b01;
                regeneratedSizeMask = MASK_10_BITS;
                compressedSizeMask = MASK_10_BITS;
                headerSize = 3;
            } else if (regeneratedSize <= MASK_14_BITS) {
                sizeFormat = 0b10;
                regeneratedSizeMask = MASK_14_BITS;
                compressedSizeMask = MASK_14_BITS;
                headerSize = 4;
            } else if (regeneratedSize <= MASK_18_BITS) {
                sizeFormat = 0b11;
                regeneratedSizeMask = MASK_18_BITS;
                compressedSizeMask = MASK_18_BITS;
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
                d = ((long) compressedSize & compressedSizeMask) << 14
                        | ((long) regeneratedSize & regeneratedSizeMask) << 4
                        | (sizeFormat & 0b11) << 2
                        | (blockType.id & 0b11);
            else if (headerSize == 4)
                // format in bits [4 bytes]: DDDDDDDD_DDDDDDCC_CCCCCCCC_CCCCBBAA
                d = ((long) compressedSize & compressedSizeMask) << 18
                        | ((long) regeneratedSize & regeneratedSizeMask) << 4
                        | (sizeFormat & 0b11) << 2
                        | (blockType.id & 0b11);
            else if (headerSize == 5)
                // format in bits [5 bytes]: DDDDDDDD_DDDDDDDD_DDCCCCCC_CCCCCCCC_CCCCBBAA
                d = ((long) compressedSize & compressedSizeMask) << 22
                        | ((long) regeneratedSize & regeneratedSizeMask) << 4
                        | (sizeFormat & 0b11) << 2
                        | (blockType.id & 0b11);
            else
                throw new RuntimeException("headerSize = " + headerSize + " is not supported");

            for (int i = 0; i < headerSize; i++) {
                out.putByte((byte) (d & 0xFF));
                d >>>= 8;
            }
        }

    }

}
