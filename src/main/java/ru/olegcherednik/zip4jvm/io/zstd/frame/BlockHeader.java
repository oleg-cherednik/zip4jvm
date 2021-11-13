package ru.olegcherednik.zip4jvm.io.zstd.frame;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.zstd.Buffer;
import ru.olegcherednik.zip4jvm.io.zstd.UnsafeUtil;

import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_BYTE;

/**
 * @author Oleg Cherednik
 * @since 08.11.2021
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockHeader {

    // size:3
    // bit:0
    private final boolean last;
    // bit:1-2
    private final Type type;
    // bit:3-23
    // when type == COMPRESSED or RAW - size of Block_Content
    // when type == RLE - the number of times the single byte must be repeated.
    // limited: min of Window_Size and 128Kb
    private final int size;

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public enum Type {
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

        public static Type parseId(int id) {
            for (Type type : values())
                if (type.id == id)
                    return type;

            throw new Zip4jvmException("Unknown block type: " + id);
        }
    }

    public static BlockHeader read(Buffer inputBase) {
        int data = inputBase.get3Bytes();
        boolean last = (data & 1) != 0;
        Type type = Type.parseId((data >>> 1) & 0b11);
        int size = (data >>> 3) & 0x1F_FFFF; // 21 bits

        return new BlockHeader(last, type, size);
    }

}
