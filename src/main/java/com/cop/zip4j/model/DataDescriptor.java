package com.cop.zip4j.model;

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

    // TODO 4.3.9.1 - size of compressed and uncompressed is 8 bytes

    // size:4 - crc32-32
    private long crc32;
    // size:4 - compressed size
    private long compressedSize;
    // size:4 - uncompressed size
    private long uncompressedSize;

}
