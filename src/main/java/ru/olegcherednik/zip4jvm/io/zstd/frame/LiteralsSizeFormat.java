package ru.olegcherednik.zip4jvm.io.zstd.frame;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.zstd.Buffer;

/**
 * @author Oleg Cherednik
 * @since 14.11.2021
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum LiteralsSizeFormat {
    ONE_STREAM_10BITS(0b00),
    FOUR_STREAMS_10BITS(0b01),
    FOUR_STREAMS_14BITS(0b10),
    FOUR_STREAMS_18BITS(0b11);

    private final int value;

    public static LiteralsSizeFormat parseValue(int value) {
        for (LiteralsSizeFormat literalsSizeFormat : values())
            if (literalsSizeFormat.value == value)
                return literalsSizeFormat;

        throw new Zip4jvmException("Unknown LiteralsBlock type: " + value);
    }

    public void decodeStream(Buffer inputBase, int inputLimit, ZstdFrameDecompressor decompressor) {
        if (this == ONE_STREAM_10BITS)
            decompressor.decodeSingleStream(inputBase, inputLimit);
        else
            decompressor.decode4Streams(inputBase, inputLimit);
    }


}
