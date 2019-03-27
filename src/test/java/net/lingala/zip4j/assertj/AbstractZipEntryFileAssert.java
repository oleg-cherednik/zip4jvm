package net.lingala.zip4j.assertj;

import javax.imageio.ImageIO;
import java.io.InputStream;
import java.util.zip.ZipEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
@SuppressWarnings("NewClassNamingConvention")
public abstract class AbstractZipEntryFileAssert<SELF extends AbstractZipEntryFileAssert<SELF>> extends AbstractZipEntryAssert<SELF> {

    protected AbstractZipEntryFileAssert(ZipEntry actual, Class<?> selfType, ZipFileDecorator zipFile) {
        super(actual, selfType, zipFile);
    }

    public SELF hasSize(long size) {
        assertThat(actual.getSize()).isEqualTo(size);
        return myself;
    }

    public SELF isImage() {
        try (InputStream in = zipFile.getInputStream(actual)) {
            actual.setSize(in.available());
            assertThat(ImageIO.read(in)).isNotNull();
        } catch(Exception e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }

        return myself;
    }
}
