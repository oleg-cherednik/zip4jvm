package ru.olegcherednik.zip4jvm.utils.function;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 19.09.2019
 */
@FunctionalInterface
public interface IOSupplier2<T> {

    T get() throws IOException;

}
