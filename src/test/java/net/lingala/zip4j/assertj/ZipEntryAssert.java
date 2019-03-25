package net.lingala.zip4j.assertj;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
public class ZipEntryAssert extends AbstractZipEntryAssert<ZipEntryAssert> {

    public ZipEntryAssert(ZipEntry actual, ZipFile zipFile) {
        super(actual, ZipEntryAssert.class, zipFile);
    }
}
