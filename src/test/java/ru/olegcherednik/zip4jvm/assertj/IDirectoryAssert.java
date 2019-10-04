package ru.olegcherednik.zip4jvm.assertj;

/**
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
public interface IDirectoryAssert<S extends IDirectoryAssert<S>> {

    S exists();

    S hasDirectories(int expected);

    S hasFiles(int expected);

    IDirectoryAssert<?> directory(String name);

    IFileAssert<?> file(String name);
}
