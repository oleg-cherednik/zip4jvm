package ru.olegcherednik.zip4jvm.utils.function;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 19.09.2019
 */
@FunctionalInterface
public interface InputStreamSupplier {

    InputStream get() throws IOException;

}
