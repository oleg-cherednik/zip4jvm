package ru.olegcherednik.zip4jvm.io.zstd.frame;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.zstd.Buffer;
import ru.olegcherednik.zip4jvm.io.zstd.UnsafeUtil;

import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_BYTE;

/**
 * @author Oleg Cherednik
 * @since 14.11.2021
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum BlockType {
    // this is an uncompressed block
    // Block_Content contains 'size' bytes
    RAW(0) {
        @Override
        public int decode(int blockSize, Buffer inputBase, byte[] outputBase, int output, ZstdFrameDecompressor decompressor) {
            return inputBase.copyMemory(outputBase, output, blockSize);
        }
    },
    // this is a single byte, repeated 'size' times
    // Block_Content consists of a single byte. On the decompression side,
    // this byte must be repeated 'size' times
    RLE(1) {
        @Override
        public int decode(int blockSize, Buffer inputBase, byte[] outputBase, int output, ZstdFrameDecompressor decompressor) {
            byte value = (byte)inputBase.getByte();

            for (int i = 0; i < blockSize; i++) {
                UnsafeUtil.putByte(outputBase, output, value);
                output += SIZE_OF_BYTE;
            }

            return blockSize;
        }
    },
    // this is a Zstandard compressed block. `size` is the length of
    // Block_Content, the compressed data. The decompressed size is not
    // known, but its maximum possible value is guaranteed
    COMPRESSED(2) {
        @Override
        public int decode(int blockSize, Buffer inputBase, byte[] outputBase, int output, ZstdFrameDecompressor decompressor) {
            return decompressor.decodeCompressedBlock(inputBase, blockSize, outputBase, output);
        }
    },
    // this is not a block. This value cannot be used with current version
    // of this specification. If such a value is present, it is considered
    // corrupted data
    RESERVED(3);

    private final int id;

    public int decode(int blockSize, Buffer inputBase, byte[] outputBase, int output, ZstdFrameDecompressor decompressor) {
        throw new Zip4jvmException("Invalid block type");
    }

    public static BlockType parseId(int id) {
        for (BlockType blockType : values())
            if (blockType.id == id)
                return blockType;

        throw new Zip4jvmException("Unknown block type: " + id);
    }
}
