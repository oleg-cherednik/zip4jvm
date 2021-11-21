package ru.olegcherednik.zip4jvm.io.zstd.frame;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.zstd.Buffer;

/**
 * see 3.1.1.3.1.1
 *
 * @author Oleg Cherednik
 * @since 12.11.2021
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LiteralsSectionHeader {

    // bit:0-1
    private final LiteralsBlockType literalsBlockType;
    // bit:2-3
    private final LiteralsSizeFormat literalsSizeFormat;
    // bit:4-23
    private final int sizePart1;
    // CompressedSize (18 bits) - optional

    public static LiteralsSectionHeader read(Buffer inputBase) {
        int one = inputBase.getByteNoMove();
        int two = inputBase.get3Bytes();
        LiteralsBlockType literalsBlockType = LiteralsBlockType.parseValue(one & 0b11);
        LiteralsSizeFormat literalsSizeFormat = LiteralsSizeFormat.parseValue((one >> 2) & 0b11);
        int sizePart1 = two >> 4;
        return new LiteralsSectionHeader(literalsBlockType, literalsSizeFormat, sizePart1);
    }

    public void decodeLiteralsBlock(Buffer inputBase, int blockSize, ZstdFrameDecompressor decompressors) {
        literalsBlockType.decode(inputBase, blockSize, this, decompressors);
    }

}
