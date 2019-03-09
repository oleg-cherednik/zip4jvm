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
public enum Encryption {
    OFF(-1),
    STANDARD(0),
    STRONG(1),
    AES(99);

    private final int value;
}
