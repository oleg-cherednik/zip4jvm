package ru.olegcherednik.zip4jvm.assertj;

import org.assertj.core.api.AbstractAssert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        ZipEntry entry = actual.getEntry(name);
        assertThat(entry.isDirectory()).isFalse();
        return new ZipEntryFileAssert(entry, actual);
    }

    public AbstractZipEntryFileAssert<?> file(String name, char[] password) {
        ZipEntry entry = actual.getEntry(name);
        assertThat(entry.isDirectory()).isFalse();
        return new ZipEntryFileAssert(entry, actual);
    }

    public static AbstractZipFileAssert<?> assertThatZipFile(Path zipFile, char[] password) throws IOException {
        return Zip4jvmAssertionsForClassTypes.assertThat(new ZipFileEncryptedDecoder(zipFile, password));
    }

    public SELF exists() {
        isNotNull();
        assertThat(Files.exists(actual.getZip())).isTrue();
        assertThat(Files.isRegularFile(actual.getZip())).isTrue();
        return myself;
    }

    public SELF hasCommentSize(int size) {
        if (size == 0)
            assertThat(actual.getComment()).isNull();
        else
            assertThat(actual.getComment()).hasSize(size);

        return myself;
    }

    public SELF hasComment(String comment) {
        assertThat(actual.getComment()).isEqualTo(comment);
        return myself;
    }

}
