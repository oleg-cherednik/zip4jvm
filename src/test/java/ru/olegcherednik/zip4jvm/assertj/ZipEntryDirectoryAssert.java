package ru.olegcherednik.zip4jvm.assertj;

import org.apache.commons.io.FilenameUtils;
import org.assertj.core.internal.Failures;

import java.util.function.Consumer;
import java.util.zip.ZipEntry;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
public class ZipEntryDirectoryAssert extends AbstractZipEntryAssert<ZipEntryDirectoryAssert> implements IDirectoryAssert<ZipEntryDirectoryAssert> {

    public ZipEntryDirectoryAssert(ZipEntry actual, ZipFileDecorator zipFile) {
        super(actual, ZipEntryDirectoryAssert.class, zipFile);
    }

    @Override
    public ZipEntryDirectoryAssert hasDirectories(int expected) {
        long actual = getFoldersAmount();

        if (actual != expected)
            throw Failures.instance().failure(
                    String.format("Zip directory '%s' contains illegal amount of directories: actual - '%d', expected - '%d'",
                            this.actual, actual, expected));

        return myself;
    }

    @Override
    public ZipEntryDirectoryAssert hasFiles(int expected) {
        long actual = getRegularFilesAmount();

        if (actual != expected)
            throw Failures.instance().failure(String.format("Zip directory '%s' contains illegal amount of files: actual - '%d', expected - '%d'",
                    this.actual, actual, expected));

        return myself;
    }

    @Override
    public ZipEntryFileAssert file(String name) {
        return new ZipEntryFileAssert(getZipEntry(name), zipFile);
    }

    @Override
    public ZipEntryDirectoryAssert directory(String name) {
        return new ZipEntryDirectoryAssert(new ZipEntry(name), zipFile);
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    private ZipEntry getZipEntry(String name) {
        name = "/".equals(actual.getName()) ? name : actual.getName() + name;
        return new ZipEntry(name);
    }

    @Override
    public ZipEntryDirectoryAssert matches(Consumer<IDirectoryAssert<?>> consumer) {
        consumer.accept(this);
        return myself;
    }

    private int getFoldersAmount() {
        return (int)zipFile.getSubEntries(actual.getName()).stream()
                           .filter(ZipEntryDirectoryAssert::isDirectory)
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
