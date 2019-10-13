package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 07.10.2019
 */
@SuppressWarnings("CharsetObjectCanBeUsed")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Charsets {

    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Charset IBM437 = Charset.forName("IBM437");
    public static final Charset SYSTEM = Charset.forName(System.getProperty("sun.jnu.encoding", "UTF-8"));

    public static final Function<Charset, Charset> STANDARD_ZIP_CHARSET = charset -> charset;
    public static final Function<Charset, Charset> SYSTEM_CHARSET = charset -> SYSTEM;

}
