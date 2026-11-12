package io.airlift.compress.zstd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 12.11.2026
 */
@Getter
@RequiredArgsConstructor
public enum LiteralsBlockType {
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
