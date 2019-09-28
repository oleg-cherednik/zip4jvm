package ru.olegcherednik.zip4jvm.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.function.InputStreamSupplier;
import ru.olegcherednik.zip4jvm.utils.function.ZipEntryInputStreamSupplier;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 19.09.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmptyInputStreamSupplier implements InputStreamSupplier, ZipEntryInputStreamSupplier {

    public static final EmptyInputStreamSupplier INSTANCE = new EmptyInputStreamSupplier();

    @Override
    public InputStream get() throws IOException {
        return EmptyInputStream.INSTANCE;
    }

    @Override
    public InputStream get(ZipEntry zipEntry) throws IOException {
        return EmptyInputStream.INSTANCE;
    }
}
