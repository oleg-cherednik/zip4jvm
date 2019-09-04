package ru.olegcherednik.zip4jvm.assertj;

import lombok.experimental.UtilityClass;

import java.nio.file.Path;
import java.util.zip.ZipEntry;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
@UtilityClass
public class Zip4jAssertionsForClassTypes {

    public static AbstractZipFileAssert<?> assertThat(ZipFileDecorator actual) {
        return new ZipFileAssert(actual);
    }

    public static AbstractZipEntryAssert<?> assertThat(ZipEntry actual, ZipFileDecorator zipFile) {
        return actual.isDirectory() ? new ZipEntryFileAssert(actual, zipFile) : new ZipEntryDirectoryAssert(actual, zipFile);
    }

    public static AbstractDirectoryAssert<?> assertThatDirectory(Path path) {
        return new DirectoryAssert(path);
    }

    public static AbstractFileExtAssert<?> assertThatFile(Path path) {
        return new FileAssert(path);
    }
}
