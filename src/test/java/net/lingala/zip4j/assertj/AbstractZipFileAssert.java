package net.lingala.zip4j.assertj;

import org.assertj.core.api.AbstractAssert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
@SuppressWarnings("NewClassNamingConvention")
public class AbstractZipFileAssert<SELF extends AbstractZipFileAssert<SELF>> extends AbstractAssert<SELF, ZipFile> {

    public AbstractZipFileAssert(ZipFile actual, Class<?> selfType) {
        super(actual, selfType);
    }

    public AbstractZipEntryDirectoryAssert<?> rootEntry() {
        return directory("/");
    }

    public AbstractZipEntryDirectoryAssert<?> directory(String name) {
        ZipEntry entry = new ZipEntry(name);
        Zip4jAssertions.assertThat(entry.isDirectory()).isTrue();
        return new ZipEntryDirectoryAssert(entry, actual);
    }

    public AbstractZipEntryFileAssert<?> file(String name) {
        ZipEntry entry = new ZipEntry(name);
        assertThat(entry.isDirectory()).isFalse();
        return new ZipEntryFileAssert(entry, actual);
    }

    public SELF exists() {
        isNotNull();

        Path path = Paths.get(actual.getName());
        assertThat(Files.exists(path)).isTrue();
        assertThat(Files.isRegularFile(path)).isTrue();

        return myself;
    }

}
