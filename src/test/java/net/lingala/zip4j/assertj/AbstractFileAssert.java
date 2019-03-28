package net.lingala.zip4j.assertj;

import org.assertj.core.api.AbstractAssert;

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
@SuppressWarnings("NewClassNamingConvention")
public class AbstractFileAssert<SELF extends AbstractFileAssert<SELF>> extends AbstractAssert<SELF, Path> {

    public AbstractFileAssert(Path actual, Class<?> selfType) {
        super(actual, selfType);
    }

    public SELF isImage() {
        try (InputStream in = new FileInputStream(actual.toFile())) {
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
            assertThat(Files.size(actual)).isEqualTo(size);
        } catch(Exception e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }

        return myself;
    }

    public SELF exists() {
        isNotNull();
        assertThat(Files.exists(actual)).isTrue();
        assertThat(Files.isRegularFile(actual)).isTrue();
        return myself;
    }

}
