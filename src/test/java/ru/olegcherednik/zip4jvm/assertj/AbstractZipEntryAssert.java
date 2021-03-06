package ru.olegcherednik.zip4jvm.assertj;

import org.assertj.core.api.AbstractAssert;

import java.util.zip.ZipEntry;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
public abstract class AbstractZipEntryAssert<S extends AbstractZipEntryAssert<S>> extends AbstractAssert<S, ZipEntry> {

    protected final ZipFileDecorator zipFile;

    protected AbstractZipEntryAssert(ZipEntry actual, Class<?> selfType, ZipFileDecorator zipFile) {
        super(actual, selfType);
        this.zipFile = zipFile;
    }

    public S exists() {
        isNotNull();
        assertThat(zipFile.containsEntry(actual.getName())).isTrue();
        return myself;
    }

    public S notExists() {
        isNotNull();
        assertThat(zipFile.containsEntry(actual.getName())).isFalse();
        return myself;
    }

    @Override
    public String toString() {
        return actual.getName();
    }

}
