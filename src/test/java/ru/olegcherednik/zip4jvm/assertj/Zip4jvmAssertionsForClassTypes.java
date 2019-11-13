package ru.olegcherednik.zip4jvm.assertj;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.zip.ZipEntry;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Zip4jvmAssertionsForClassTypes {

    public static ZipFileAssert assertThat(ZipFileDecorator actual) {
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
