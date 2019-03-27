package net.lingala.zip4j.assertj;

import org.apache.commons.io.FilenameUtils;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
@SuppressWarnings("NewClassNamingConvention")
public class AbstractZipEntryDirectoryAssert<SELF extends AbstractZipEntryDirectoryAssert<SELF>> extends AbstractZipEntryAssert<SELF> {

    public AbstractZipEntryDirectoryAssert(ZipEntry actual, Class<?> selfType, ZipFile zipFile) {
        super(actual, selfType, zipFile);
    }

    public SELF hasSubDirectories(int expected) {
        assertThat(getFoldersAmount()).isEqualTo(expected);
        return myself;
    }

    public SELF hasFiles(int expected) {
        assertThat(getRegularFilesAmount()).isEqualTo(expected);
        return myself;
    }

    public AbstractZipEntryFileAssert<?> file(String name) {
        return new ZipEntryFileAssert(new ZipEntry(actual.getName() + name), zipFile);
    }

    @Override
    public SELF exists() {
        isNotNull();
        assertThat(map).containsKey(actual.getName());
        return myself;
    }

    public SELF matches(Consumer<AbstractZipEntryDirectoryAssert<?>> consumer) {
        consumer.accept(this);
        return myself;
    }

    private int getFoldersAmount() {
        return (int)map.getOrDefault(actual.getName(), Collections.emptySet()).stream()
                       .filter(AbstractZipEntryDirectoryAssert::isDirectory)
                       .count();
    }

    private long getRegularFilesAmount() {
        return (int)map.getOrDefault(actual.getName(), Collections.emptySet()).stream()
                       .filter(entryName -> !isDirectory(entryName))
                       .count();
    }

    private static boolean isDirectory(String entryName) {
        return FilenameUtils.getExtension(entryName).isEmpty();
    }
}
