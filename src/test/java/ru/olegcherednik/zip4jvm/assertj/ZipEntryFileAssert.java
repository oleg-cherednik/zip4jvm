package ru.olegcherednik.zip4jvm.assertj;

import java.util.zip.ZipEntry;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
public class ZipEntryFileAssert extends AbstractZipEntryFileAssert<ZipEntryFileAssert> {

    public ZipEntryFileAssert(ZipEntry actual, ZipFileDecorator zipFile) {
        this(actual, zipFile, null);
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public ZipEntryFileAssert(ZipEntry actual, ZipFileDecorator zipFile, char[] password) {
        super(actual, ZipEntryFileAssert.class, zipFile, password);
    }
}
