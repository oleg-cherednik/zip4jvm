package ru.olegcherednik.zip4jvm.assertj;

import java.util.function.Consumer;

/**
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
public interface IDirectoryAssert<S extends IDirectoryAssert<S>> {

    S exists();

    S hasDirectories(int expected);

    S hasFiles(int expected);

    default S isEmpty() {
        hasDirectories(0);
        return hasFiles(0);
    }

    S directory(String name);

    IFileAssert<?> file(String name);

    S matches(Consumer<IDirectoryAssert<?>> consumer);

}
