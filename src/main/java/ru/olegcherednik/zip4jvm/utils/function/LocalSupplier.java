package ru.olegcherednik.zip4jvm.utils.function;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 19.10.2019
 */
// TODO should be renamed
@FunctionalInterface
public interface LocalSupplier<T> {

    T get() throws IOException;

}
