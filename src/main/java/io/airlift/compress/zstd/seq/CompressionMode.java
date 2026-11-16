package io.airlift.compress.zstd.seq;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 16.11.2026
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum CompressionMode {
    PREDEFINED(0),
    RLE(1),
    FSE_COMPRESSED(2),
    REPEAT(3);

    private final int id;

}
