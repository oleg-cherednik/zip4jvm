package net.lingala.zip4j.assertj;

import javax.imageio.ImageIO;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static net.lingala.zip4j.assertj.Zip4jAssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
@SuppressWarnings("NewClassNamingConvention")
public abstract class AbstractZipEntryFileAssert<SELF extends AbstractZipEntryFileAssert<SELF>> extends AbstractZipEntryAssert<SELF> {

    protected AbstractZipEntryFileAssert(ZipEntry actual, Class<?> selfType, ZipFile zipFile) {
        super(actual, selfType, zipFile);
    }

    public SELF hasSize(long size) {
        if (actual.getSize() == -1)
            actual.setSize(getSize());
        assertThat(actual.getSize()).isEqualTo(size);
        return myself;
    }

    public SELF isImage() {
        try (InputStream in = zipFile.getInputStream(actual)) {
            assertThat(ImageIO.read(in)).isNotNull();
        } catch(Exception e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }

        return myself;
    }

    // TODO move to ZipEntryDecorator
    private long getSize() {
        try (InputStream in = zipFile.getInputStream(actual)) {
            return in.available();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
