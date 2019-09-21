package ru.olegcherednik.zip4jvm.model;

import lombok.Getter;
import lombok.Setter;

/**
 * see 4.3.9
 *
 * @author Oleg Cherednik
 * @since 25.07.2019
 */
@Getter
@Setter
public class DataDescriptor {

    public static final int SIGNATURE = 0x08074B50;

    // size:4 - crc-32
    private long crc32;
    // size:4 (zip64:8) - compressed size
    private long compressedSize;
    // size:4(zip64:8) - uncompressed size
    private long uncompressedSize;

}
