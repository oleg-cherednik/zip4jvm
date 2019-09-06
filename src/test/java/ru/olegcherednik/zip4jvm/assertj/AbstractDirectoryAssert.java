package ru.olegcherednik.zip4jvm.assertj;

import org.assertj.core.api.AbstractFileAssert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 27.03.2019
 */
@SuppressWarnings("NewClassNamingConvention")
public class AbstractDirectoryAssert<SELF extends AbstractDirectoryAssert<SELF>> extends AbstractFileAssert<SELF> {

    public AbstractDirectoryAssert(Path actual, Class<?> selfType) {
        super(actual.toFile(), selfType);
    }

    // TODO name == "/" -> will give root like d:\

    public AbstractDirectoryAssert<?> directory(String name) {
        return new DirectoryAssert(actual.toPath().resolve(name));
    }

    public SELF hasSubDirectories(int expected) {
        assertThat(getFoldersAmount()).isEqualTo(expected);
        return myself;
    }

    public SELF hasFiles(int expected) {
        assertThat(getRegularFilesAmount()).isEqualTo(expected);
        return myself;
    }

    @Override
    public SELF exists() {
        super.exists();
        isDirectory();
        return myself;
    }

    private long getFoldersAmount() {
        try {
            return Files.list(actual.toPath()).filter(path -> Files.isDirectory(path)).count();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SELF matches(Consumer<AbstractDirectoryAssert<?>> consumer) {
        consumer.accept(this);
        return myself;
    }

    public AbstractFileExtAssert<?> file(String name) {
        return new FileAssert(actual.toPath().resolve(name));
    }

    private long getRegularFilesAmount() {
        try {
            return Files.list(actual.toPath()).filter(path -> Files.isRegularFile(path)).count();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

}
