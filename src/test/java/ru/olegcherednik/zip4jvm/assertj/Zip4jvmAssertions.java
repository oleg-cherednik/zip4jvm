package ru.olegcherednik.zip4jvm.assertj;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.assertj.core.api.Assertions;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 24.03.2019
 */
@SuppressWarnings({ "ExtendsUtilityClass", "MethodCanBeVariableArityMethod" })
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Zip4jvmAssertions extends Assertions {

    public static ZipFileAssert assertThatZipFile(Path zip) throws IOException {
        ZipFileDecorator decorator = isSplit(zip) ? new ZipFileSplitDecorator(zip) : new ZipFileNoSplitNoEncryptedDecorator(zip);
        return Zip4jvmAssertionsForClassTypes.assertThat(decorator);
    }

    public static ZipFileAssert assertThatZipFile(Path zip, char[] password) throws IOException {
        ZipFileDecorator decorator = isSplit(zip) ? new ZipFileSplitDecorator(zip, password) : new ZipFileEncryptedDecoder(zip, password);
        return Zip4jvmAssertionsForClassTypes.assertThat(decorator);
    }

    public static DirectoryAssert assertThatDirectory(Path path) {
        return Zip4jvmAssertionsForClassTypes.assertThatDirectory(path);
    }

    public static AbstractFileExtAssert<?> assertThatFile(Path path) {
        return Zip4jvmAssertionsForClassTypes.assertThatFile(path);
    }

    private static boolean isSplit(Path zip) {
        return Files.exists(ZipModel.getSplitFilePath(zip, 1));
    }

}
