package ru.olegcherednik.zip4jvm.assertj;

import org.assertj.core.api.AbstractFileAssert;
import org.assertj.core.internal.Failures;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author Oleg Cherednik
 * @since 27.03.2019
 */
public class DirectoryAssert extends AbstractFileAssert<DirectoryAssert> implements IDirectoryAssert<DirectoryAssert> {

    public DirectoryAssert(Path actual) {
        super(actual.toFile(), DirectoryAssert.class);
    }

    @Override
    public DirectoryAssert directory(String name) {
        if ("/".equals(name))
            throw new Zip4jvmException("Name cannot be '/'");

        return new DirectoryAssert(actual.toPath().resolve(name));
    }

    @Override
    public DirectoryAssert hasDirectories(int expected) {
        long actual = getFoldersAmount();

        if (actual != expected)
            throw Failures.instance().failure(
                    String.format("Directory '%s' contains illegal amount of directories: actual - '%d', expected - '%d'",
                            this.actual.getAbsolutePath(), actual, expected));

        return myself;
    }

    @Override
    public DirectoryAssert hasFiles(int expected) {
        long actual = getRegularFilesAmount();

        if (actual != expected)
            throw Failures.instance().failure(String.format("Directory '%s' contains illegal amount of files: actual - '%d', expected - '%d'",
                    this.actual.getAbsolutePath(), actual, expected));

        return myself;
    }

    @Override
    public DirectoryAssert exists() {
        super.exists();
        isDirectory();
        return myself;
    }

    private long getFoldersAmount() {
        try {
            return Files.list(actual.toPath()).filter(path -> Files.isDirectory(path)).count();
        } catch(IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    public DirectoryAssert matches(Consumer<IDirectoryAssert<?>> consumer) {
        consumer.accept(this);
        return myself;
    }

    @Override
    public FileExtAssert file(String name) {
        return new FileExtAssert(actual.toPath().resolve(name));
    }

    private long getRegularFilesAmount() {
        try {
            return Files.list(actual.toPath()).filter(path -> Files.isRegularFile(path)).count();
        } catch(IOException e) {
            throw new Zip4jvmException(e);
        }
    }

}
