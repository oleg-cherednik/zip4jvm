package ru.olegcherednik.zip4jvm.assertj;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.assertj.core.api.Assertions;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 24.03.2019
 */
@SuppressWarnings("ExtendsUtilityClass")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Zip4jvmAssertions extends Assertions {

    public static AbstractZipFileAssert<?> assertThatZipFile(Path zipFile) throws IOException {
        return Zip4jvmAssertionsForClassTypes.assertThat(new ZipFileDecorator(zipFile));
    }

    public static AbstractZipFileAssert<?> assertThatZipFile(Path zipFile, char[] password) throws IOException {
        return Zip4jvmAssertionsForClassTypes.assertThat(new ZipFileEncryptedDecoder(zipFile, password));
    }

    public static AbstractDirectoryAssert<?> assertThatDirectory(Path path) {
        return Zip4jvmAssertionsForClassTypes.assertThatDirectory(path);
    }

    public static AbstractFileExtAssert<?> assertThatFile(Path path) {
        return Zip4jvmAssertionsForClassTypes.assertThatFile(path);
    }

}
