package com.cop.zip4j.assertj;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
public class ZipFileAssert extends AbstractZipFileAssert<ZipFileAssert> {

    public ZipFileAssert(ZipFileDecorator actual) {
        super(actual, ZipFileAssert.class);
    }
}
