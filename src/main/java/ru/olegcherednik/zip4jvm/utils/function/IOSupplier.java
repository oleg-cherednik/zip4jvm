package ru.olegcherednik.zip4jvm.utils.function;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.09.2019
 */
@FunctionalInterface
public interface IOSupplier<T> {

    T get(@NonNull ZipEntry entry) throws IOException;

}
