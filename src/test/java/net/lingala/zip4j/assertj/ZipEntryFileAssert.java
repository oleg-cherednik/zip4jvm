package net.lingala.zip4j.assertj;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
public class ZipEntryFileAssert extends AbstractZipEntryFileAssert<ZipEntryFileAssert> {

    public ZipEntryFileAssert(ZipEntry actual, ZipFile zipFile) {
        super(actual, ZipEntryFileAssert.class, zipFile);
    }
}
