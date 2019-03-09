package net.lingala.zip4j.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 09.03.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum CompressionLevel {
    DEFAULT(-1),
    FASTEST(1),
    FAST(3),
    NORMAL(5),
    MAXIMUM(7),
    ULTRA(9);

    private final int value;

}
