package ru.olegcherednik.zip4jvm.crypto.aes;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 22.09.2019
 */
@Test
public class AesStrengthTest {

    public void shouldThrowExceptionWhenParseUnknownCode() {
        assertThatThrownBy(() -> AesStrength.parseValue(-1)).isExactlyInstanceOf(EnumConstantNotPresentException.class);
    }

}
