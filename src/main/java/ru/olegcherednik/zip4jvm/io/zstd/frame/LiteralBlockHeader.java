package ru.olegcherednik.zip4jvm.io.zstd.frame;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.zstd.Buffer;

/**
 * @author Oleg Cherednik
 * @since 12.11.2021
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LiteralBlockHeader {

    public static final int SIZE_FORMAT_1STREAM_10BITS = 0b00;
    public static final int SIZE_FORMAT_4STREAMS_10BITS = 0b01;
    public static final int SIZE_FORMAT_4STREAMS_14BITS = 0b10;
    public static final int SIZE_FORMAT_4STREAMS_18BITS = 0b11;

    // bit:0-1
    private final Type type;
    // bit:2-3
    private final int sizeFormat;
    // bit:4-23
    private final int sizePart1;
    // CompressedSize (18 bits) - optional

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public enum Type {
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

        public static Type parseId(int id) {
            for (Type type : values())
                if (type.id == id)
                    return type;

            throw new Zip4jvmException("Unknown LiteralsBlock type: " + id);
        }
    }

    public static LiteralBlockHeader read(Buffer inputBase) {
        int one = inputBase.getByteNoMove();
        int two = inputBase.get3Bytes();
        Type type = Type.parseId(one & 0b11);
        int sizeFormat = (one >> 2) & 0b11;
        int sizePart1 = two >> 4;
        return new LiteralBlockHeader(type, sizeFormat, sizePart1);
    }

}
