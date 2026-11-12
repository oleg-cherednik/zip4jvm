package io.airlift.compress.zstd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * see: 3.1.1.3.1.1
 *
 * @author Oleg Cherednik
 * @since 12.11.2026
 */
@Setter
@Getter
public final class LiteralsSectionHeader {

    // 2 bits
    private BlockType blockType;
    // 1-2 bits
    private int sizeFormat;
    // 5-20 bits
    private int regeneratedSize;
    // 0-18 bits
    private int compressedSize;

    public int getHeaderSize() {
        if (regeneratedSize < 31)
            return 1;
        if (regeneratedSize < 4095)
            return 2;
        return 3;
    }

    public void encode(OutByteArrayWithOffs out) {
        int headerSize = getHeaderSize();

        if (headerSize == 1)
            out.putByte((byte) (blockType.id | (regeneratedSize << 3)));
        else if (headerSize == 2)
            out.putShort((short) (blockType.id | (1 << 2) | (regeneratedSize << 4)));
        else
            out.putInt(blockType.id | 3 << 2 | regeneratedSize << 4);
    }

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

    }

}
