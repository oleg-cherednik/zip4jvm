package net.lingala.zip4j.assertj;

import java.util.zip.ZipFile;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
public class ZipFileAssert extends AbstractZipFileAssert<ZipFileAssert> {

    public ZipFileAssert(ZipFile actual) {
        super(actual, ZipFileAssert.class);
    }
}
