package ru.olegcherednik.zip4jvm.model;

import lombok.Builder;
import lombok.Getter;

/**
 * see 4.3.9
 *
 * @author Oleg Cherednik
 * @since 25.07.2019
 */
@Getter
@Builder
public final class DataDescriptor {

    public static final int SIGNATURE = 0x08074B50;

    // size:4 - crc-32
    private final long crc32;
    // size:4 (zip64:8) - compressed size
    private final long compressedSize;
    // size:4(zip64:8) - uncompressed size
    private final long uncompressedSize;

}
