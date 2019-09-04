package ru.olegcherednik.zip4jvm.assertj;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
public class ZipFileAssert extends AbstractZipFileAssert<ZipFileAssert> {

    public ZipFileAssert(ZipFileDecorator actual) {
        super(actual, ZipFileAssert.class);
    }
}
