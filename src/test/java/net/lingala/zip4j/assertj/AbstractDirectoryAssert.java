package net.lingala.zip4j.assertj;

import org.assertj.core.api.AbstractAssert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 27.03.2019
 */
@SuppressWarnings("NewClassNamingConvention")
public class AbstractDirectoryAssert<SELF extends AbstractDirectoryAssert<SELF>> extends AbstractAssert<SELF, Path> {

    public AbstractDirectoryAssert(Path actual, Class<?> selfType) {
        super(actual, selfType);
    }

    public SELF hasSubDirectories(int expected) {
        assertThat(getFoldersAmount()).isEqualTo(expected);
        return myself;
    }

    public SELF hasFiles(int expected) {
        assertThat(getRegularFilesAmount()).isEqualTo(expected);
        return myself;
    }

    public SELF exists() {
        isNotNull();
        assertThat(Files.exists(actual)).isTrue();
        assertThat(Files.isDirectory(actual)).isTrue();
        return myself;
    }

    private long getFoldersAmount() {
        try {
            return Files.list(actual).filter(path -> Files.isDirectory(path)).count();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long getRegularFilesAmount() {
        try {
            return Files.list(actual).filter(path -> Files.isRegularFile(path)).count();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

}
