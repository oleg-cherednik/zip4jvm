package ru.olegcherednik.zip4jvm.assertj;

import org.apache.commons.io.FilenameUtils;

import java.util.function.Consumer;
import java.util.zip.ZipEntry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
public class AbstractZipEntryDirectoryAssert<S extends AbstractZipEntryDirectoryAssert<S>> extends AbstractZipEntryAssert<S> {

    public AbstractZipEntryDirectoryAssert(ZipEntry actual, Class<?> selfType, ZipFileDecorator zipFile) {
        super(actual, selfType, zipFile);
    }

    public S hasSubDirectories(int expected) {
        assertThat(getFoldersAmount()).isEqualTo(expected);
        return myself;
    }

    public S hasFiles(int expected) {
        assertThat(getRegularFilesAmount()).isEqualTo(expected);
        return myself;
    }

    public AbstractZipEntryFileAssert<?> file(String name) {
        return new ZipEntryFileAssert(getZipEntry(name), zipFile);
    }

    public AbstractZipEntryDirectoryAssert<?> directory(String name) {
        return new ZipEntryDirectoryAssert(new ZipEntry(name), zipFile);
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    private ZipEntry getZipEntry(String name) {
        name = "/".equals(actual.getName()) ? name : actual.getName() + name;
        return new ZipEntry(name);
    }

    public S matches(Consumer<AbstractZipEntryDirectoryAssert<?>> consumer) {
        consumer.accept(this);
        return myself;
    }

    private int getFoldersAmount() {
        return (int)zipFile.getSubEntries(actual.getName()).stream()
                           .filter(AbstractZipEntryDirectoryAssert::isDirectory)
                           .count();
    }

    private long getRegularFilesAmount() {
        return (int)zipFile.getSubEntries(actual.getName()).stream()
                           .filter(entryName -> !isDirectory(entryName))
                           .count();
    }

    private static boolean isDirectory(String entryName) {
        return FilenameUtils.getExtension(entryName).isEmpty();
    }
}
