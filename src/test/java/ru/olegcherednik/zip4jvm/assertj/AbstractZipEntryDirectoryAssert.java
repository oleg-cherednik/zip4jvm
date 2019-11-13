package ru.olegcherednik.zip4jvm.assertj;

import org.apache.commons.io.FilenameUtils;
import org.assertj.core.internal.Failures;

import java.util.function.Consumer;
import java.util.zip.ZipEntry;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
public class AbstractZipEntryDirectoryAssert<S extends AbstractZipEntryDirectoryAssert<S>> extends AbstractZipEntryAssert<S> implements IDirectoryAssert<S> {

    public AbstractZipEntryDirectoryAssert(ZipEntry actual, Class<?> selfType, ZipFileDecorator zipFile) {
        super(actual, selfType, zipFile);
    }

    @Override
    public S hasDirectories(int expected) {
        long actual = getFoldersAmount();

        if (actual != expected)
            throw Failures.instance().failure(
                    String.format("Zip directory '%s' contains illegal amount of directories: actual - '%d', expected - '%d'",
                            this.actual, actual, expected));

        return myself;
    }

    @Override
    public S hasFiles(int expected) {
        long actual = getRegularFilesAmount();

        if (actual != expected)
            throw Failures.instance().failure(String.format("Zip directory '%s' contains illegal amount of files: actual - '%d', expected - '%d'",
                    this.actual, actual, expected));

        return myself;
    }

    public ZipEntryFileAssert file(String name) {
        return new ZipEntryFileAssert(getZipEntry(name), zipFile);
    }

    @Override
    public AbstractZipEntryDirectoryAssert<?> directory(String name) {
        return new ZipEntryDirectoryAssert(new ZipEntry(name), zipFile);
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    private ZipEntry getZipEntry(String name) {
        name = "/".equals(actual.getName()) ? name : actual.getName() + name;
        return new ZipEntry(name);
    }

    public S matches(Consumer<IDirectoryAssert<?>> consumer) {
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
