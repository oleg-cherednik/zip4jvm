package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 07.10.2019
 */
@SuppressWarnings("CharsetObjectCanBeUsed")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Charsets {

    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Charset IBM437 = Charset.forName("IBM437");

}
