package net.lingala.zip4j.assertj;

import org.assertj.core.api.AbstractAssert;

import java.util.zip.ZipEntry;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
@SuppressWarnings("NewClassNamingConvention")
public abstract class AbstractZipEntryAssert<SELF extends AbstractZipEntryAssert<SELF>> extends AbstractAssert<SELF, ZipEntry> {

    protected final ZipFileDecorator zipFile;

    protected AbstractZipEntryAssert(ZipEntry actual, Class<?> selfType, ZipFileDecorator zipFile) {
        super(actual, selfType);
        this.zipFile = zipFile;
    }

    public SELF exists() {
        isNotNull();
        assertThat(zipFile.containsEntry(actual.getName())).isTrue();
        return myself;
    }

    @Override
    public String toString() {
        return actual.getName();
    }

}
