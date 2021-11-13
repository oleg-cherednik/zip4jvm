package ru.olegcherednik.zip4jvm.io.zstd.frame;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.zstd.Buffer;

/**
 * see 3.1.1.3.1.1
 *
 * @author Oleg Cherednik
 * @since 14.11.2021
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum LiteralsBlockType {
    // Literals are stored uncompressed
    RAW(0) {
        @Override
        public void decode(Buffer inputBase, int blockSize, LiteralsSectionHeader literalsSectionHeader, ZstdFrameDecompressor decompressor) {
            decompressor.decodeRawLiterals(inputBase, blockSize, literalsSectionHeader);
        }
    },
    // Literals consist of a single byte value repeated Regenerated_Size times
    RLE(1) {
        @Override
        public void decode(Buffer inputBase, int blockSize, LiteralsSectionHeader literalsSectionHeader, ZstdFrameDecompressor decompressor) {
            decompressor.decodeRleLiterals(inputBase, literalsSectionHeader);
        }
    },
    // This is a standard Huffman-compressed block, starting with a Huffman
    // tree description
    COMPRESSED(2) {
        @Override
        public void decode(Buffer inputBase, int blockSize, LiteralsSectionHeader literalsSectionHeader, ZstdFrameDecompressor decompressor) {
            decompressor.decodeCompressedLiterals(inputBase, blockSize, literalsSectionHeader);
        }
    },
    // This is a Huffman-compressed block, using Huffman tree from previous
    // Huffman-compressed literals block. Huffman_Tree_Description will be
    // skipped. Note: If this mode is triggered without any previous
    // Huffman-table in the frame (or dictionary), this should be treated as
    // data corruption
    TREELESS(3) {
        @Override
        public void decode(Buffer inputBase, int blockSize, LiteralsSectionHeader literalsSectionHeader, ZstdFrameDecompressor decompressor) {
            if (!decompressor.huffman.isLoaded())
                throw new Zip4jvmException("Dictionary is corrupted");
            decompressor.decodeCompressedLiterals(inputBase, blockSize, literalsSectionHeader);
        }
    };

    private final int id;

    public void decode(Buffer inputBase, int blockSize, LiteralsSectionHeader literalsSectionHeader, ZstdFrameDecompressor decompressors) {
        throw new Zip4jvmException("Invalid Literals_Block type");
    }

    public static LiteralsBlockType parseId(int id) {
        for (LiteralsBlockType literalsBlockType : values())
            if (literalsBlockType.id == id)
                return literalsBlockType;

        throw new Zip4jvmException("Unknown LiteralsBlock type: " + id);
    }
}
