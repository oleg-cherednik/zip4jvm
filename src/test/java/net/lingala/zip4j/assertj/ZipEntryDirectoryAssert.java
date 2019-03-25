package net.lingala.zip4j.assertj;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
public class ZipEntryDirectoryAssert extends AbstractZipEntryDirectoryAssert<ZipEntryDirectoryAssert> {

    public ZipEntryDirectoryAssert(ZipEntry actual, ZipFile zipFile) {
        super(actual, ZipEntryDirectoryAssert.class, zipFile);
    }
}
