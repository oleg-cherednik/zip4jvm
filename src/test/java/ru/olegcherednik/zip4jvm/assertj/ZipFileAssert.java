package ru.olegcherednik.zip4jvm.assertj;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.internal.Failures;

import java.nio.file.Files;
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

    public ZipEntryDirectoryAssert root() {
        return directory("/");
    }

    public ZipEntryDirectoryAssert directory(String name) {
        ZipEntry entry = new ZipEntry(name);

        if (!entry.isDirectory())
            throw Failures.instance().failure(
                    String.format("Zip file does not contain directory entry '%s' (directory entry should end with '/'", name));

        return new ZipEntryDirectoryAssert(entry, actual);
    }

    public ZipEntryFileAssert file(String name) {
        ZipEntry entry = actual.getEntry(name);

        if (entry == null)
            throw Failures.instance().failure(
                    String.format("Zip file does not contain file entry '%s'", name));

        if (entry.isDirectory())
            throw Failures.instance().failure(
                    String.format("Zip file does not contain file entry '%s' (file entry should not end with '/'", name));

        return new ZipEntryFileAssert(entry, actual);
    }

    public ZipEntryFileAssert file(String name, char[] password) {
        ZipEntry entry = actual.getEntry(name);

        if (entry == null)
            throw Failures.instance().failure(
                    String.format("Zip file does not contain file entry '%s'", name));

        assertThat(entry.isDirectory()).isFalse();
        return new ZipEntryFileAssert(entry, actual);
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
