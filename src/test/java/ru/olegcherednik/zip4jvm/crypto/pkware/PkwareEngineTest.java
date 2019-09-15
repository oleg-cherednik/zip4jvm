package ru.olegcherednik.zip4jvm.crypto.pkware;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 15.09.2019
 */
@Test
public class PkwareEngineTest {

    public void shouldThrowNullPointerExceptionWhenNull() {
        assertThatThrownBy(() -> new PkwareEngine(null)).isExactlyInstanceOf(NullPointerException.class);
    }
}
