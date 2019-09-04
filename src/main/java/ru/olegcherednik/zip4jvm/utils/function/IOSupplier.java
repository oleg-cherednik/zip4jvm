package ru.olegcherednik.zip4jvm.utils.function;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.09.2019
 */
public interface IOSupplier<T> {

    T get() throws IOException;

}
