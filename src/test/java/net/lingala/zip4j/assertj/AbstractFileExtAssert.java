package net.lingala.zip4j.assertj;

import org.assertj.core.api.AbstractFileAssert;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 28.03.2019
 */
@SuppressWarnings("NewClassNamingConvention")
public class AbstractFileExtAssert<SELF extends AbstractFileExtAssert<SELF>> extends AbstractFileAssert<SELF> {

    public AbstractFileExtAssert(Path actual, Class<?> selfType) {
        super(actual.toFile(), selfType);
    }

    public SELF isImage() {
        try (InputStream in = new FileInputStream(actual)) {
            assertThat(ImageIO.read(in)).isNotNull();
        } catch(Exception e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }

        return myself;
    }

    public SELF hasSize(long size) {
        try {
            assertThat(actual.length()).isEqualTo(size);
        } catch(Exception e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }

        return myself;
    }

    public SELF hasEmptyContent() {
        return hasContent("");
    }

    @Override
    public SELF exists() {
        super.exists();
        isFile();
        return myself;
    }

}
