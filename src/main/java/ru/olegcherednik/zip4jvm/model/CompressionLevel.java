package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.view.Title;

/**
 * see 4.4.4
 *
 * @author Oleg Cherednik
 * @since 09.03.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum CompressionLevel implements Title {
    SUPER_FAST(1, "superfast"),
    FAST(3, "fast"),
    NORMAL(5, "normal"),
    MAXIMUM(7, "maximum");

    private final int code;
    private final String title;

}
