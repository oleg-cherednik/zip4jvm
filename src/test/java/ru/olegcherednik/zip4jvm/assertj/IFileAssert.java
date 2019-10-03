package ru.olegcherednik.zip4jvm.assertj;

/**
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
public interface IFileAssert<S extends IFileAssert<S>> {

    S exists();

    S hasSize(long size);

    S isImage();

}
