package ru.olegcherednik.zip4jvm.assertj;

import org.assertj.core.api.AbstractPathAssert;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 28.03.2019
 */
@SuppressWarnings("CatchMayIgnoreException")
public class FileExtAssert extends AbstractPathAssert<FileExtAssert> implements IFileAssert<FileExtAssert> {

    public FileExtAssert(Path actual) {
        super(actual, FileExtAssert.class);
    }

    @Override
    public FileExtAssert isImage() {
        try (InputStream in = new FileInputStream(actual.toFile())) {
            assertThat(ImageIO.read(in)).isNotNull();
        } catch(Exception e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }

        return myself;
    }

    @Override
    public FileExtAssert hasSize(long size) {
        try {
            assertThat(Files.size(actual)).isEqualTo(size);
        } catch(Exception e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }

        return myself;
    }

    public FileExtAssert matches(Consumer<IFileAssert<?>> consumer) {
        consumer.accept(this);
        return myself;
    }

    @Override
    public FileExtAssert exists() {
        super.exists();
        isRegularFile();
        return myself;
    }

}
