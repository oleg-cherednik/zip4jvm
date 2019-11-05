package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.view.Title;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum ShannonFanoTreesNumber implements Title {
    TWO("2"),
    THREE("3");

    private final String title;
}
