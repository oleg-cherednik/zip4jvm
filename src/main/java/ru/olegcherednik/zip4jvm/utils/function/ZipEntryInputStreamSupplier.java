package ru.olegcherednik.zip4jvm.utils.function;

import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 04.09.2019
 */
@FunctionalInterface
public interface ZipEntryInputStreamSupplier {

    InputStream get(ZipEntry zipEntry) throws IOException;

}
