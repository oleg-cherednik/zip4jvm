package ru.olegcherednik.zip4jvm.assertj;

import org.assertj.core.api.AbstractAssert;

import java.nio.file.Files;
import java.util.zip.ZipEntry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
@SuppressWarnings("NewClassNamingConvention")
public class AbstractZipFileAssert<SELF extends AbstractZipFileAssert<SELF>> extends AbstractAssert<SELF, ZipFileDecorator> {

    public AbstractZipFileAssert(ZipFileDecorator actual, Class<?> selfType) {
        super(actual, selfType);
    }

    public AbstractZipEntryDirectoryAssert<?> rootEntry() {
        return directory("/");
    }

    public AbstractZipEntryDirectoryAssert<?> directory(String name) {
        ZipEntry entry = new ZipEntry(name);
        assertThat(entry.isDirectory()).isTrue();
        return new ZipEntryDirectoryAssert(entry, actual);
    }

    public AbstractZipEntryFileAssert<?> file(String name) {
        ZipEntry entry = new ZipEntry(name);
        assertThat(entry.isDirectory()).isFalse();
        return new ZipEntryFileAssert(entry, actual);
    }

    public SELF exists() {
        isNotNull();
        assertThat(Files.exists(actual.getZipFile())).isTrue();
        assertThat(Files.isRegularFile(actual.getZipFile())).isTrue();
        return myself;
    }

    public SELF hasCommentSize(int size) {
        assertThat(actual.getComment()).hasSize(size);
        return myself;
    }

}
