package ru.olegcherednik.zip4jvm.assertj;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
@SuppressWarnings("NewClassNamingConvention")
public abstract class AbstractZipEntryFileAssert<SELF extends AbstractZipEntryFileAssert<SELF>> extends AbstractZipEntryAssert<SELF> {

    private final char[] password;

    @SuppressWarnings({ "MethodCanBeVariableArityMethod", "AssignmentOrReturnOfFieldWithMutableType" })
    protected AbstractZipEntryFileAssert(ZipEntry actual, Class<?> selfType, ZipFileDecorator zipFile, char[] password) {
        super(actual, selfType, zipFile);
        this.password = password;
    }

    public SELF hasSize(long size) {
        if (actual.getSize() == -1) {
            try (InputStream in = zipFile.getInputStream(actual)) {
                actual.setSize(in.available());
            } catch(Exception e) {
                assertThatThrownBy(() -> {
                    throw e;
                }).doesNotThrowAnyException();
            }
        }

        assertThat(actual.getSize()).isEqualTo(size);
        return myself;
    }

    public SELF isImage() {
//        try (InputStream in = zipFile.getInputStream(actual)) {
//            actual.setSize(in.available());
//            assertThat(ImageIO.read(in)).isNotNull();
//        } catch(Exception e) {
//            assertThatThrownBy(() -> {
//                throw e;
//            }).doesNotThrowAnyException();
//        }

        return myself;
    }

    public SELF hasEmptyContent() {
        return hasContent("");
    }

    private static final Pattern NEW_LINE = Pattern.compile("\\r?\\n");

    public SELF hasContent(String expected) {
        try (InputStream in = zipFile.getInputStream(actual)) {
            String[] expectedLines = expected.isEmpty() ? ArrayUtils.EMPTY_STRING_ARRAY : NEW_LINE.split(expected);

            List<String> lines = IOUtils.readLines(in, StandardCharsets.UTF_8);
            assertThat(lines).hasSize(expectedLines.length);

            int i = 0;

            for (String line : lines)
                assertThat(line).isEqualTo(expectedLines[i++]);
        } catch(Exception e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }

        return myself;
    }

    public SELF hasComment(String comment) {
        if (comment == null)
            assertThat(actual.getComment()).isNull();
        else
            assertThat(actual.getComment()).isEqualTo(comment);
        return myself;
    }
}
