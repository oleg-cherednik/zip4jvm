package ru.olegcherednik.zip4jvm.assertj;

import org.assertj.core.api.AbstractPathAssert;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 28.03.2019
 */
@SuppressWarnings("CatchMayIgnoreException")
public class AbstractFileExtAssert<S extends AbstractFileExtAssert<S>> extends AbstractPathAssert<S> implements IFileAssert<S> {

    public AbstractFileExtAssert(Path actual, Class<?> selfType) {
        super(actual, selfType);
    }

    @Override
    public S isImage() {
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
    public S hasSize(long size) {
        try {
            assertThat(Files.size(actual)).isEqualTo(size);
        } catch(Exception e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }

        return myself;
    }

    public S hasEmptyContent() {
        return hasContent("");
    }

    @Override
    public S exists() {
        int a = 0;
        a++;
        super.exists();
        isRegularFile();
        return myself;
    }

}
