package net.lingala.zip4j.assertj;

import org.apache.commons.io.FilenameUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
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

    private int getFoldersAmount() {
        Map<String, Set<String>> map = walk();
        int count = 0;

        for (String entryName : map.getOrDefault(actual.getName(), Collections.emptySet()))
            if (isDirectory(entryName))
                count++;

        return count;
    }

    private long getRegularFilesAmount() {
        Map<String, Set<String>> map = walk();
        long count = 0;

        for (String entryName : map.getOrDefault(actual.getName(), Collections.emptySet()))
            if (!isDirectory(entryName))
                count++;

        return count;
    }

    private static boolean isDirectory(String entryName) {
        return FilenameUtils.getExtension(entryName).isEmpty();
    }
}
