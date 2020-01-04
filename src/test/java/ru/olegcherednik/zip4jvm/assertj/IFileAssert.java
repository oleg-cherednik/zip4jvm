package ru.olegcherednik.zip4jvm.assertj;

import java.util.function.Consumer;

/**
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
public interface IFileAssert<S extends IFileAssert<S>> {

    S exists();

    S hasSize(long size);

    S isImage();

    S matches(Consumer<IFileAssert<?>> consumer);

}
