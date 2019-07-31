package com.cop.zip4j.assertj;

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
public final class Zip4jAssertions extends Assertions {

    public static AbstractZipFileAssert<?> assertThatZipFile(Path zipFile) throws IOException {
        return Zip4jAssertionsForClassTypes.assertThat(new ZipFileDecorator(zipFile));
    }

    public static AbstractZipFileAssert<?> assertThatZipFile(Path zipFile, char[] password) throws IOException {
        return Zip4jAssertionsForClassTypes.assertThat(new ZipFileEncryptedDecoder(zipFile, password));
    }

    public static AbstractDirectoryAssert<?> assertThatDirectory(Path path) {
        return Zip4jAssertionsForClassTypes.assertThatDirectory(path);
    }

    public static AbstractFileExtAssert<?> assertThatFile(Path path) {
        return Zip4jAssertionsForClassTypes.assertThatFile(path);
    }

}
