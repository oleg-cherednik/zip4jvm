package ru.olegcherednik.zip4jvm.model;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 28.09.2019
 */
@Test
public class CompressionMethodTest {

    public void shouldRetrieveCompressionMethodWhenKnownCode() {
        for (CompressionMethod method : CompressionMethod.values())
            assertThat(CompressionMethod.parseCode(method.getCode())).isSameAs(method);
    }

    public void shouldThrowExceptionWhenUnknownCode() {
        assertThatThrownBy(() -> CompressionMethod.parseCode(-1)).isExactlyInstanceOf(EnumConstantNotPresentException.class);
    }

    public void shouldRetrieveNotBlankTitleWhenGetTitle() {
        for (CompressionMethod method : CompressionMethod.values())
            assertThat(method.getTitle()).isNotBlank();
    }

}
