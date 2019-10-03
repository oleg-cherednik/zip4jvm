package ru.olegcherednik.zip4jvm.assertj;

/**
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
public interface EntryAssert<S extends EntryAssert<S>> {

    S exists();

    S hasDirectories(int expected);

    S hasFiles(int expected);

    EntryAssert<?> directory(String name);

//    EntryAssert<?> file(String name);

}
