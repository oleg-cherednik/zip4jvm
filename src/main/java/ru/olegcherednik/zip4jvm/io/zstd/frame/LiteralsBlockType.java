package ru.olegcherednik.zip4jvm.io.zstd.frame;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

/**
 * @author Oleg Cherednik
 * @since 10.11.2021
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum LiteralsBlockType {
    // Literals are stored uncompressed
    RAW(0),
    // Literals consist of a single byte value repeated Regenerated_Size times
    RLE(1),
    // This is a standard Huffman-compressed block, starting with a Huffman
    // tree description
    COMPRESSED(2),
    // This is a Huffman-compressed block, using Huffman tree from previous
    // Huffman-compressed literals block. Huffman_Tree_Description will be
    // skipped. Note: If this mode is triggered without any previous
    // Huffman-table in the frame (or dictionary), this should be treated as
    // data corruption
    TREELESS(3);

    private final int id;

    public static LiteralsBlockType parseId(int id) {
        for (LiteralsBlockType type : values())
            if (type.id == id)
                return type;

        throw new Zip4jvmException("Unknown LiteralsBlock type: " + id);
    }
}
