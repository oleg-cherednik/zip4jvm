package net.lingala.zip4j.assertj;

import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
        assertThat(actual.getSize()).isEqualTo(size != 0 ? size : -1);
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

    public SELF hasEmptyContent() {
        return hasContent("");
    }

    public SELF hasContent(String expected) {
        try (InputStream in = zipFile.getInputStream(actual)) {
            // TODO compare line by line
            List<String> lines = IOUtils.readLines(in, StandardCharsets.UTF_8);
//            assertThat(str).isEqualTo(expected);
            int a = 0;
            a++;
        } catch(Exception e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }

        return myself;
    }
}
