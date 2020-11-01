package ru.olegcherednik.zip4jvm.model;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 28.09.2019
 */
@Test
public class CompressionTest {

    public void shouldParseCompressionMethodWhenSupportedMethod() {
        assertThat(Compression.parseCompressionMethod(CompressionMethod.STORE)).isSameAs(Compression.STORE);
        assertThat(Compression.parseCompressionMethod(CompressionMethod.DEFLATE)).isSameAs(Compression.DEFLATE);
    }

    public void shouldThrowExceptionWhenCompressionMethodNotSupported() {
        for (CompressionMethod compressionMethod : CompressionMethod.values())
            if (parseCompressionMethod(compressionMethod) == null)
                assertThatThrownBy(() -> Compression.parseCompressionMethod(compressionMethod))
                        .isExactlyInstanceOf(EnumConstantNotPresentException.class);
    }

    private static Compression parseCompressionMethod(CompressionMethod compressionMethod) {
        try {
            return Compression.parseCompressionMethod(compressionMethod);
        } catch(EnumConstantNotPresentException ignore) {
            return null;
        }
    }

}
