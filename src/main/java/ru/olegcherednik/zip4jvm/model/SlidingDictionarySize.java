package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * see 4.4.4
 *
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum SlidingDictionarySize {
    SD_4K("4K"),
    SD_8K("8K");

    private final String title;
}
