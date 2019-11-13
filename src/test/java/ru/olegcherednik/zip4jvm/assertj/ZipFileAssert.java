package ru.olegcherednik.zip4jvm.assertj;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.internal.Failures;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
@SuppressWarnings("MethodCanBeVariableArityMethod")
public class ZipFileAssert extends AbstractAssert<ZipFileAssert, ZipFileDecorator> {

    public ZipFileAssert(ZipFileDecorator actual) {
        super(actual, ZipFileAssert.class);
    }

    public AbstractZipEntryDirectoryAssert<?> root() {
        return directory("/");
    }

    public AbstractZipEntryDirectoryAssert<?> directory(String name) {
        ZipEntry entry = new ZipEntry(name);

        if (!entry.isDirectory())
            throw Failures.instance().failure(
                    String.format("Zip file does not contain directory entry '%s' (directory entry should end with '/'", name));

        return new ZipEntryDirectoryAssert(entry, actual);
    }

    public AbstractZipEntryFileAssert<?> file(String name) {
        ZipEntry entry = actual.getEntry(name);

        if (entry.isDirectory())
            throw Failures.instance().failure(
                    String.format("Zip file does not contain file entry '%s' (file entry should not end with '/'", name));

        return new ZipEntryFileAssert(entry, actual);
    }

    public AbstractZipEntryFileAssert<?> file(String name, char[] password) {
        ZipEntry entry = actual.getEntry(name);
        assertThat(entry.isDirectory()).isFalse();
        return new ZipEntryFileAssert(entry, actual);
    }

    public static ZipFileAssert assertThatZipFile(Path zipFile, char[] password) throws IOException {
        return Zip4jvmAssertionsForClassTypes.assertThat(new ZipFileEncryptedDecoder(zipFile, password));
    }

    public ZipFileAssert exists() {
        isNotNull();
        assertThat(Files.exists(actual.getZip())).isTrue();
        assertThat(Files.isRegularFile(actual.getZip())).isTrue();
        return myself;
    }

    public ZipFileAssert hasCommentSize(int size) {
        if (size == 0)
            assertThat(actual.getComment()).isNull();
        else
            assertThat(actual.getComment()).hasSize(size);

        return myself;
    }

    public ZipFileAssert hasComment(String comment) {
        assertThat(actual.getComment()).isEqualTo(comment);
        return myself;
    }

}
