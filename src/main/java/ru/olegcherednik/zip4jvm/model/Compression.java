package ru.olegcherednik.zip4jvm.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
@Getter
@RequiredArgsConstructor
public enum Compression {
    STORE(CompressionMethod.STORE),
    DEFLATE(CompressionMethod.DEFLATE),
    ENHANCED_DEFLATE(CompressionMethod.ENHANCED_DEFLATE),
    BZIP2(CompressionMethod.BZIP2),
    LZMA(CompressionMethod.LZMA);

    private final CompressionMethod method;

    public static Compression parseCompressionMethod(CompressionMethod compressionMethod) {
        for (Compression compression : values())
            if (compression.method == compressionMethod)
                return compression;
        throw new EnumConstantNotPresentException(Compression.class, "compressionMethod=" + compressionMethod);
    }

}
