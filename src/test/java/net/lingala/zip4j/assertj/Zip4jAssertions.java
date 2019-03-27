package net.lingala.zip4j.assertj;

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

    public static AbstractZipFileAssert<?> assertThatZipFile(Path path) throws IOException {
        return assertThat(new ZipFileDecorator(path));
    }

    public static AbstractZipFileAssert<?> assertThat(ZipFileDecorator actual) {
        return Zip4jAssertionsForClassTypes.assertThat(actual);
    }

}
